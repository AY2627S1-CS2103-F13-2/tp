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
 * Contains integration tests (interaction with the Model) and unit tests for {@code UnlogCommand}.
 */
public class UnlogCommandTest {

    private static final Interaction OCT_1 = new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat");
    private static final Interaction OCT_15 = new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat");
    private static final Person ALICE_WITH_HISTORY = ALICE.withInteractions(List.of(OCT_1, OCT_15));

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    private Model modelWithAliceHistory() {
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(ALICE, ALICE_WITH_HISTORY);
        return expectedModel;
    }

    @Test
    public void execute_byIndex_removesInteractionAndShowsRemainingHistory() {
        Model expectedModel = modelWithAliceHistory();
        expectedModel.setPerson(ALICE_WITH_HISTORY, ALICE.withInteractions(List.of(OCT_15)));
        model.setPerson(ALICE, ALICE_WITH_HISTORY);

        String expectedMessage = "Removed interaction 2026-10-01: Career fair chat from Alice Pauline.\n"
                + "Interaction history:\n"
                + "1. 2026-10-15: Coffee chat";
        assertCommandSuccess(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_lastInteractionByName_saysNoneLeft() throws Exception {
        model.setPerson(ALICE, ALICE.withInteraction(OCT_15));

        CommandResult result = new UnlogCommand("alice", Index.fromOneBased(1)).execute(model);

        assertEquals("Removed interaction 2026-10-15: Coffee chat from Alice Pauline.\n"
                + "Alice Pauline has no logged interactions left.", result.getFeedbackToUser());
        assertTrue(model.getContactFile().getPersonList().contains(ALICE));
    }

    @Test
    public void execute_entryOutOfRange_failure() {
        model.setPerson(ALICE, ALICE_WITH_HISTORY);

        assertCommandFailure(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3)), model,
                "There is no interaction 3. Alice Pauline has 2 logged interaction(s).");
    }

    @Test
    public void execute_noInteractions_failure() {
        assertCommandFailure(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)), model,
                String.format(UnlogCommand.MESSAGE_NO_INTERACTIONS, "Alice Pauline"));
    }

    @Test
    public void execute_invalidPersonIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new UnlogCommand(outOfBounds, Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_severalNameMatches_listsThemWithoutChanges() throws Exception {
        CommandResult result = new UnlogCommand("Meier", Index.fromOneBased(1)).execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("2 contacts found:"));
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Meier")));
        assertEquals(expectedModel, model);
    }

    @Test
    public void equals() {
        UnlogCommand first = new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1));

        assertTrue(first.equals(first));
        assertTrue(first.equals(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1))));
        assertTrue(new UnlogCommand("Alice", Index.fromOneBased(1))
                .equals(new UnlogCommand("Alice", Index.fromOneBased(1))));

        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
        assertFalse(first.equals(new UnlogCommand(INDEX_SECOND_PERSON, Index.fromOneBased(1))));
        assertFalse(first.equals(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2))));
        assertFalse(first.equals(new UnlogCommand("Alice", Index.fromOneBased(1))));
        assertFalse(new UnlogCommand("Alice", Index.fromOneBased(1))
                .equals(new UnlogCommand("Benson", Index.fromOneBased(1))));
    }

    @Test
    public void toStringMethod() {
        UnlogCommand command = new UnlogCommand("Alice", Index.fromOneBased(2));
        String expected = UnlogCommand.class.getCanonicalName() + "{index=null, targetName=Alice, entry="
                + Index.fromOneBased(2) + "}";
        assertEquals(expected, command.toString());
    }
}
