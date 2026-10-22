package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class InteractionTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Interaction(null, "Coffee chat"));
        assertThrows(NullPointerException.class, () -> new Interaction(DATE, null));
    }

    @Test
    public void constructor_blankNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Interaction(DATE, "   "));
    }

    @Test
    public void constructor_noteWithExtraSpaces_normalised() {
        assertEquals("Discussed backend internships",
                new Interaction(DATE, "  Discussed   backend internships ").getNote());
    }

    @Test
    public void isValidNote() {
        assertTrue(Interaction.isValidNote("Coffee chat"));
        assertFalse(Interaction.isValidNote(null));
        assertFalse(Interaction.isValidNote(" "));
    }

    @Test
    public void isValidDate() {
        assertTrue(Interaction.isValidDate("2026-10-15"));
        assertTrue(Interaction.isValidDate(" 2024-02-29 ")); // leap day, outer spaces

        assertFalse(Interaction.isValidDate(null));
        assertFalse(Interaction.isValidDate(""));
        assertFalse(Interaction.isValidDate("15-10-2026")); // wrong order
        assertFalse(Interaction.isValidDate("2026-02-30")); // not a real date
        assertFalse(Interaction.isValidDate("2026-13-01")); // no 13th month
    }

    @Test
    public void isSameInteraction_ignoresNoteCase() {
        Interaction interaction = new Interaction(DATE, "Coffee chat");

        assertTrue(interaction.isSameInteraction(new Interaction(DATE, "coffee CHAT")));
        assertFalse(interaction.isSameInteraction(new Interaction(DATE.minusDays(1), "Coffee chat")));
        assertFalse(interaction.isSameInteraction(new Interaction(DATE, "Lunch")));
        assertFalse(interaction.isSameInteraction(null));
    }

    @Test
    public void equals() {
        Interaction interaction = new Interaction(DATE, "Coffee chat");

        assertTrue(interaction.equals(interaction));
        assertTrue(interaction.equals(new Interaction(DATE, "Coffee chat")));
        assertEquals(interaction.hashCode(), new Interaction(DATE, "Coffee chat").hashCode());

        assertFalse(interaction.equals(null));
        assertFalse(interaction.equals("Coffee chat"));
        assertFalse(interaction.equals(new Interaction(DATE, "coffee chat")));
        assertFalse(interaction.equals(new Interaction(DATE.plusDays(1), "Coffee chat")));
    }

    @Test
    public void toStringMethod() {
        assertEquals("2026-10-15: Coffee chat", new Interaction(DATE, "Coffee chat").toString());
    }
}
