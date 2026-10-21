package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Contains integration tests for {@code ViewCommand}.
 */
public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_validIndex_success() {
        Index firstIndex = Index.fromOneBased(1);
        Person personToView = model.getFilteredPersonList().get(firstIndex.getZeroBased());

        ViewCommand command = new ViewCommand(firstIndex);

        assertCommandSuccess(command, model,
                CommandResult.createPersonDetails(Messages.format(personToView), personToView), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ViewCommand command = new ViewCommand(invalidIndex);

        assertCommandFailure(command, model, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        Index secondIndex = Index.fromOneBased(2);

        ViewCommand firstCommand = new ViewCommand(firstIndex);
        ViewCommand secondCommand = new ViewCommand(secondIndex);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(new ViewCommand(firstIndex)));
        assertFalse(firstCommand.equals(secondCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(1));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        ViewCommand command = new ViewCommand(index);

        String expected = ViewCommand.class.getCanonicalName()
            + "{targetIndex=" + index + "}";
        assertEquals(expected, command.toString());
    }
}
