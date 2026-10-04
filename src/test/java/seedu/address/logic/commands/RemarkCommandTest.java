package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_setAndClearRemark_persistsChanges() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person expected = new PersonBuilder(original).withRemark("Likes to swim").build();
        CommandResult result = new AddressBookParser().parseCommand("remark 1 r/Likes to swim").execute(model);
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(expected), expected.getRemark()), result.getFeedbackToUser());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().get().getPersonList().get(0));

        result = new AddressBookParser().parseCommand("remark 1 r/").execute(model);
        expected = new PersonBuilder(original).withRemark("").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().get().getPersonList().get(0));
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        Person first = model.getFilteredPersonList().get(0);
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim")).execute(model);
        assertEquals(new PersonBuilder(second).withRemark("Likes to swim").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_failsWithoutChangingModel() {
        Index invalid = Index.fromZeroBased(model.getFilteredPersonList().size());
        assertCommandFailure(new RemarkCommand(invalid, new Remark("Test")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Test")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
