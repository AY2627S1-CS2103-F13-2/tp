package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Tests that clear previews all stored contacts and waits for confirmation.
 */
public class ClearCommandTest {

    @Test
    public void execute_emptyContactFile_throwsCommandException() {
        assertCommandFailure(new ClearCommand(), new ModelManager(), ClearCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void execute_nonEmptyContactFile_waitsForConfirmation() throws Exception {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        ContactFile original = new ContactFile(model.getContactFile());
        CommandResult preview = new ClearCommand().execute(model);
        assertEquals(original, model.getContactFile());
        PendingRequest request = preview.getPendingRequest().orElseThrow();
        assertTrue(request.getPrompt().contains("Clear all " + original.getPersonList().size()));
        assertTrue(request.respond("help", model).getPendingRequest().isPresent());
        assertEquals(original, model.getContactFile());
        CommandResult result = request.respond("y", model);
        assertTrue(model.getContactFile().getPersonList().isEmpty());
        assertEquals(String.format(ClearCommand.MESSAGE_SUCCESS, original.getPersonList().size()),
                result.getFeedbackToUser());
    }

    @Test
    public void equals() {
        ClearCommand command = new ClearCommand();
        assertEquals(command, new ClearCommand());
        assertFalse(command.equals(new ListCommand()));
        assertFalse(command.equals(null));
    }
}
