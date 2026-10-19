package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LogCommand}.
 */
public class LogCommandTest {

    private static final Interaction OCT_1 = new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat");
    private static final Interaction OCT_15 = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_byIndex_addsInteractionAndShowsHistory() {
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, OCT_15);

        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(ALICE, ALICE.withInteraction(OCT_15));
        String expectedMessage = "Logged an interaction with Alice Pauline on 2026-10-15.\n"
                + "Interaction history:\n"
                + "1. 2026-10-15: Coffee chat";

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_olderInteraction_historyShownInDateOrder() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, OCT_15).execute(model);

        CommandResult result = new LogCommand("alice", OCT_1).execute(model);

        assertEquals("Logged an interaction with Alice Pauline on 2026-10-01.\n"
                + "Interaction history:\n"
                + "1. 2026-10-01: Career fair chat\n"
                + "2. 2026-10-15: Coffee chat", result.getFeedbackToUser());
    }

    @Test
    public void execute_byName_success() {
        LogCommand logCommand = new LogCommand("alice pauline", OCT_15);

        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(ALICE, ALICE.withInteraction(OCT_15));

        assertCommandSuccess(logCommand, model, String.format(LogCommand.MESSAGE_SUCCESS, "Alice Pauline",
                "2026-10-15", "1. 2026-10-15: Coffee chat"), expectedModel);
    }

    @Test
    public void execute_severalNameMatches_listsMatchesWithoutLogging() throws Exception {
        CommandResult result = new LogCommand("Meier", OCT_15).execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("2 contacts found:"));
        for (Person person : model.getContactFile().getPersonList()) {
            assertTrue(person.getInteractions().isEmpty());
        }
        assertTrue(new NameContainsKeywordsPredicate(List.of("Meier")).test(model.getFilteredPersonList().get(0)));
    }

    @Test
    public void execute_noNameMatch_failure() {
        assertCommandFailure(new LogCommand("Zed", OCT_15), model,
                String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed"));
    }

    @Test
    public void execute_sameInteractionTwice_failure() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, OCT_15).execute(model);

        Interaction sameInDifferentCase = new Interaction(OCT_15.date, "coffee CHAT");
        assertCommandFailure(new LogCommand(INDEX_FIRST_PERSON, sameInDifferentCase), model,
                String.format(LogCommand.MESSAGE_DUPLICATE_INTERACTION, "Alice Pauline"));
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new LogCommand(outOfBounds, OCT_15), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        LogCommand logFirst = new LogCommand(INDEX_FIRST_PERSON, OCT_15);

        assertTrue(logFirst.equals(logFirst));
        assertTrue(logFirst.equals(new LogCommand(INDEX_FIRST_PERSON, OCT_15)));

        assertFalse(logFirst.equals(null));
        assertFalse(logFirst.equals(new ClearCommand()));
        assertFalse(logFirst.equals(new LogCommand(INDEX_SECOND_PERSON, OCT_15)));
        assertFalse(logFirst.equals(new LogCommand(INDEX_FIRST_PERSON, OCT_1)));
        assertFalse(logFirst.equals(new LogCommand("Alice", OCT_15)));
        assertFalse(new LogCommand("Alice", OCT_15).equals(new LogCommand("Benson", OCT_15)));
    }

    @Test
    public void toStringMethod() {
        LogCommand logCommand = new LogCommand("Alice", OCT_15);
        String expected = LogCommand.class.getCanonicalName() + "{index=null, targetName=Alice, interaction="
                + OCT_15 + "}";
        assertEquals(expected, logCommand.toString());
    }
}
