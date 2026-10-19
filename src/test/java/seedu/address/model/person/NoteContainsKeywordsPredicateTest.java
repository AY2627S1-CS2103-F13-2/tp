package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class NoteContainsKeywordsPredicateTest {

    private static final Interaction INTERNSHIP = new Interaction(LocalDate.of(2026, 10, 1),
            "Discussed backend Internships");
    private static final Interaction COFFEE = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");

    private final Person alice = ALICE.withInteraction(INTERNSHIP).withInteraction(COFFEE);

    @Test
    public void test_anyKeywordInAnyNoteIgnoringCase_returnsTrue() {
        assertTrue(new NoteContainsKeywordsPredicate(List.of("internship")).test(alice));
        assertTrue(new NoteContainsKeywordsPredicate(List.of("COFFEE")).test(alice));
        assertTrue(new NoteContainsKeywordsPredicate(List.of("salary", "chat")).test(alice));
    }

    @Test
    public void test_noKeywordInAnyNote_returnsFalse() {
        assertFalse(new NoteContainsKeywordsPredicate(List.of("salary")).test(alice));
        assertFalse(new NoteContainsKeywordsPredicate(List.of("internship")).test(ALICE)); // no interactions
    }

    @Test
    public void test_keywordInNameOnly_returnsFalse() {
        assertFalse(new NoteContainsKeywordsPredicate(List.of("Alice")).test(alice));
    }

    @Test
    public void matchingInteractions_returnsOnlyMatchingNotes() {
        assertEquals(List.of(INTERNSHIP),
                new NoteContainsKeywordsPredicate(List.of("backend")).matchingInteractions(alice));
        assertEquals(List.of(INTERNSHIP, COFFEE),
                new NoteContainsKeywordsPredicate(List.of("backend", "coffee")).matchingInteractions(alice));
    }

    @Test
    public void firstMatchIndex() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("chat", "coffee"));
        assertEquals(0, predicate.firstMatchIndex("Coffee chat"));
        assertEquals(-1, predicate.firstMatchIndex("Lunch"));
    }

    @Test
    public void equals() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("coffee"));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new NoteContainsKeywordsPredicate(List.of("Coffee"))));
        assertEquals(predicate.hashCode(), new NoteContainsKeywordsPredicate(List.of("Coffee")).hashCode());

        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(1));
        assertFalse(predicate.equals(new NoteContainsKeywordsPredicate(List.of("tea"))));
    }

    @Test
    public void toStringMethod() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("Coffee"));
        assertEquals(NoteContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[coffee]}",
                predicate.toString());
    }
}
