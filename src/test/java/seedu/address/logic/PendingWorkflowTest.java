package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises parsing, pending replies, real disk persistence, and rollback together.
 */
public class PendingWorkflowTest {

    @TempDir
    public Path testFolder;

    private Model model;
    private LogicManager logic;
    private CountingStorage contactStorage;
    private Path activeFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        activeFile = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(activeFile).saveContactFile(model.getContactFile());
        contactStorage = new CountingStorage(activeFile);
        logic = new LogicManager(model, new StorageManager(contactStorage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    @Test
    public void execute_readOnlyAndRejectedCommands_neverSave() throws Exception {
        byte[] original = Files.readAllBytes(activeFile);
        for (String command : List.of("list", "find Alice", "view 1", "help")) {
            logic.execute(command);
        }
        assertThrows(ParseException.class, () -> logic.execute("clear 3"));
        assertThrows(CommandException.class, () -> logic.execute("delete 99"));
        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Alice Pauline"));
        assertEquals(0, contactStorage.saveCount);
        assertArrayEquals(original, Files.readAllBytes(activeFile));
    }

    @Test
    public void execute_clearWhileFiltered_countsAllAndCancelsWithoutSaving() throws Exception {
        logic.execute("find Alice");
        ContactFile original = new ContactFile(model.getContactFile());
        CommandResult preview = logic.execute("clear");
        assertTrue(preview.getFeedbackToUser().contains("Clear all " + original.getPersonList().size()));
        for (String reply : List.of("maybe", "delete 1", "list", "")) {
            assertTrue(logic.execute(reply).getPendingRequest().isPresent());
            assertEquals(original, model.getContactFile());
        }
        logic.execute("n");
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(original, model.getContactFile());
        assertEquals(0, contactStorage.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    public void execute_confirmClear_savesEmptyListOnce() throws Exception {
        logic.execute("find Alice");
        logic.execute("clear");
        logic.execute(" Y ");
        assertTrue(model.getContactFile().getPersonList().isEmpty());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(contactStorage.readContactFile().orElseThrow().getPersonList().isEmpty());
        assertEquals(1, contactStorage.saveCount);
    }

    @Test
    public void execute_saveFailure_restoresDataFilterAndBindings() throws Exception {
        for (String command : List.of("clear")) {
            logic.execute("find Alice");
            ContactFile original = new ContactFile(model.getContactFile());
            byte[] originalBytes = Files.readAllBytes(activeFile);
            ObservableList<Person> binding = model.getFilteredPersonList();
            logic.execute(command);
            contactStorage.shouldFail = true;
            assertThrows(CommandException.class, () -> logic.execute("y"));
            contactStorage.shouldFail = false;
            assertEquals(original, model.getContactFile());
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
            assertSame(binding, model.getFilteredPersonList());
            assertArrayEquals(originalBytes, Files.readAllBytes(activeFile));
            assertThrows(ParseException.class, () -> logic.execute("y"));
        }
    }

    @Test
    public void execute_failedAdd_restoresOriginalPredicateForFutureContacts() throws Exception {
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        contactStorage.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("add n/New Contact"));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        Person anotherAlice = new PersonBuilder().withName("Alice New").build();
        model.setPerson(BENSON, anotherAlice);
        assertEquals(List.of(ALICE, anotherAlice), model.getFilteredPersonList());
    }

    /**
     * Counts actual save attempts and can fail before any disk mutation.
     */
    private static final class CountingStorage extends JsonContactFileStorage {
        private int saveCount;
        private boolean shouldFail;

        private CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
            saveCount++;
            if (shouldFail) {
                throw new IOException("Simulated save failure");
            }
            super.saveContactFile(contactFile);
        }
    }
}
