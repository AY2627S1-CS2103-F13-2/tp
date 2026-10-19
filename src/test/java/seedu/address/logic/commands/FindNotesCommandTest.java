package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.NoteContainsKeywordsPredicate;

/**
 * Contains integration tests (interaction with the Model) for {@code FindNotesCommand}.
 */
public class FindNotesCommandTest {

    private static final Interaction ALICE_FAIR = new Interaction(LocalDate.of(2026, 10, 1),
            "Met at the career fair, keen on internships");
    private static final Interaction ALICE_LUNCH = new Interaction(LocalDate.of(2026, 10, 8), "Lunch");
    private static final Interaction ALICE_CALL = new Interaction(LocalDate.of(2026, 10, 15),
            "Call about the Internship offer");
    private static final Interaction CARL_COFFEE = new Interaction(LocalDate.of(2026, 10, 2), "Coffee chat");

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    {
        model.setPerson(ALICE, ALICE.withInteraction(ALICE_FAIR).withInteraction(ALICE_LUNCH)
                .withInteraction(ALICE_CALL));
        model.setPerson(CARL, CARL.withInteraction(CARL_COFFEE));
    }

    @Test
    public void execute_matches_groupsNotesUnderEachContact() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("internship", "COFFEE"));
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.updateFilteredPersonList(predicate);

        String expectedMessage = "2 contact(s) with matching notes:\n"
                + "1. Alice Pauline\n"
                + "   2026-10-01: Met at the career fair, keen on internships\n"
                + "   2026-10-15: Call about the Internship offer\n"
                + "2. Carl Kurz\n"
                + "   2026-10-02: Coffee chat";

        assertCommandSuccess(new FindNotesCommand(predicate), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noMatch_reportsNoMatchingNotes() {
        NoteContainsKeywordsPredicate predicate = new NoteContainsKeywordsPredicate(List.of("salary"));
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());

        // the displayed list is left as it was
        assertCommandSuccess(new FindNotesCommand(predicate), model, FindNotesCommand.MESSAGE_NO_MATCHING_NOTES,
                expectedModel);
    }

    @Test
    public void excerpt_shortNote_unchanged() {
        FindNotesCommand command = new FindNotesCommand(new NoteContainsKeywordsPredicate(List.of("chat")));
        assertEquals("Coffee chat", command.excerpt("Coffee chat"));
    }

    @Test
    public void excerpt_longNote_cutAroundFirstMatch() {
        FindNotesCommand command = new FindNotesCommand(new NoteContainsKeywordsPredicate(List.of("salary")));
        String note = "We spent most of the afternoon talking about the team and its projects, "
                + "and then briefly about the expected salary range for new graduates in Singapore this year";

        String excerpt = command.excerpt(note);

        assertTrue(excerpt.startsWith("..."));
        assertTrue(excerpt.endsWith("..."));
        assertTrue(excerpt.contains("salary"));
        assertEquals(FindNotesCommand.MAX_EXCERPT_LENGTH + 6, excerpt.length());
    }

    @Test
    public void excerpt_matchNearStartOrEnd_cutOnOneSideOnly() {
        String note = "Salary chat first, then a long talk about the team, its projects and the office move";
        FindNotesCommand salary = new FindNotesCommand(new NoteContainsKeywordsPredicate(List.of("salary")));
        FindNotesCommand office = new FindNotesCommand(new NoteContainsKeywordsPredicate(List.of("move")));

        assertTrue(salary.excerpt(note).startsWith("Salary chat"));
        assertTrue(salary.excerpt(note).endsWith("..."));
        assertTrue(office.excerpt(note).startsWith("..."));
        assertTrue(office.excerpt(note).endsWith("office move"));
    }
}
