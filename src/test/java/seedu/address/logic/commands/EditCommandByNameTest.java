package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for editing a person by name with {@code EditCommand}.
 */
public class EditCommandByNameTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private final EditPersonDescriptor phoneBob = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();

    @Test
    public void execute_oneMatchIgnoringCase_success() {
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand("alice", phoneBob);

        String expectedMessage = "Updated Alice Pauline's phone to " + VALID_PHONE_BOB + ".";
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(ALICE, editedAlice);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_severalMatches_showsNumberedListWithoutEditing() {
        EditCommand editCommand = new EditCommand("Meier", phoneBob);

        String expectedMessage = "2 contacts found:\n"
                + "1. Benson Meier \u2014 GovTech \u2014 johnd@example.com\n"
                + "2. Daniel Meier \u2014 DBS \u2014 cornelia@example.com\n"
                + "To edit one of them, enter the command again with its index in place of the name.";
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Meier")));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_severalMatchesThenIndex_editsChosenPerson() throws Exception {
        new EditCommand("Meier", phoneBob).execute(model);
        Person danielMeier = model.getFilteredPersonList().get(1);

        new EditCommand(Index.fromOneBased(2), phoneBob).execute(model);

        Person editedDaniel = new PersonBuilder(danielMeier).withPhone(VALID_PHONE_BOB).build();
        assertTrue(model.getAddressBook().getPersonList().contains(editedDaniel));
    }

    @Test
    public void execute_noMatch_failure() {
        EditCommand editCommand = new EditCommand("Zed", phoneBob);

        assertCommandFailure(editCommand, model, String.format(EditCommand.MESSAGE_NAME_NOT_FOUND, "Zed"));
    }

    @Test
    public void equals() {
        EditCommand editAlice = new EditCommand("Alice", phoneBob);

        assertTrue(editAlice.equals(new EditCommand("Alice", phoneBob)));
        assertFalse(editAlice.equals(new EditCommand("Benson", phoneBob)));
        assertFalse(editAlice.equals(new EditCommand(INDEX_FIRST_PERSON, phoneBob)));
    }
}
