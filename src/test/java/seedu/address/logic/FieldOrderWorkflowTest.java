package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
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
            List.of("email", "phone", "name", "tags", "company", "role", "linkedin", "followups"));

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
