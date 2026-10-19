package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import seedu.address.commons.util.StringUtil;

/**
 * Represents one recorded interaction with a contact: when it happened and what was discussed.
 * Guarantees: immutable; the note is not blank.
 */
public class Interaction {

    public static final String DATE_FORMAT = "YYYY-MM-DD";
    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Interaction dates should be real dates in the format " + DATE_FORMAT + ", e.g. 2026-10-15.";
    public static final String MESSAGE_NOTE_CONSTRAINTS = "Interaction notes should not be blank.";

    public final LocalDate date;
    public final String note;

    /**
     * Constructs an {@code Interaction} on {@code date} with the given {@code note}.
     * Spaces in the note are normalised, as they are for other contact details.
     */
    public Interaction(LocalDate date, String note) {
        requireNonNull(date);
        requireNonNull(note);
        checkArgument(isValidNote(note), MESSAGE_NOTE_CONSTRAINTS);
        this.date = date;
        this.note = StringUtil.normalise(note);
    }

    /**
     * Returns true if {@code test} is a valid interaction note.
     */
    public static boolean isValidNote(String test) {
        return test != null && !test.isBlank();
    }

    /**
     * Returns true if {@code test} is a real date in the format {@value #DATE_FORMAT}.
     */
    public static boolean isValidDate(String test) {
        if (test == null) {
            return false;
        }
        try {
            LocalDate.parse(test.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if {@code other} records the same date and the same note, ignoring letter case.
     */
    public boolean isSameInteraction(Interaction other) {
        return other != null && date.equals(other.date) && note.equalsIgnoreCase(other.note);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Interaction otherInteraction)) {
            return false;
        }

        return date.equals(otherInteraction.date) && note.equals(otherInteraction.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, note);
    }

    /**
     * Returns the interaction as "DATE: NOTE", e.g. "2026-10-15: Discussed internships".
     */
    @Override
    public String toString() {
        return date + ": " + note;
    }
}
