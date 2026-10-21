package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Tests deletion previews, stable displayed-index resolution, and confirmed model changes.
 */
public class DeleteCommandTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_validIndex_waitsForConfirmation() throws Exception {
        ContactFile original = new ContactFile(model.getContactFile());
        Person target = model.getFilteredPersonList().get(0);
        CommandResult preview = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertEquals(original, model.getContactFile());
        assertTrue(preview.getFeedbackToUser().contains(Messages.format(target)));

        CommandResult result = preview.getPendingRequest().orElseThrow().respond("y", model);
        assertFalse(model.getContactFile().getPersonList().contains(target));
        assertTrue(result.getFeedbackToUser().contains("contact(s) remaining."));
        assertTrue(result.getPendingRequest().isEmpty());
    }

    @Test
    public void execute_filteredIndex_confirmsDisplayedTargetAndResetsList() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person target = model.getFilteredPersonList().get(0);
        CommandResult preview = new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertTrue(model.getContactFile().getPersonList().contains(target));
        preview.getPendingRequest().orElseThrow().respond("y", model);
        assertFalse(model.getContactFile().getPersonList().contains(target));
        assertEquals(model.getContactFile().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalid = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new DeleteCommand(invalid), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new DeleteCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactFile_throwsCommandException() {
        Model emptyModel = new ModelManager(new ContactFile(), new UserPrefs());
        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), emptyModel,
                DeleteCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void equals() {
        DeleteCommand first = new DeleteCommand(INDEX_FIRST_PERSON);
        assertEquals(first, new DeleteCommand(INDEX_FIRST_PERSON));
        assertFalse(first.equals(new DeleteCommand(INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    @Test
    public void toStringMethod() {
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON);
        assertEquals(DeleteCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", targetName=null, targetPhone=null, targetEmails=[]}",
                command.toString());
    }
}
