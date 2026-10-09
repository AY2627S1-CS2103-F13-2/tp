package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        Model model = new ModelManager();

        assertCommandFailure(new ClearCommand(), model, ClearCommand.MESSAGE_EMPTY_ADDRESS_BOOK);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

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
