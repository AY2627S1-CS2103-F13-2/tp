package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    private final ObservableValue<FieldOrder> fieldOrder;
    private final boolean compact;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, new ReadOnlyObjectWrapper<>(FieldOrder.DEFAULT).getReadOnlyProperty());
    }

    /**
     * Creates a panel that refreshes existing cards whenever the global field order changes.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableValue<FieldOrder> fieldOrder) {
        this(personList, fieldOrder, false);
    }

    /**
     * Creates a list with optional compact summaries for a separate detail pane.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableValue<FieldOrder> fieldOrder, boolean compact) {
        super(FXML);
        this.fieldOrder = fieldOrder;
        this.compact = compact;
        fieldOrder.addListener((observable, previous, current) -> personListView.refresh());
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.setPlaceholder(new Label("No matching contacts"));
        if (compact) {
            personListView.getStyleClass().add("contact-summaries");
        }
    }

    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Selects the requested contact if it is in the displayed list.
     */
    public void selectPerson(Person person) {
        int index = personListView.getItems().indexOf(person);
        if (index >= 0) {
            personListView.getSelectionModel().select(index);
            personListView.scrollTo(index);
        }
    }

    /**
     * Selects the first result when the current filter has no selection.
     */
    public void selectFirstIfNeeded() {
        if (selectedPersonProperty().get() == null && !personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().selectFirst();
        }
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else if (compact) {
                Label index = new Label((getIndex() + 1) + ".");
                index.getStyleClass().add("contact-index");
                Label summary = new Label(person.getName().fullName
                        + person.getCompany().map(company -> " — " + company.value).orElse("")
                        + (person.isStarred() ? " ★" : ""));
                summary.setMinWidth(0);
                summary.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(summary, Priority.ALWAYS);
                HBox row = new HBox(10, index, summary);
                row.setMinWidth(0);
                row.prefWidthProperty().bind(personListView.widthProperty().subtract(38));
                row.getStyleClass().add("contact-summary");
                setText(null);
                setGraphic(row);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, fieldOrder.getValue()).getRoot());
            }
        }
    }

}
