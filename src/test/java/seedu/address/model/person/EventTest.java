package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    public void constructor_validEvent_createsEvent() {
        Event event = new Event("NUS Career Fair", "2026-10-20");

        assertEquals("NUS Career Fair", event.getName());
        assertEquals("2026-10-20", event.getDate());
    }

    @Test
    public void constructor_eventNameWithSpaces_trimsName() {
        Event event = new Event("  NUS Career Fair  ", "2026-10-20");

        assertEquals("NUS Career Fair", event.getName());
    }

    @Test
    public void constructor_blankName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Event("   ", "2026-10-20"));
    }

    @Test
    public void constructor_invalidDate_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Event("NUS Career Fair", "20-10-2026"));
    }

    @Test
    public void constructor_impossibleDate_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Event("NUS Career Fair", "2026-02-30"));
    }

    @Test
    public void isValidName_validName_returnsTrue() {
        assertTrue(Event.isValidName("NUS Career Fair"));
    }

    @Test
    public void isValidName_blankName_returnsFalse() {
        assertFalse(Event.isValidName("   "));
    }

    @Test
    public void isValidDate_validDate_returnsTrue() {
        assertTrue(Event.isValidDate("2026-10-20"));
    }

    @Test
    public void isValidDate_invalidDate_returnsFalse() {
        assertFalse(Event.isValidDate("20-10-2026"));
    }

    @Test
    public void isValidDate_impossibleDate_returnsFalse() {
        assertFalse(Event.isValidDate("2026-02-30"));
    }

    @Test
    public void equals_sameValues_returnsTrue() {
        Event firstEvent = new Event("NUS Career Fair", "2026-10-20");
        Event secondEvent = new Event("NUS Career Fair", "2026-10-20");

        assertTrue(firstEvent.equals(secondEvent));
    }

    @Test
    public void equals_differentName_returnsFalse() {
        Event firstEvent = new Event("NUS Career Fair", "2026-10-20");
        Event secondEvent = new Event("Career Expo", "2026-10-20");

        assertFalse(firstEvent.equals(secondEvent));
    }

    @Test
    public void equals_differentDate_returnsFalse() {
        Event firstEvent = new Event("NUS Career Fair", "2026-10-20");
        Event secondEvent = new Event("NUS Career Fair", "2026-10-21");

        assertFalse(firstEvent.equals(secondEvent));
    }

    @Test
    public void equals_differentCase_returnsTrue() {
        Event firstEvent = new Event("NUS Career Fair", "2026-10-20");
        Event secondEvent = new Event("nus career fair", "2026-10-20");

        assertTrue(firstEvent.equals(secondEvent));
    }

    @Test
    public void equals_null_returnsFalse() {
        Event event = new Event("NUS Career Fair", "2026-10-20");

        assertFalse(event.equals(null));
    }

    @Test
    public void equals_differentType_returnsFalse() {
        Event event = new Event("NUS Career Fair", "2026-10-20");

        assertFalse(event.equals("NUS Career Fair"));
    }

    @Test
    public void toStringMethod_returnsFormattedEvent() {
        Event event = new Event("NUS Career Fair", "2026-10-20");

        assertEquals("NUS Career Fair (2026-10-20)", event.toString());
    }

    @Test
    public void isValidName_nullName_returnsFalse() {
        assertFalse(Event.isValidName(null));
    }

    @Test
    public void isValidDate_nullDate_returnsFalse() {
        assertFalse(Event.isValidDate(null));
    }
}
