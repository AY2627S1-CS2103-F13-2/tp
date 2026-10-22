package seedu.address.logic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user. Fields the person does not have are omitted.
     */
    public static String format(Person person) {
        return format(person, FieldOrder.DEFAULT);
    }

    /**
     * Formats full details in the requested order, omitting absent fields.
     */
    public static String format(Person person, FieldOrder order) {
        return order.getFields().stream().map(field -> formatField(person, field))
                .filter(value -> !value.isEmpty()).collect(Collectors.joining("; "));
    }

    private static String formatField(Person person, FieldOrder.Field field) {
        return switch (field) {
            case NAME -> person.getName().toString();
            case TAGS -> person.getTags().isEmpty() ? "" : "Tags: " + person.getTags().stream()
                    .map(Object::toString).sorted().collect(Collectors.joining());
            case COMPANY -> person.getCompany().map(value -> "Company: " + value).orElse("");
            case ROLE -> person.getRole().map(value -> "Role: " + value).orElse("");
            case PHONE -> person.getPhone().map(value -> "Phone: " + value).orElse("");
            case EMAIL -> person.getEmail().map(value -> "Email: " + value).orElse("");
            case LINKEDIN -> person.getLinkedin().map(value -> "LinkedIn: " + value).orElse("");
            case FOLLOWUPS -> person.getFollowUps().isEmpty() ? "" : "Follow-ups:\n" + formatFollowUps(person);
            case INTERACTIONS -> person.getInteractions().isEmpty() ? ""
                    : "Interaction history:\n" + formatInteractions(person.getInteractions());
        };
    }

    /**
     * Returns the interactions as numbered lines in the given order, e.g. "1. 2026-10-15: Discussed internships".
     */
    public static String formatInteractions(List<Interaction> interactions) {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < interactions.size(); i++) {
            if (i > 0) {
                history.append("\n");
            }
            history.append(i + 1).append(". ").append(interactions.get(i));
        }
        return history.toString();
    }

    /**
     * Formats actions in stored order with their stable IDs and statuses.
     */
    public static String formatFollowUps(Person person) {
        return person.getFollowUps().stream().map(Object::toString).collect(Collectors.joining("\n"));
    }
}
