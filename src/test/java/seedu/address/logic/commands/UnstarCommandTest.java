package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UnstarCommandTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_validIndex_unstarsContactAndPreservesDetails() {
        for (Person original : List.of(ALICE, NAME_ONLY)) {
            Person starredPerson = new PersonBuilder(original).withStarred(true).build();
            Model testModel = new ModelManager();
            testModel.addPerson(starredPerson);
            Model expectedModel = new ModelManager();
            expectedModel.addPerson(original);

            assertCommandSuccess(new UnstarCommand(INDEX_FIRST_PERSON), testModel,
                    String.format(UnstarCommand.MESSAGE_SUCCESS, original.getName()), expectedModel);
        }
    }

    @Test
    public void execute_notStarred_reportsStatusWithoutReplacingContact() {
        Person original = model.getFilteredPersonList().getFirst();
        Model expectedModel = new ModelManager(model.getContactFile(), new UserPrefs());

        assertCommandSuccess(new UnstarCommand(INDEX_FIRST_PERSON), model,
                String.format(UnstarCommand.MESSAGE_NOT_STARRED, original.getName()), expectedModel);
        assertSame(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_unstarsDisplayedContactAndRetainsFilter() {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person starredPerson = new PersonBuilder(original).withStarred(true).build();
        model.setPerson(original, starredPerson);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getContactFile(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(starredPerson, original);

        assertCommandSuccess(new UnstarCommand(INDEX_FIRST_PERSON), model,
                String.format(UnstarCommand.MESSAGE_SUCCESS, original.getName()), expectedModel);
        assertEquals(List.of(original), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failureWithoutChanges() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new UnstarCommand(invalidIndex), model, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failureWithoutChanges() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new UnstarCommand(INDEX_SECOND_PERSON), model, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactFile_failureWithoutChanges() {
        assertCommandFailure(new UnstarCommand(INDEX_FIRST_PERSON), new ModelManager(),
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
