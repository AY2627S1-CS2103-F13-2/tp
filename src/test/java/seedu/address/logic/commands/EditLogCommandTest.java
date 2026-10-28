package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code EditLogCommand}.
 */
public class EditLogCommandTest {

    private static final Interaction OCT_1 = new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat");
    private static final Interaction OCT_15 = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
    private Person alice;

    @BeforeEach
    public void setUp() {
        alice = ALICE.withInteractions(List.of(OCT_1, OCT_15));
        model.setPerson(ALICE, alice);
    }

    private Model expectedModelWith(Interaction... interactions) {
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(alice, alice.withInteractions(List.of(interactions)));
        return expectedModel;
    }

    @Test
    public void execute_noteOnly_keepsDate() {
        Interaction edited = new Interaction(OCT_1.getDate(), "Career fair chat about internships");
        assertCommandSuccess(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null,
                "Career fair chat about internships"), model,
                String.format(EditLogCommand.MESSAGE_SUCCESS, 1, "Alice Pauline", edited,
                        "1. 2026-10-01: Career fair chat about internships\n2. 2026-10-15: Coffee chat"),
                expectedModelWith(edited, OCT_15));
    }

    @Test
    public void execute_newDate_movesEntryAndReportsNewNumber() {
        Interaction edited = new Interaction(LocalDate.of(2026, 10, 20), "Career fair chat");
        assertCommandSuccess(
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, LocalDate.of(2026, 10, 20), null),
                model, String.format(EditLogCommand.MESSAGE_SUCCESS, 2, "Alice Pauline", edited,
                        "1. 2026-10-15: Coffee chat\n2. 2026-10-20: Career fair chat"),
                expectedModelWith(OCT_15, edited));
    }

    @Test
    public void execute_byName_success() throws Exception {
        new EditLogCommand("alice", INDEX_SECOND_PERSON, null, "Coffee chat at the library").execute(model);
        assertEquals(new Interaction(OCT_15.getDate(), "Coffee chat at the library"),
                model.getContactFile().getPersonList().getFirst().getInteractions().get(1));
    }

    @Test
    public void execute_nameMatchesSeveral_listsMatchesWithoutChange() throws Exception {
        CommandResult result = new EditLogCommand("meier", INDEX_FIRST_PERSON, null, "x").execute(model);
        assertTrue(result.getFeedbackToUser().startsWith("2 contacts found:"));
        assertEquals(List.of(OCT_1, OCT_15), model.getContactFile().getPersonList().getFirst().getInteractions());
    }

    @Test
    public void execute_letterCaseFix_success() throws Exception {
        CommandResult result = new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, null, "coffee CHAT")
                .execute(model);
        assertTrue(result.getFeedbackToUser().startsWith("Edited interaction 2 of Alice Pauline: "
                + "2026-10-15: coffee CHAT"));
    }

    @Test
    public void execute_unchanged_failure() {
        assertCommandFailure(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, null, "  Coffee   chat "),
                model, String.format(EditLogCommand.MESSAGE_NOT_CHANGED, 2, "Alice Pauline", OCT_15));
    }

    @Test
    public void execute_duplicateOfOtherEntry_failure() {
        assertCommandFailure(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                LocalDate.of(2026, 10, 15), "COFFEE chat"), model,
                String.format(EditLogCommand.MESSAGE_DUPLICATE_INTERACTION, "Alice Pauline", OCT_15));
    }

    @Test
    public void execute_entryOutOfRange_failure() {
        assertCommandFailure(new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), null, "x"), model,
                String.format(UnlogCommand.MESSAGE_INVALID_ENTRY, 3, "Alice Pauline", 2));
    }

    @Test
    public void execute_noInteractions_failure() {
        assertCommandFailure(new EditLogCommand(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON, null, "x"), model,
                String.format(EditLogCommand.MESSAGE_NO_INTERACTIONS, "Benson Meier"));
    }

    @Test
    public void constructor_nothingToChange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, " "));
    }

    @Test
    public void equals() {
        EditLogCommand byIndex = new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, "note");
        assertTrue(byIndex.equals(byIndex));
        assertTrue(byIndex.equals(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, "note")));
        assertFalse(byIndex.equals(new EditLogCommand("alice", INDEX_FIRST_PERSON, null, "note")));
        assertFalse(byIndex.equals(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, null, "note")));
        assertFalse(byIndex.equals(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                LocalDate.of(2026, 10, 1), "note")));
        assertFalse(byIndex.equals(new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, "other")));
        assertFalse(byIndex.equals(null));
        EditLogCommand byName = new EditLogCommand("alice", INDEX_FIRST_PERSON, null, "note");
        assertTrue(byName.equals(new EditLogCommand("alice", INDEX_FIRST_PERSON, null, "note")));
        assertFalse(byName.equals(new EditLogCommand("benson", INDEX_FIRST_PERSON, null, "note")));
        assertEquals(byIndex.toString(),
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, null, "note").toString());
    }
}
