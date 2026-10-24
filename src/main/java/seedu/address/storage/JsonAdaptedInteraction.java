package seedu.address.storage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

/**
 * Jackson-friendly version of {@link Interaction}.
 */
class JsonAdaptedInteraction {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Interaction's %s field is missing!";
    public static final String MESSAGE_FUTURE_DATE = "Interaction's date %1$s is in the future.";

    private final String date;
    private final String note;

    /**
     * Constructs a {@code JsonAdaptedInteraction} with the given date and note.
     */
    @JsonCreator
    public JsonAdaptedInteraction(@JsonProperty("date") String date, @JsonProperty("note") String note) {
        this.date = date;
        this.note = note;
    }

    /**
     * Converts a given {@code Interaction} into this class for Jackson use.
     */
    public JsonAdaptedInteraction(Interaction source) {
        date = source.getDate().toString();
        note = source.getNote();
    }

    /**
     * Converts this Jackson-friendly adapted interaction into the model's {@code Interaction} object.
     *
     * Applies the same rules as {@code log}, so a hand-edited file cannot hold an interaction
     * that could not have been logged.
     *
     * @throws IllegalValueException if the date or note is missing or invalid, or the date is in the future.
     */
    public Interaction toModelType() throws IllegalValueException {
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"));
        }
        if (!Interaction.isValidDate(date)) {
            throw new IllegalValueException(Interaction.MESSAGE_DATE_CONSTRAINTS);
        }
        if (note == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "note"));
        }
        if (!Interaction.isValidNote(note)) {
            throw new IllegalValueException(Interaction.MESSAGE_NOTE_CONSTRAINTS);
        }
        LocalDate modelDate = LocalDate.parse(date.trim());
        if (modelDate.isAfter(LocalDate.now())) {
            throw new IllegalValueException(String.format(MESSAGE_FUTURE_DATE, modelDate));
        }
        return new Interaction(modelDate, note);
    }
}
