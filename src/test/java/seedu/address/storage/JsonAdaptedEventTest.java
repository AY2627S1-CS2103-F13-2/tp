package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Event;

public class JsonAdaptedEventTest {

    @Test
    public void toModelType_validEventWithDate_returnsEvent() throws Exception {
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent("NUS Career Fair", "2026-10-20");

        Event expected =
                new Event("NUS Career Fair", "2026-10-20");

        assertEquals(expected, jsonEvent.toModelType());
    }

    @Test
    public void toModelType_validEventWithoutDate_returnsEvent() throws Exception {
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent("NUS Career Fair", null);

        Event expected =
                new Event("NUS Career Fair");

        assertEquals(expected, jsonEvent.toModelType());
    }

    @Test
    public void toModelType_missingName_throwsException() {
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent(null, "2026-10-20");

        assertThrows(
                IllegalValueException.class,
                jsonEvent::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsException() {
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent("   ", "2026-10-20");

        assertThrows(
                IllegalValueException.class,
                jsonEvent::toModelType);
    }

    @Test
    public void toModelType_invalidDate_throwsException() {
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent("NUS Career Fair", "2026-02-30");

        assertThrows(
                IllegalValueException.class,
                jsonEvent::toModelType);
    }

    @Test
    public void constructor_fromEventWithDate_preservesEventDetails()
            throws Exception {
        Event original =
                new Event("NUS Career Fair", "2026-10-20");
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent(original);

        assertEquals(original, jsonEvent.toModelType());
    }

    @Test
    public void constructor_fromEventWithoutDate_preservesEventDetails()
            throws Exception {
        Event original =
                new Event("NUS Career Fair");
        JsonAdaptedEvent jsonEvent =
                new JsonAdaptedEvent(original);

        assertEquals(original, jsonEvent.toModelType());
    }
}
