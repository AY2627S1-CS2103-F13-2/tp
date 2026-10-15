package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) for deleting a person by name with
 * {@code DeleteCommand}.
 */
public class DeleteCommandByNameTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_fullName_success() {
        assertDeletes(new DeleteCommand("Alice Pauline"), ALICE);
    }

    @Test
    public void execute_partialNameMatchingOnePerson_success() {
        assertDeletes(new DeleteCommand("Pauline"), ALICE);
    }

    @Test
    public void execute_nameInDifferentCase_success() {
        assertDeletes(new DeleteCommand("alice pauline"), ALICE);
    }

    @Test
    public void execute_nameHiddenByFilter_success() {
        // Carl is not on display, but naming him in full still deletes him.
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        assertDeletes(new DeleteCommand("Carl Kurz"), CARL);
    }

    @Test
    public void execute_severalMatches_showsNumberedListWithoutDeleting() {
        DeleteCommand deleteCommand = new DeleteCommand("Meier");

        String expectedMessage = "2 contacts found:\n"
                + "1. Benson Meier \u2014 GovTech \u2014 johnd@example.com\n"
                + "2. Daniel Meier \u2014 DBS \u2014 cornelia@example.com\n"
                + "To delete one of them, enter the command again with its index in place of the name.";
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Meier")));

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_severalMatchesThenIndex_deletesChosenPerson() throws Exception {
        new DeleteCommand("Meier").execute(model);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());

        new DeleteCommand(Index.fromOneBased(2)).execute(model);

        assertFalse(model.getContactFile().getPersonList().contains(DANIEL));
        assertTrue(model.getContactFile().getPersonList().contains(BENSON));
    }

    @Test
    public void execute_noMatch_failure() {
        DeleteCommand deleteCommand = new DeleteCommand("Zed");

        assertCommandFailure(deleteCommand, model,
                String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed"));
    }

    @Test
    public void execute_emptyContactFile_failure() {
        Model emptyModel = new ModelManager(new ContactFile(), new UserPrefs());

        assertCommandFailure(new DeleteCommand("Alice Pauline"), emptyModel,
                DeleteCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void execute_byName_showsEveryRemainingContactAfterwards() throws Exception {
        new DeleteCommand("Alice Pauline").execute(model);

        // Resolving the name narrowed the list to Alice alone; once she is gone the full list is back.
        assertEquals(model.getContactFile().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        DeleteCommand deleteAlice = new DeleteCommand("Alice");

        assertTrue(deleteAlice.equals(new DeleteCommand("Alice")));
        assertFalse(deleteAlice.equals(new DeleteCommand("Benson")));
        assertFalse(deleteAlice.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(deleteAlice.equals(null));
        assertFalse(deleteAlice.equals(1));
    }

    @Test
    public void toStringMethod() {
        DeleteCommand deleteCommand = new DeleteCommand("Alice");
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=null, targetName=Alice}";

        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Asserts that {@code deleteCommand} removes {@code personToDelete} and reports it, leaving every
     * remaining contact on display.
     */
    private void assertDeletes(DeleteCommand deleteCommand, Person personToDelete) {
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }
}
