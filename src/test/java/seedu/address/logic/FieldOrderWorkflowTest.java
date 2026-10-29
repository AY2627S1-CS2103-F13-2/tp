package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FieldOrder;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class FieldOrderWorkflowTest {
    private static final FieldOrder CUSTOM = new FieldOrder(
            List.of("email", "phone", "introducer", "name", "tags", "company", "role", "linkedin", "followups",
                    "interactions"));

    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private CountingContacts contacts;
    private CountingPreferences prefs;
    private Path contactFile;
    private Path prefsFile;
    private byte[] contactBytes;
    private byte[] prefsBytes;

    @BeforeEach
    void setUp() throws Exception {
        model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        contactFile = testFolder.resolve("contacts.json");
        prefsFile = testFolder.resolve("preferences.json");
        new JsonContactFileStorage(contactFile).saveContactFile(model.getContactFile());
        new JsonUserPrefsStorage(prefsFile).saveUserPrefs(model.getUserPrefs());
        contactBytes = Files.readAllBytes(contactFile);
        prefsBytes = Files.readAllBytes(prefsFile);
        contacts = new CountingContacts(contactFile);
        prefs = new CountingPreferences(prefsFile);
        logic = new LogicManager(model, new StorageManager(contacts, prefs));
    }

    @Test
    void execute_queryAndSameOrder_doNotSaveOrRefresh() throws Exception {
        List<FieldOrder> changes = new ArrayList<>();
        logic.getFieldOrderProperty().addListener((observable, previous, current) -> changes.add(current));
        assertTrue(logic.execute("order").getFeedbackToUser().contains(FieldOrder.DEFAULT.toString()));
        assertTrue(logic.execute("order " + FieldOrder.DEFAULT).getFeedbackToUser().contains("unchanged"));
        assertEquals(0, prefs.saveCount);
        assertEquals(0, contacts.saveCount);
        assertTrue(changes.isEmpty());
        assertArrayEquals(prefsBytes, Files.readAllBytes(prefsFile));
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
    }

    @Test
    void execute_changedOrder_savesPreferencesOnceAndPreservesDataFilterAndBinding() throws Exception {
        logic.execute("find Alice");
        ObservableList<Person> binding = model.getFilteredPersonList();
        model.setGuiSettings(new GuiSettings(1000, 800, 10, 20));
        logic.execute("order " + CUSTOM);
        assertEquals(CUSTOM, model.getFieldOrder());
        assertEquals(CUSTOM, logic.getFieldOrderProperty().get());
        assertEquals(model.getUserPrefs(), prefs.readUserPrefs().orElseThrow());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertSame(binding, model.getFilteredPersonList());
        assertEquals(getTypicalContactFile(), model.getContactFile());
        assertEquals(1, prefs.saveCount);
        assertEquals(0, contacts.saveCount);
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
        assertEquals(Messages.format(ALICE, CUSTOM), logic.execute("view 1").getFeedbackToUser());
        assertTrue(logic.execute("order " + CUSTOM).getFeedbackToUser().contains("unchanged"));
        assertEquals(1, prefs.saveCount);
    }

    @Test
    void execute_invalidOrder_preservesPreferencesFileAndPriorView() throws Exception {
        logic.execute("find Alice");
        for (String input : List.of("order name", "order " + FieldOrder.DEFAULT + " extra",
                "order name tags company role phone email linkedin name",
                "order name tags company role phone email linkedin unknown")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(FieldOrder.DEFAULT, model.getFieldOrder());
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
        }
        assertEquals(0, prefs.saveCount);
        assertEquals(0, contacts.saveCount);
        assertArrayEquals(prefsBytes, Files.readAllBytes(prefsFile));
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
    }

    @Test
    void execute_preferenceFailure_restoresSavedOrderAndNotifiesViewsOfRollback() throws Exception {
        logic.execute("order " + CUSTOM);
        byte[] saved = Files.readAllBytes(prefsFile);
        logic.execute("find Alice");
        ObservableList<Person> binding = model.getFilteredPersonList();
        List<FieldOrder> changes = new ArrayList<>();
        logic.getFieldOrderProperty().addListener((observable, previous, current) -> changes.add(current));
        prefs.failure = new IOException("Simulated preference save failure");
        assertThrows(CommandException.class, () -> logic.execute("order " + FieldOrder.DEFAULT));
        assertEquals(List.of(FieldOrder.DEFAULT, CUSTOM), changes);
        assertEquals(CUSTOM, model.getFieldOrder());
        assertEquals(CUSTOM, model.getUserPrefs().getFieldOrder());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertSame(binding, model.getFilteredPersonList());
        assertArrayEquals(saved, Files.readAllBytes(prefsFile));
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
        assertEquals(0, contacts.saveCount);
        assertEquals(2, prefs.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    void execute_permissionFailure_keepsModelAndMissingPreferenceFileUnchanged() throws Exception {
        Files.delete(prefsFile);
        prefs.failure = new AccessDeniedException(prefsFile.toString());
        assertThrows(CommandException.class, () -> logic.execute("order " + CUSTOM));
        assertEquals(FieldOrder.DEFAULT, model.getFieldOrder());
        assertFalse(Files.exists(prefsFile));
        assertEquals(0, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
    }

    @Test
    void execute_restart_loadsSavedOrderAndUsesItForDetails() throws Exception {
        logic.execute("order " + CUSTOM);
        ModelManager reloaded = new ModelManager(contacts.readContactFile().orElseThrow(),
                prefs.readUserPrefs().orElseThrow());
        LogicManager restarted = new LogicManager(reloaded, new StorageManager(contacts, prefs));
        assertEquals(CUSTOM, restarted.getFieldOrderProperty().get());
        assertEquals(Messages.format(ALICE, CUSTOM), restarted.execute("view 1").getFeedbackToUser());
        assertTrue(restarted.execute("order").getFeedbackToUser().contains(CUSTOM.toString()));
        assertEquals(1, prefs.saveCount);
        assertEquals(0, contacts.saveCount);
    }

    @Test
    void execute_contactMutation_routesOnlyToContactStorage() throws Exception {
        logic.execute("order " + CUSTOM);
        byte[] savedPrefs = Files.readAllBytes(prefsFile);
        logic.execute("followup 1 a/Send notes");
        assertEquals(1, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
        assertArrayEquals(savedPrefs, Files.readAllBytes(prefsFile));
        assertEquals(CUSTOM, model.getFieldOrder());
        contacts.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("followup 1 a/Another action"));
        assertEquals(CUSTOM, model.getFieldOrder());
        assertEquals(1, model.getFilteredPersonList().getFirst().getFollowUps().size());
        assertEquals(1, prefs.saveCount);
    }

    @Test
    void execute_orderWhilePending_doesNotRunOrSavePreferences() throws Exception {
        logic.execute("clear");
        assertTrue(logic.execute("order " + CUSTOM).getPendingRequest().isPresent());
        assertEquals(FieldOrder.DEFAULT, model.getFieldOrder());
        logic.execute("cancel");
        assertEquals(0, prefs.saveCount);
        assertEquals(0, contacts.saveCount);
        assertArrayEquals(prefsBytes, Files.readAllBytes(prefsFile));
    }

    @Test
    void execute_filteredDelete_usesSavedOrderForPreviewAndResult() throws Exception {
        logic.execute("order " + CUSTOM);
        logic.execute("filter t/friends");
        String details = Messages.format(ALICE, CUSTOM);
        assertTrue(logic.execute("delete 1").getFeedbackToUser().contains(details));
        assertEquals(0, contacts.saveCount);
        assertTrue(logic.execute("y").getFeedbackToUser().contains(details));
        assertFalse(model.getContactFile().getPersonList().contains(ALICE));
        assertEquals(model.getContactFile().getPersonList(), model.getFilteredPersonList());
        assertEquals(CUSTOM, model.getFieldOrder());
        assertEquals(1, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
    }

    @Test
    void execute_filteredDeleteFailure_restoresTagFilterAndSavedOrder() throws Exception {
        logic.execute("order " + CUSTOM);
        logic.execute("filter t/friends");
        List<Person> filtered = List.copyOf(model.getFilteredPersonList());
        ObservableList<Person> binding = model.getFilteredPersonList();
        byte[] savedPrefs = Files.readAllBytes(prefsFile);
        logic.execute("delete 1");
        contacts.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertEquals(filtered, model.getFilteredPersonList());
        assertSame(binding, model.getFilteredPersonList());
        assertEquals(getTypicalContactFile(), model.getContactFile());
        assertEquals(CUSTOM, model.getFieldOrder());
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
        assertArrayEquals(savedPrefs, Files.readAllBytes(prefsFile));
        assertEquals(1, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    void execute_hiddenNameWithActions_usesSavedOrderWithoutQueryWrites() throws Exception {
        logic.execute("order " + CUSTOM);
        logic.execute("followup 1 a/Send portfolio");
        Person updated = model.getContactFile().getPersonList().getFirst();
        byte[] savedContacts = Files.readAllBytes(contactFile);
        byte[] savedPrefs = Files.readAllBytes(prefsFile);
        logic.execute("filter t/owesMoney");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        CommandResult result = logic.execute("view pAuLiNe");
        assertEquals(Messages.format(updated, CUSTOM), result.getFeedbackToUser());
        assertTrue(result.getFeedbackToUser().contains("#1 [PENDING] Send portfolio"));
        assertEquals(updated, result.getPersonForDetails().orElseThrow());
        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertEquals(1, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
        assertArrayEquals(savedContacts, Files.readAllBytes(contactFile));
        assertArrayEquals(savedPrefs, Files.readAllBytes(prefsFile));
    }

    @Test
    void execute_ambiguousNameRetry_preservesOrderedDetailsAndReadOnlyStorage() throws Exception {
        logic.execute("order " + CUSTOM);
        logic.execute("filter t/owesMoney");
        CommandResult candidates = logic.execute("view Meier");
        assertTrue(candidates.getFeedbackToUser().contains("2 contacts found:"));
        assertTrue(candidates.getPersonForDetails().isEmpty());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
        CommandResult selected = logic.execute("view 2");
        assertEquals(Messages.format(DANIEL, CUSTOM), selected.getFeedbackToUser());
        assertEquals(DANIEL, selected.getPersonForDetails().orElseThrow());
        assertEquals(0, contacts.saveCount);
        assertEquals(1, prefs.saveCount);
        assertArrayEquals(contactBytes, Files.readAllBytes(contactFile));
    }

    private static final class CountingPreferences extends JsonUserPrefsStorage {
        private int saveCount;
        private IOException failure;

        private CountingPreferences(Path path) {
            super(path);
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs settings) throws IOException {
            saveCount++;
            if (failure != null) {
                throw failure;
            }
            super.saveUserPrefs(settings);
        }
    }

    private static final class CountingContacts extends JsonContactFileStorage {
        private int saveCount;
        private boolean shouldFail;

        private CountingContacts(Path path) {
            super(path);
        }

        @Override
        public void saveContactFile(ReadOnlyContactFile data) throws IOException {
            saveCount++;
            if (shouldFail) {
                throw new IOException("Simulated contact save failure");
            }
            super.saveContactFile(data);
        }
    }
}
