package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

import seedu.address.commons.util.StringUtil;

/**
 * Represents an event associated with a contact.
 */
public class Event {

    public static final String MESSAGE_NAME_CONSTRAINTS =
            "Event names must not be blank.";

    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Event dates must use the format YYYY-MM-DD.";

    private final String name;
    private final String date;

    /**
     * Creates an event without a date.
     *
     * @param name name of the event
     */
    public Event(String name) {
        requireNonNull(name);

        String normalizedName = StringUtil.normalise(name);
        checkArgument(isValidName(normalizedName), MESSAGE_NAME_CONSTRAINTS);

        this.name = normalizedName;
        this.date = null;
    }

    /**
     * Creates an event with an optional date.
     *
     * @param name name of the event
     * @param date date of the event in YYYY-MM-DD format
     */
    public Event(String name, String date) {
        requireNonNull(name);
        requireNonNull(date);

        String normalizedName = StringUtil.normalise(name);
        String normalizedDate = date.trim();

        checkArgument(isValidName(normalizedName), MESSAGE_NAME_CONSTRAINTS);
        checkArgument(isValidDate(normalizedDate), MESSAGE_DATE_CONSTRAINTS);

        this.name = normalizedName;
        this.date = normalizedDate;
    }

    /**
     * Returns true if the given event name is valid.
     */
    public static boolean isValidName(String name) {
        return name != null
                && !StringUtil.normalise(name).isBlank();
    }

    /**
     * Returns true if the given date is valid and uses YYYY-MM-DD format.
     */
    public static boolean isValidDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Event otherEvent)) {
            return false;
        }

        return name.equalsIgnoreCase(otherEvent.name)
                && Objects.equals(date, otherEvent.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toUpperCase(Locale.ROOT), date);
    }

    @Override
    public String toString() {
        return date == null
                ? name
                : name + " (" + date + ")";
    }
}
