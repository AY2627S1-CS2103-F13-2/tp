package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.EditCommand.RemovableField;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for removing optional fields with {@code EditCommand}.
 */
public class EditCommandRemoveFieldTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_removeCompany_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.removeField(RemovableField.COMPANY);
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Person editedAlice = new PersonBuilder(ALICE).withCompany(null).build();
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(ALICE, editedAlice);

        assertCommandSuccess(editCommand, model, "Updated Alice Pauline's company to none.", expectedModel);
    }

    @Test
    public void execute_removeRoleAndPhone_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.removeField(RemovableField.ROLE);
        descriptor.removeField(RemovableField.PHONE);
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Person editedAlice = new PersonBuilder(ALICE).withRole(null).withPhone(null).build();
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(ALICE, editedAlice);

        String expectedMessage = "Updated Alice Pauline's role to none.\n"
                + "Updated Alice Pauline's phone to none.";
        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeAbsentField_noChanges() {
        Person aliceWithoutLinkedin = new PersonBuilder(ALICE).withLinkedin(null).build();
        model.setPerson(ALICE, aliceWithoutLinkedin);
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.removeField(RemovableField.LINKEDIN);

        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model, EditCommand.MESSAGE_NO_CHANGES);
    }

    @Test
    public void descriptor_removalCountsAsEditAndAffectsEquality() {
        EditPersonDescriptor removesEmail = new EditPersonDescriptor();
        removesEmail.removeField(RemovableField.EMAIL);

        assertTrue(removesEmail.isAnyFieldEdited());
        assertTrue(removesEmail.equals(new EditPersonDescriptor(removesEmail)));
        assertFalse(removesEmail.equals(new EditPersonDescriptor()));
    }
}
