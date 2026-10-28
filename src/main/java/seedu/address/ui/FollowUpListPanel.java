package seedu.address.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.FollowUpListing;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.person.FollowUp;

/**
 * Presents follow-up results as selectable rows using the command's exact numbered order.
 */
public class FollowUpListPanel extends UiPart<Region> {
    private static final String FXML = "FollowUpListPanel.fxml";

    @FXML
    private Label followUpHeading;

    @FXML
    private ListView<FollowUpEntry> followUpListView;

    /**
     * Creates a list that supports mouse and keyboard selection.
     */
    public FollowUpListPanel() {
        super(FXML);
        followUpListView.setPlaceholder(new Label("No follow-ups match this filter."));
        followUpListView.setCellFactory(list -> new FollowUpCell());
    }

    public ReadOnlyObjectProperty<FollowUpEntry> selectedEntryProperty() {
        return followUpListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Replaces the results and selects the first action, clearing any previous filter's selection.
     */
    public void showListing(FollowUpListing listing) {
        followUpHeading.setText(listing.heading() + " (" + listing.entries().size() + ")");
        followUpListView.setItems(FXCollections.observableArrayList(listing.entries()));
        if (!listing.entries().isEmpty()) {
            followUpListView.getSelectionModel().selectFirst();
        }
    }

    /**
     * Synchronizes the highlight after a numbered selection through Logic.
     */
    public void selectEntry(FollowUpEntry entry) {
        followUpListView.getSelectionModel().select(entry);
        followUpListView.scrollTo(entry);
    }

    private class FollowUpCell extends ListCell<FollowUpEntry> {
        @Override
        protected void updateItem(FollowUpEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            setText(null);
            if (empty || entry == null) {
                setGraphic(null);
                return;
            }
            FollowUp action = entry.followUp();
            Label contact = new Label((getIndex() + 1) + ". " + entry.contact().getName().fullName);
            contact.getStyleClass().add("followup-contact");
            Label description = new Label(action.getDescription());
            description.setWrapText(true);
            description.setMinWidth(0);
            String status = action.isOverdue() ? "OVERDUE" : action.getStatus().toString();
            Label metadata = new Label("#" + action.getId() + " · " + status + " · "
                    + action.getDueDate().map(date -> "Due " + date).orElse("No due date"));
            metadata.getStyleClass().add(action.isOverdue() ? "followup-overdue"
                    : action.getStatus() == FollowUp.Status.COMPLETED ? "followup-completed" : "followup-metadata");
            VBox row = new VBox(5, contact, description, metadata);
            row.getStyleClass().add("followup-row");
            row.setMinWidth(0);
            row.prefWidthProperty().bind(followUpListView.widthProperty().subtract(38));
            contact.maxWidthProperty().bind(row.widthProperty());
            metadata.maxWidthProperty().bind(row.widthProperty());
            setGraphic(row);
        }
    }
}
