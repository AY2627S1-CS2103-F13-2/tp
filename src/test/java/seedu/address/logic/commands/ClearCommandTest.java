package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyContactFile_throwsCommandException() {
        Model model = new ModelManager();

        assertCommandFailure(new ClearCommand(), model, ClearCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void execute_nonEmptyContactFile_success() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalContactFile(), new UserPrefs());
        expectedModel.setContactFile(new ContactFile());

        String expectedMessage = String.format(ClearCommand.MESSAGE_SUCCESS, getTypicalPersons().size());

        assertCommandSuccess(new ClearCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        ClearCommand clearCommand = new ClearCommand();

        // same object -> returns true
        assertTrue(clearCommand.equals(clearCommand));

        // another ClearCommand -> returns true, as the command carries no state
        assertTrue(clearCommand.equals(new ClearCommand()));

        // different type -> returns false
        assertFalse(clearCommand.equals(new ListCommand()));

        // null -> returns false
        assertFalse(clearCommand.equals(null));
    }

}
