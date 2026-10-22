package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Tests name resolution, candidate selection, and confirmation before deletion.
 */
public class DeleteCommandByNameTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_fullName_confirmsCorrectPerson() throws Exception {
        assertConfirmedDeletion("Alice Pauline", ALICE);
    }

    @Test
    public void execute_partialName_confirmsCorrectPerson() throws Exception {
        assertConfirmedDeletion("Pauline", ALICE);
    }

    @Test
    public void execute_differentCase_confirmsCorrectPerson() throws Exception {
        assertConfirmedDeletion("alice pauline", ALICE);
    }

    @Test
    public void execute_nameHiddenByFilter_searchesAllContacts() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertConfirmedDeletion("Carl Kurz", CARL);
    }

    @Test
    public void execute_severalMatches_requiresSelectionThenConfirmation() throws Exception {
        ContactFile original = new ContactFile(model.getContactFile());
        CommandResult selection = new DeleteCommand("Meier").execute(model);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
        assertEquals(original, model.getContactFile());
        assertTrue(selection.getFeedbackToUser().contains("2. Daniel Meier"));

        PendingRequest request = selection.getPendingRequest().orElseThrow();
        CommandResult invalid = request.respond("99", model);
        assertEquals(original, model.getContactFile());
        assertTrue(invalid.getPendingRequest().isPresent());

        CommandResult confirmation = request.respond("2", model);
        assertEquals(original, model.getContactFile());
        assertTrue(confirmation.getFeedbackToUser().contains(Messages.format(DANIEL)));
        confirmation.getPendingRequest().orElseThrow().respond("y", model);
        assertFalse(model.getContactFile().getPersonList().contains(DANIEL));
        assertTrue(model.getContactFile().getPersonList().contains(BENSON));
        assertEquals(model.getContactFile().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatch_failure() {
        assertCommandFailure(new DeleteCommand("Zed"), model,
                String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed"));
    }

    @Test
    public void execute_emptyContactFile_failure() {
        Model emptyModel = new ModelManager(new ContactFile(), new UserPrefs());
        assertCommandFailure(new DeleteCommand("Alice Pauline"), emptyModel,
                DeleteCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void equals() {
        DeleteCommand command = new DeleteCommand("Alice");
        assertEquals(command, new DeleteCommand("Alice"));
        assertFalse(command.equals(new DeleteCommand("Benson")));
        assertFalse(command.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(command.equals(null));
    }

    @Test
    public void toStringMethod() {
        assertEquals(DeleteCommand.class.getCanonicalName()
                + "{targetIndex=null, targetName=Alice, targetPhone=null, targetEmails=[]}",
                new DeleteCommand("Alice").toString());
    }

    private void assertConfirmedDeletion(String name, Person target) throws Exception {
        ContactFile original = new ContactFile(model.getContactFile());
        CommandResult preview = new DeleteCommand(name).execute(model);
        assertEquals(original, model.getContactFile());
        preview.getPendingRequest().orElseThrow().respond("y", model);
        assertFalse(model.getContactFile().getPersonList().contains(target));
        assertEquals(model.getContactFile().getPersonList(), model.getFilteredPersonList());
    }
}
