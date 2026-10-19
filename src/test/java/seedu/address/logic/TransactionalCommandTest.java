package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies contact write counts and rollback independently of confirmation workflows.
 */
public class TransactionalCommandTest {

    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private CountingStorage storage;
    private Path activeFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        activeFile = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(activeFile).saveContactFile(model.getContactFile());
        storage = new CountingStorage(activeFile);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    @Test
    public void execute_queriesAndNoOpEdit_doNotSave() throws Exception {
        byte[] original = Files.readAllBytes(activeFile);
        for (String command : List.of("list", "find Alice", "view 1", "help")) {
            logic.execute(command);
        }
        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Alice Pauline"));
        assertEquals(0, storage.saveCount);
        assertArrayEquals(original, Files.readAllBytes(activeFile));
    }

    @Test
    public void execute_failedAdd_restoresDataBindingAndDynamicFilter() throws Exception {
        logic.execute("find Alice");
        ContactFile original = new ContactFile(model.getContactFile());
        byte[] bytes = Files.readAllBytes(activeFile);
        ObservableList<Person> binding = model.getFilteredPersonList();
        storage.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("add n/New Contact"));
        assertEquals(original, model.getContactFile());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertSame(binding, model.getFilteredPersonList());
        assertArrayEquals(bytes, Files.readAllBytes(activeFile));
        Person anotherAlice = new PersonBuilder().withName("Alice New").build();
        model.setPerson(BENSON, anotherAlice);
        assertEquals(List.of(ALICE, anotherAlice), model.getFilteredPersonList());
    }

    @Test
    public void execute_failedEdit_restoresOriginalContactAndFile() throws Exception {
        logic.execute("find Alice");
        byte[] bytes = Files.readAllBytes(activeFile);
        ContactFile original = new ContactFile(model.getContactFile());
        storage.shouldFail = true;
        assertThrows(CommandException.class, () -> logic.execute("edit 1 p/76543210"));
        assertEquals(original, model.getContactFile());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertArrayEquals(bytes, Files.readAllBytes(activeFile));
        assertEquals(1, storage.saveCount);
    }

    /**
     * Counts save attempts and simulates a storage failure before writing.
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
