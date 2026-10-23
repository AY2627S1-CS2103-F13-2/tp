package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
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
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ContactFileBuilder;
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
    public void execute_storageThrowsRuntimeException_restoresModel() {
        JsonContactFileStorage contactFileStorage =
                new JsonContactFileStorage(temporaryFolder.resolve("RuntimeExceptionContactFile.json")) {
                    @Override
                    public void saveContactFile(ReadOnlyContactFile contactFile) {
                        throw new IllegalStateException("dummy runtime exception");
                    }
                };
        StorageManager storage = new StorageManager(contactFileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("RuntimeExceptionUserPrefs.json")));
        logic = new LogicManager(model, storage);

        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + COMPANY_DESC_AMY + ROLE_DESC_AMY
                + PHONE_DESC_AMY + EMAIL_DESC_AMY + LINKEDIN_DESC_AMY;

        assertThrows(IllegalStateException.class, () -> logic.execute(addCommand));
        assertEquals(new ModelManager(), model);
    }

    @Test
    public void execute_dropConfirmationAcceptsYesAndNo() throws Exception {
        Person person = new PersonBuilder().withTags("outdated", "keep").build();
        model = new ModelManager(new ContactFileBuilder().withPerson(person).build(), new UserPrefs());
        AtomicInteger saveCount = new AtomicInteger();
        JsonContactFileStorage countingStorage = new JsonContactFileStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
                saveCount.incrementAndGet();
            }
        };
        logic = new LogicManager(model, new StorageManager(countingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        String preview = "Remove the following detail(s) from " + person.getName() + "?\n"
                + "- tag: outdated\nConfirm? (y/n; cancel to stop)";
        assertEquals(preview, logic.execute("drop 1 t/outdated").getFeedbackToUser());
        assertEquals("Request cancelled. No contacts changed.", logic.execute("n").getFeedbackToUser());

        assertEquals(0, saveCount.get());
        assertEquals(person, model.getContactFile().getPersonList().getFirst());

        assertEquals(preview, logic.execute("drop 1 t/outdated").getFeedbackToUser());
        logic.execute("y");
        assertEquals(1, saveCount.get());
        assertEquals(new PersonBuilder(person).withTags("keep").build(),
                model.getContactFile().getPersonList().getFirst());
    }

    @Test
    public void execute_introduceReplacementConfirmationAcceptsYesAndNo() throws Exception {
        Person person = new PersonBuilder().withName("Jane Doe").withIntroducer("Old Introducer").build();
        model = new ModelManager(new ContactFileBuilder().withPerson(person).build(), new UserPrefs());
        AtomicInteger saveCount = new AtomicInteger();
        JsonContactFileStorage countingStorage = new JsonContactFileStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
                saveCount.incrementAndGet();
            }
        };
        logic = new LogicManager(model, new StorageManager(countingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        String preview = "The existing introducer for Jane Doe is Old Introducer. Replace it with New Introducer?"
                + "\nConfirm? (y/n; cancel to stop)";
        assertEquals(preview, logic.execute("introduce 1 i/New Introducer").getFeedbackToUser());
        assertEquals("Request cancelled. No contacts changed.", logic.execute("n").getFeedbackToUser());
        assertEquals(person, model.getContactFile().getPersonList().getFirst());
        assertEquals(0, saveCount.get());

        assertEquals(preview, logic.execute("introduce 1 i/New Introducer").getFeedbackToUser());
        logic.execute("y");
        assertEquals(1, saveCount.get());
        assertEquals(new PersonBuilder(person).withIntroducer("New Introducer").build(),
                model.getContactFile().getPersonList().getFirst());
    }

    @Test
    public void execute_historyRecordsDataChangesAndOmitsViewCommands() throws Exception {
        logic.execute("add n/Alice Smith p/12345678 t/outdated");
        logic.execute("view 1");
        logic.execute("drop 1 t/outdated");
        assertFalse(logic.execute("history").getFeedbackToUser().contains("drop:"));
        logic.execute("drop 1 t/outdated --confirm");
        logic.execute("edit 1 p/87654321");
        logic.execute("introduce 1 i/Jane Doe");
        logic.execute("delete 1");
        logic.execute("add n/Bob Jones");
        logic.execute("clear");

        String history = logic.execute("history").getFeedbackToUser();
        assertTrue(history.contains("addition:"));
        assertTrue(history.contains("deletion:"));
        assertTrue(history.contains("drop:"));
        assertTrue(history.contains("clear:"));
        assertTrue(history.contains("edit:"));
        assertTrue(history.contains("introduce:"));
        assertFalse(history.contains("view:"));
    }

    @Test
    public void execute_undoEdit_requiresConfirmationAndRestoresContact() throws Exception {
        Person original = new PersonBuilder().withName("Alice Smith").withPhone("12345678")
                .withEmail(null).build();
        model = new ModelManager(new ContactFileBuilder().withPerson(original).build(), new UserPrefs());
        AtomicInteger saveCount = useCountingStorage();

        logic.execute("edit 1 p/87654321");
        Person edited = model.getContactFile().getPersonList().getFirst();
        assertEquals("87654321", edited.getPhone().orElseThrow().value);
        int savesAfterEdit = saveCount.get();

        CommandResult preview = logic.execute("undo");
        assertEquals(String.format(UndoManager.MESSAGE_CONFIRM_UNDO, "edit",
                "Updated Alice Smith's phone to 87654321."), preview.getFeedbackToUser());
        assertFalse(preview.shouldSaveContactFile());
        assertEquals(edited, model.getContactFile().getPersonList().getFirst());
        assertEquals(savesAfterEdit, saveCount.get());
        assertTrue(logic.execute("history").getFeedbackToUser().contains("edit:"));

        CommandResult undoResult = logic.execute("undo --confirm");
        assertEquals("Restored contact:\n" + Messages.format(original),
                undoResult.getFeedbackToUser());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());
        assertEquals(savesAfterEdit + 1, saveCount.get());
    }

    @Test
    public void execute_undoDeletion_restoresDeletedContact() throws Exception {
        Person original = new PersonBuilder().withName("Alice Smith").withPhone("12345678")
                .withEmail(null).build();
        model = new ModelManager(new ContactFileBuilder().withPerson(original).build(), new UserPrefs());
        AtomicInteger saveCount = useCountingStorage();

        logic.execute("delete 1");
        logic.execute("y");
        assertTrue(model.getContactFile().getPersonList().isEmpty());
        logic.execute("undo");

        CommandResult undoResult = logic.execute("undo --confirm");
        assertEquals("Restored contact:\n" + Messages.format(original),
                undoResult.getFeedbackToUser());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());
        assertEquals(2, saveCount.get());
    }

    @Test
    public void execute_undoWithoutSupportedChange_reportsNothingToUndo() throws Exception {
        AtomicInteger saveCount = useCountingStorage();

        CommandResult result = logic.execute("undo");

        assertEquals(UndoManager.MESSAGE_NOTHING_TO_UNDO, result.getFeedbackToUser());
        assertFalse(result.shouldSaveContactFile());
        assertTrue(model.getContactFile().getPersonList().isEmpty());
        assertEquals(0, saveCount.get());
    }

    @Test
    public void execute_undoAfterNonUndoableChange_reportsNothingToUndo() throws Exception {
        Person original = new PersonBuilder().withName("Alice Smith").withPhone("12345678")
                .withEmail(null).build();
        model = new ModelManager(new ContactFileBuilder().withPerson(original).build(), new UserPrefs());
        useCountingStorage();

        logic.execute("edit 1 p/87654321");
        logic.execute("add n/Bob Smith");
        CommandResult result = logic.execute("undo");

        assertEquals(UndoManager.MESSAGE_NOTHING_TO_UNDO, result.getFeedbackToUser());
        assertEquals(2, model.getContactFile().getPersonList().size());
        assertEquals("87654321", model.getContactFile().getPersonList().getFirst().getPhone().orElseThrow().value);
    }

    @Test
    public void execute_undoWouldViolateUniqueness_reportsConflictWithoutChangingData() throws Exception {
        Person original = new PersonBuilder().withName("Alice Smith").withPhone("12345678")
                .withEmail(null).build();
        model = new ModelManager(new ContactFileBuilder().withPerson(original).build(), new UserPrefs());
        AtomicInteger saveCount = useCountingStorage();

        logic.execute("edit 1 p/87654321");
        logic.execute("undo");
        Person conflictingContact = new PersonBuilder().withName("Bob Smith").withPhone("12345678")
                .withEmail(null).build();
        model.addPerson(conflictingContact);
        var stateBeforeConfirmation = List.copyOf(model.getContactFile().getPersonList());
        int savesBeforeConfirmation = saveCount.get();

        CommandResult result = logic.execute("undo --confirm");

        assertEquals(UndoManager.MESSAGE_UNDO_CONFLICT, result.getFeedbackToUser());
        assertFalse(result.shouldSaveContactFile());
        assertEquals(stateBeforeConfirmation, model.getContactFile().getPersonList());
        assertEquals(savesBeforeConfirmation, saveCount.get());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void setGuiSettings_updatesModel() {
        GuiSettings guiSettings = new GuiSettings(900, 700, 30, 40);

        logic.setGuiSettings(guiSettings);

        assertEquals(guiSettings, model.getGuiSettings());
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

    private AtomicInteger useCountingStorage() {
        AtomicInteger saveCount = new AtomicInteger();
        JsonContactFileStorage countingStorage = new JsonContactFileStorage(temporaryFolder.resolve("counted.json")) {
            @Override
            public void saveContactFile(ReadOnlyContactFile contactFile) {
                saveCount.incrementAndGet();
            }
        };
        logic = new LogicManager(model, new StorageManager(countingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("countedPrefs.json"))));
        return saveCount;
    }
}
