package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

public class RecordCommandTest {

    @Test
    public void execute_validIntroducerNotInContactFile_recordsAndDisplaysContact() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        Person target = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(target).withIntroducer("Someone Outside Astra").build();
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        RecordCommand command = new RecordCommand(Index.fromOneBased(1),
                new Name("Someone Outside Astra"), false);

        assertCommandSuccess(command, model, String.format(RecordCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_existingIntroducer_requestsConfirmationWithoutChangingContact() {
        Person original = new PersonBuilder().withIntroducer("Old Introducer").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();
        Model model = new ModelManager(contactFile, new UserPrefs());

        RecordCommand command = new RecordCommand(Index.fromOneBased(1), new Name("New Introducer"), false);

        String expectedMessage = String.format(RecordCommand.MESSAGE_CONFIRM_REPLACEMENT,
                original.getName(), original.getIntroducer().orElseThrow(), "New Introducer", 1);
        assertCommandSuccess(command, model, expectedMessage,
                new ModelManager(new ContactFile(contactFile), new UserPrefs()));
    }

    @Test
    public void execute_confirmedReplacement_replacesAndDisplaysContact() {
        Person original = new PersonBuilder().withIntroducer("Old Introducer").build();
        Person updated = new PersonBuilder(original).withIntroducer("New Introducer").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        Model expectedModel = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        expectedModel.setPerson(original, updated);

        RecordCommand command = new RecordCommand(Index.fromOneBased(1), new Name("New Introducer"), true);

        assertCommandSuccess(command, model, String.format(RecordCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        RecordCommand command = new RecordCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                new Name("Jane Doe"), false);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
