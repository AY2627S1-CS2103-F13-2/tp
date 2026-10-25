package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class AliasCommandTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    private Model copyOf(Model source) {
        UserPrefs prefs = new UserPrefs(source.getUserPrefs());
        return new ModelManager(new ContactFile(source.getContactFile()), prefs);
    }

    @Test
    public void execute_newShortcut_saved() {
        Model expectedModel = copyOf(model);
        expectedModel.setCommandAlias("fn", "find /notes");

        assertCommandSuccess(AliasCommand.add("fn", "find /notes"), model,
                String.format(AliasCommand.MESSAGE_SAVED, "fn", "find /notes"), expectedModel);
    }

    @Test
    public void execute_existingShortcut_showsCurrentExpansionWithoutReplacing() {
        model.setCommandAlias("fn", "find /notes");

        String expectedMessage = "Shortcut fn already runs: find /notes\n"
                + "No changes were applied. To replace it, enter: alias /replace fn find /email";
        assertCommandFailure(AliasCommand.add("fn", "find /email"), model, expectedMessage);
        assertEquals("find /notes", model.getCommandAliases().get("fn"));
    }

    @Test
    public void execute_replaceExistingShortcut_replaced() {
        model.setCommandAlias("fn", "find /notes");
        Model expectedModel = copyOf(model);
        expectedModel.setCommandAlias("fn", "find /email");

        assertCommandSuccess(AliasCommand.replace("fn", "find /email"), model,
                String.format(AliasCommand.MESSAGE_REPLACED, "fn", "find /notes", "find /email"), expectedModel);
    }

    @Test
    public void execute_replaceMissingShortcut_saved() {
        Model expectedModel = copyOf(model);
        expectedModel.setCommandAlias("fn", "find /notes");

        assertCommandSuccess(AliasCommand.replace("fn", "find /notes"), model,
                String.format(AliasCommand.MESSAGE_SAVED, "fn", "find /notes"), expectedModel);
    }

    @Test
    public void execute_removeExistingShortcut_removed() {
        model.setCommandAlias("fn", "find /notes");
        Model expectedModel = copyOf(model);
        expectedModel.removeCommandAlias("fn");

        assertCommandSuccess(AliasCommand.remove("fn"), model,
                String.format(AliasCommand.MESSAGE_REMOVED, "fn", "find /notes"), expectedModel);
    }

    @Test
    public void execute_removeMissingShortcut_failure() {
        assertCommandFailure(AliasCommand.remove("fn"), model, String.format(AliasCommand.MESSAGE_NOT_FOUND, "fn"));
    }

    @Test
    public void execute_listWithoutShortcuts_saysNone() {
        assertCommandSuccess(AliasCommand.list(), model, AliasCommand.MESSAGE_NO_SHORTCUTS, copyOf(model));
    }

    @Test
    public void execute_listWithShortcuts_listsInAlphabeticalOrder() {
        model.setCommandAlias("fn", "find /notes");
        model.setCommandAlias("ce", "find /email");

        assertCommandSuccess(AliasCommand.list(), model,
                "Your shortcuts:\nce: find /email\nfn: find /notes", copyOf(model));
    }

    @Test
    public void equals() {
        AliasCommand addFn = AliasCommand.add("fn", "find /notes");

        assertTrue(addFn.equals(addFn));
        assertTrue(addFn.equals(AliasCommand.add("fn", "find /notes")));
        assertTrue(AliasCommand.list().equals(AliasCommand.list()));
        assertEquals(addFn.hashCode(), AliasCommand.add("fn", "find /notes").hashCode());

        assertFalse(addFn.equals(null));
        assertFalse(addFn.equals(1));
        assertFalse(addFn.equals(AliasCommand.replace("fn", "find /notes")));
        assertFalse(addFn.equals(AliasCommand.add("fe", "find /notes")));
        assertFalse(addFn.equals(AliasCommand.add("fn", "find /email")));
        assertFalse(AliasCommand.remove("fn").equals(AliasCommand.remove("fe")));
    }

    @Test
    public void toStringMethod() {
        String expected = AliasCommand.class.getCanonicalName()
                + "{action=ADD, shortcut=fn, expansion=find /notes}";
        assertEquals(expected, AliasCommand.add("fn", "find /notes").toString());
    }
}
