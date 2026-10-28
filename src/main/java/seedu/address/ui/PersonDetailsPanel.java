package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.model.FieldOrder;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.person.Person;

/**
 * A scrollable, labelled detail card for the selected contact.
 */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";

    @FXML
    private VBox fields;

    /**
     * Creates an empty detail card.
     */
    public PersonDetailsPanel() {
        super(FXML);
        showPerson(null, FieldOrder.DEFAULT);
    }

    /**
     * Displays the contact in the saved field order, or an empty-state prompt.
     */
    public void showPerson(Person person, FieldOrder order) {
        if (person == null) {
            Label title = new Label("Your next connection");
            title.getStyleClass().add("detail-name");
            Label hint = new Label("Select a contact to see their details.\nUse find to narrow your list, "
                    + "or add to make a new connection.");
            hint.setWrapText(true);
            hint.getStyleClass().add("detail-empty");
            fields.getChildren().setAll(title, hint);
            return;
        }

        Label name = new Label(person.getName().fullName + (person.isStarred() ? " ★" : ""));
        name.setId("detailName");
        name.setWrapText(true);
        name.getStyleClass().add("detail-name");
        VBox nameRow = new VBox(name);
        nameRow.setId("nameRow");

        Map<FieldOrder.Field, Node> rows = Map.of(
                FieldOrder.Field.NAME, nameRow,
                FieldOrder.Field.TAGS, row("tags", "Tags", person.getTags().stream()
                        .map(tag -> tag.tagName).sorted().collect(Collectors.joining(", "))),
                FieldOrder.Field.COMPANY, row("company", "Company",
                        person.getCompany().map(value -> value.value).orElse("")),
                FieldOrder.Field.ROLE, row("role", "Role", person.getRole().map(value -> value.value).orElse("")),
                FieldOrder.Field.PHONE, row("phone", "Phone", person.getPhone().map(value -> value.value).orElse("")),
                FieldOrder.Field.EMAIL, row("email", "Email", person.getEmail().map(value -> value.value).orElse("")),
                FieldOrder.Field.LINKEDIN, row("linkedin", "LinkedIn",
                        person.getLinkedin().map(value -> value.value).orElse("")),
                FieldOrder.Field.FOLLOWUPS, row("followUps", "Follow-ups", Messages.formatFollowUps(person)),
                FieldOrder.Field.INTERACTIONS, row("interactions", "Notes",
                        Messages.formatInteractions(person.getInteractions())));

        List<Node> ordered = new ArrayList<>();
        for (FieldOrder.Field field : order.getFields()) {
            if (field != FieldOrder.Field.INTERACTIONS || !person.getInteractions().isEmpty()) {
                ordered.add(rows.get(field));
            }
            if (field == FieldOrder.Field.LINKEDIN && person.getIntroducer().isPresent()) {
                ordered.add(row("introducer", "Introduced by", person.getIntroducer().orElseThrow().fullName));
            }
        }
        if (!person.getEvents().isEmpty()) {
            ordered.add(row("events", "Events", person.getEvents().stream()
                    .sorted(Comparator.comparing(event -> event.getName(), String.CASE_INSENSITIVE_ORDER))
                    .map(Object::toString).collect(Collectors.joining("\n"))));
        }
        fields.getChildren().setAll(ordered);
    }

    /**
     * Keeps the selected action visible alongside its contact's details.
     */
    public void showFollowUp(FollowUpEntry entry, FieldOrder order) {
        showPerson(entry.contact(), order);
        Label title = new Label("Selected follow-up");
        title.getStyleClass().add("selected-followup-title");
        Label action = new Label(Messages.formatFollowUp(entry.followUp()));
        action.setId("selectedFollowUpText");
        action.setWrapText(true);
        VBox context = new VBox(title, action);
        context.setId("selectedFollowUp");
        context.getStyleClass().add("selected-followup");
        Node history = fields.lookup("#followUps");
        if (history != null) {
            history.setVisible(false);
            history.setManaged(false);
        }
        fields.getChildren().add(fields.getChildren().indexOf(fields.lookup("#nameRow")) + 1, context);
    }

    private static HBox row(String id, String caption, String text) {
        Label label = new Label(caption);
        label.getStyleClass().add("detail-caption");
        label.setMinWidth(92);
        label.setPrefWidth(92);
        Label value = new Label(text);
        value.getStyleClass().add("detail-value");
        value.setId(id + "Value");
        value.setWrapText(true);
        value.setMinWidth(0);
        value.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(value, Priority.ALWAYS);
        HBox row = new HBox(12, label, value);
        row.setId(id);
        row.getStyleClass().add("detail-row");
        row.setVisible(!text.isBlank());
        row.setManaged(!text.isBlank());
        return row;
    }
}
