package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private HBox nameRow;
    @FXML
    private VBox fields;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label company;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label linkedin;
    @FXML
    private Label introducer;
    @FXML
    private Label events;
    @FXML
    private FlowPane tags;
    @FXML
    private Label followUps;

    /**
     * Creates a {@code PersonCard} with the given person and index.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, FieldOrder.DEFAULT);
    }

    /**
     * Creates a contact card with every present field arranged in the global order.
     */
    public PersonCard(Person person, int displayedIndex, FieldOrder order) {
        super(FXML);
        this.person = person;

        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);

        setOptionalLabel(
                company,
                person.getCompany().map(value -> value.value).orElse(null));
        setOptionalLabel(
                role,
                person.getRole().map(value -> value.value).orElse(null));
        setOptionalLabel(
                phone,
                person.getPhone().map(value -> value.value).orElse(null));
        setOptionalLabel(
                email,
                person.getEmail().map(value -> value.value).orElse(null));
        setOptionalLabel(
                linkedin,
                person.getLinkedin().map(value -> value.value).orElse(null));
        setOptionalLabel(
                introducer,
                person.getIntroducer()
                        .map(value -> "Introduced by: " + value)
                        .orElse(null));
        setOptionalLabel(
                events,
                person.getEvents().isEmpty()
                        ? null
                        : "Events:\n" + formatEvents(person));
        setOptionalLabel(
                followUps,
                person.getFollowUps().isEmpty()
                        ? null
                        : "Follow-ups:\n" + Messages.formatFollowUps(person));

        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));

        tags.setVisible(!person.getTags().isEmpty());
        tags.setManaged(!person.getTags().isEmpty());

        Map<FieldOrder.Field, Node> rows = Map.of(
                FieldOrder.Field.NAME, nameRow,
                FieldOrder.Field.TAGS, tags,
                FieldOrder.Field.COMPANY, company,
                FieldOrder.Field.ROLE, role,
                FieldOrder.Field.PHONE, phone,
                FieldOrder.Field.EMAIL, email,
                FieldOrder.Field.LINKEDIN, linkedin,
                FieldOrder.Field.FOLLOWUPS, followUps,
                FieldOrder.Field.INTRODUCER, introducer);

        List<Node> orderedRows = new ArrayList<>();

        order.getFields().stream()
                .filter(rows::containsKey)
                .forEach(field -> orderedRows.add(rows.get(field)));

        if (!person.getEvents().isEmpty()) {
            orderedRows.add(events);
        }

        fields.getChildren().setAll(orderedRows);
    }

    /**
     * Formats the events for display.
     */
    private static String formatEvents(Person person) {
        return person.getEvents().stream()
                .sorted(Comparator.comparing(
                        Event::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .map(Event::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Sets a label value, or hides the label when the value is absent.
     */
    private static void setOptionalLabel(Label label, String value) {
        if (value == null) {
            label.setVisible(false);
            label.setManaged(false);
            return;
        }

        label.setText(value);
        label.setVisible(true);
        label.setManaged(true);
    }
}
