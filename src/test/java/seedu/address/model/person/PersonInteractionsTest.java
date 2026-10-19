package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Contains tests for the interaction history of {@code Person}.
 */
public class PersonInteractionsTest {

    private static final Interaction OCT_1 = new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat");
    private static final Interaction OCT_15 = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");
    private static final Interaction OCT_15_LATER = new Interaction(LocalDate.of(2026, 10, 15), "Follow-up call");

    @Test
    public void newPerson_hasNoInteractions() {
        assertTrue(ALICE.getInteractions().isEmpty());
    }

    @Test
    public void withInteraction_olderInteraction_keptInDateOrder() {
        Person person = ALICE.withInteraction(OCT_15).withInteraction(OCT_1);

        assertEquals(List.of(OCT_1, OCT_15), person.getInteractions());
    }

    @Test
    public void withInteraction_sameDate_keepsOrderOfRecording() {
        Person person = ALICE.withInteraction(OCT_15).withInteraction(OCT_15_LATER);

        assertEquals(List.of(OCT_15, OCT_15_LATER), person.getInteractions());
    }

    @Test
    public void withInteraction_doesNotChangeOriginalOrOtherDetails() {
        Person person = ALICE.withInteraction(OCT_1);

        assertTrue(ALICE.getInteractions().isEmpty());
        assertEquals(ALICE.getName(), person.getName());
        assertEquals(ALICE.getEmail(), person.getEmail());
        assertEquals(ALICE.getTags(), person.getTags());
    }

    @Test
    public void getInteractions_modifyList_throwsUnsupportedOperationException() {
        Person person = ALICE.withInteraction(OCT_1);
        assertThrows(UnsupportedOperationException.class, () -> person.getInteractions().clear());
    }

    @Test
    public void hasInteraction() {
        Person person = ALICE.withInteraction(OCT_15);

        assertTrue(person.hasInteraction(new Interaction(OCT_15.date, "coffee chat")));
        assertFalse(person.hasInteraction(OCT_1));
    }

    @Test
    public void equals_differentInteractions_returnsFalse() {
        assertFalse(ALICE.equals(ALICE.withInteraction(OCT_1)));
        assertTrue(ALICE.withInteraction(OCT_1).equals(ALICE.withInteraction(OCT_1)));
        assertEquals(ALICE.withInteraction(OCT_1).hashCode(), ALICE.withInteraction(OCT_1).hashCode());
    }
}
