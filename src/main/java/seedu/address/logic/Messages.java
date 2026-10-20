package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
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
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName());
        person.getCompany().ifPresent(company -> builder.append("; Company: ").append(company));
        person.getRole().ifPresent(role -> builder.append("; Role: ").append(role));
        person.getPhone().ifPresent(phone -> builder.append("; Phone: ").append(phone));
        person.getEmail().ifPresent(email -> builder.append("; Email: ").append(email));
        person.getLinkedin().ifPresent(linkedin -> builder.append("; LinkedIn: ").append(linkedin));
        if (!person.getTags().isEmpty()) {
            builder.append("; Tags: ");
            person.getTags().forEach(builder::append);
        }
        if (!person.getFollowUps().isEmpty()) {
            builder.append("; Follow-ups:\n").append(formatFollowUps(person));
        }
        return builder.toString();
    }

    /**
     * Formats actions in stored order with their stable IDs and statuses.
     */
    public static String formatFollowUps(Person person) {
        return person.getFollowUps().stream().map(Object::toString).collect(Collectors.joining("\n"));
    }
}
