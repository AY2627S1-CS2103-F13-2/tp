package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class UndoManagerTest {

    @Test
    public void executeUndo_confirmationForStaleEntry_reportsNothingToUndo() throws Exception {
        UndoManager undoManager = new UndoManager();
        DeleteCommand deleteAlice = new DeleteCommand(Index.fromOneBased(1));
        undoManager.recordChange(deleteAlice, List.of(ALICE), List.of(), "Deleted Alice");
        undoManager.executeUndo(new ModelManager(), false);
        DeleteCommand deleteBenson = new DeleteCommand(Index.fromOneBased(1));
        undoManager.recordChange(deleteBenson, List.of(BENSON), List.of(), "Deleted Benson");

        assertEquals(UndoManager.MESSAGE_NOTHING_TO_UNDO,
                undoManager.executeUndo(new ModelManager(), true).getFeedbackToUser());
    }

    @Test
    public void executeUndo_missingRestoredPerson_reportsConflict() throws Exception {
        UndoManager undoManager = new UndoManager();
        List<Person> before = List.of(ALICE, BENSON);
        List<Person> after = List.of(BENSON, ALICE);
        undoManager.recordChange(new EditCommand(Index.fromOneBased(1), new EditCommand.EditPersonDescriptor()),
                before, after, "Reordered contacts");
        Model model = createModel(after);
        undoManager.executeUndo(model, false);

        assertEquals(UndoManager.MESSAGE_UNDO_CONFLICT,
                undoManager.executeUndo(model, true).getFeedbackToUser());
    }

    @Test
    public void executeUndo_replacementPersonMissing_reportsConflict() throws Exception {
        UndoManager undoManager = new UndoManager();
        undoManager.recordChange(new EditCommand(Index.fromOneBased(1), new EditCommand.EditPersonDescriptor()),
                List.of(ALICE), List.of(BENSON), "Replaced Alice");
        Model model = createModel(List.of(CARL));
        undoManager.executeUndo(model, false);

        assertEquals(UndoManager.MESSAGE_UNDO_CONFLICT,
                undoManager.executeUndo(model, true).getFeedbackToUser());
    }

    private Model createModel(List<Person> persons) {
        ContactFile contactFile = new ContactFile();
        contactFile.setPersons(persons);
        return new ModelManager(contactFile, new UserPrefs());
    }
}
