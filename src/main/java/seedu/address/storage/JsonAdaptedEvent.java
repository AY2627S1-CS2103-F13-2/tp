package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Event;

/**
 * Jackson-friendly version of {@link Event}.
 */
class JsonAdaptedEvent {

    public static final String MISSING_FIELD_MESSAGE_FORMAT =
            "Event's %s field is missing!";

    private final String name;
    private final String date;

    /**
     * Constructs an adapted event.
     *
     * @param name event name
     * @param date optional event date
     */
    @JsonCreator
    public JsonAdaptedEvent(
            @JsonProperty("name") String name,
            @JsonProperty("date") String date) {
        this.name = name;
        this.date = date;
    }

    /**
     * Converts an Event into this class for Jackson use.
     *
     * @param source event to convert
     */
    public JsonAdaptedEvent(Event source) {
        name = source.getName();
        date = source.getDate();
    }

    /**
     * Converts this adapted event into a model Event.
     *
     * @throws IllegalValueException if the event data is invalid
     */
    public Event toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }

        if (!Event.isValidName(name)) {
            throw new IllegalValueException(Event.MESSAGE_NAME_CONSTRAINTS);
        }

        if (date == null || date.isBlank()) {
            return new Event(name);
        }

        if (!Event.isValidDate(date)) {
            throw new IllegalValueException(Event.MESSAGE_DATE_CONSTRAINTS);
        }

        return new Event(name, date);
    }
}
