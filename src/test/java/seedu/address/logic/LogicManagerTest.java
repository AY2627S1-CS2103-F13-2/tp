package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.COMPANY_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LINKEDIN_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.StarCommand;
import seedu.address.logic.commands.UnstarCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonContactFileStorage contactFileStorage =
                new JsonContactFileStorage(temporaryFolder.resolve("contactFile.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(contactFileStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        // The model starts empty, so delete reports that there is nothing to delete rather than
        // complaining about the index. The point of the test is that the exception reaches the caller.
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, DeleteCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_shortcut_savedToPrefsFileAndExpanded() throws Exception {
        Path prefsPath = temporaryFolder.resolve("userPrefs.json");
        logic.execute("alias ls list");

        UserPrefs savedPrefs = new JsonUserPrefsStorage(prefsPath).readUserPrefs().get();
        assertEquals(Map.of("ls", "list"), savedPrefs.getCommandAliases());
        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("LS").getFeedbackToUser());
    }

    @Test
    public void execute_shortcut_contactFileNotWritten() throws Exception {
        logic.execute("alias ls list");
        assertFalse(temporaryFolder.resolve("contactFile.json").toFile().exists());
    }

    @Test
    public void execute_shortcutPrefsSaveFails_shortcutNotKept() {
        JsonUserPrefsStorage failingPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")) {
                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                        throw DUMMY_IO_EXCEPTION;
                    }
                };
        logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(temporaryFolder.resolve("contactFile.json")), failingPrefsStorage));

        assertThrows(CommandException.class, () -> logic.execute("alias ls list"));
        assertEquals(Map.of(), model.getCommandAliases());
    }

    @Test
    public void constructor_invalidShortcutsInPrefs_droppedAndNormalised() throws Exception {
        Map<String, String> loaded = new HashMap<>();
        loaded.put("FN", "find /notes");
        loaded.put("list", "clear");
        loaded.put("bad", "search x");
        loaded.put("empty", null);
        model.setCommandAliases(loaded);

        logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(temporaryFolder.resolve("contactFile.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        assertEquals(Map.of("fn", "find /notes"), model.getCommandAliases());
        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
    }

    @Test
    public void execute_commandWithoutShortcutChange_prefsFileNotWritten() throws Exception {
        logic.execute(ListCommand.COMMAND_WORD);
        assertFalse(temporaryFolder.resolve("userPrefs.json").toFile().exists());
    }

    @Test
    public void execute_starAndUnstar_persistsStatusThroughSaveFlow() throws Exception {
        model.addPerson(AMY);
        JsonContactFileStorage savedContacts =
                new JsonContactFileStorage(temporaryFolder.resolve("contactFile.json"));
        Person starredAmy = new PersonBuilder(AMY).withStarred(true).build();

        assertEquals(String.format(StarCommand.MESSAGE_SUCCESS, AMY.getName()),
                logic.execute("star 1").getFeedbackToUser());
        assertEquals(starredAmy, model.getFilteredPersonList().getFirst());
        assertEquals(starredAmy, savedContacts.readContactFile().orElseThrow().getPersonList().getFirst());

        assertEquals(String.format(UnstarCommand.MESSAGE_SUCCESS, AMY.getName()),
                logic.execute("unstar 1").getFeedbackToUser());
        assertEquals(AMY, model.getFilteredPersonList().getFirst());
        assertEquals(AMY, savedContacts.readContactFile().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getContactFile(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonContactFileStorage that throws the IOException e when saving
        JsonContactFileStorage contactFileStorage = new JsonContactFileStorage(prefPath) {
            @Override
            public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(contactFileStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveContactFile method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + COMPANY_DESC_AMY + ROLE_DESC_AMY
                + PHONE_DESC_AMY + EMAIL_DESC_AMY + LINKEDIN_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
