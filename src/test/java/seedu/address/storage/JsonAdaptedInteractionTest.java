package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

public class JsonAdaptedInteractionTest {

    private static final Interaction INTERACTION = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");

    @Test
    public void toModelType_validInteraction_returnsInteraction() throws Exception {
        assertEquals(INTERACTION, new JsonAdaptedInteraction(INTERACTION).toModelType());
    }

    @Test
    public void toModelType_missingDate_throwsIllegalValueException() {
        String expected = String.format(JsonAdaptedInteraction.MISSING_FIELD_MESSAGE_FORMAT, "date");
        assertThrows(IllegalValueException.class, expected, () ->
                new JsonAdaptedInteraction(null, "Coffee chat").toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_DATE_CONSTRAINTS, () ->
                new JsonAdaptedInteraction("2026-02-30", "Coffee chat").toModelType());
    }

    @Test
    public void toModelType_missingNote_throwsIllegalValueException() {
        String expected = String.format(JsonAdaptedInteraction.MISSING_FIELD_MESSAGE_FORMAT, "note");
        assertThrows(IllegalValueException.class, expected, () ->
                new JsonAdaptedInteraction("2026-10-15", null).toModelType());
    }

    @Test
    public void toModelType_blankNote_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_NOTE_CONSTRAINTS, () ->
                new JsonAdaptedInteraction("2026-10-15", "  ").toModelType());
    }
}
