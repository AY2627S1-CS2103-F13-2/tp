package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;

public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage);
    }

    private Path getTempFilePath(String fileName) {
        return testFolder.resolve(fileName);
    }

    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(300, 600, 4, 6));
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void addressBookReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonAddressBookStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonAddressBookStorageTest} class.
         */
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);
        ReadOnlyAddressBook retrieved = storageManager.readAddressBook().get();
        assertEquals(original, new AddressBook(retrieved));
    }

    @Test
    public void getAddressBookFilePath() {
        assertNotNull(storageManager.getAddressBookFilePath());
    }

    @Test
    public void loadForStartup_missingFile_returnsSamplesWithoutWritingFile() throws Exception {
        StartupLoadResult result = storageManager.loadForStartup();

        assertEquals(StartupLoadResult.Source.SAMPLE, result.source());
        assertEquals(getTempFilePath("ab").toAbsolutePath().normalize(), result.sourceFile());
        assertFalse(Files.exists(storageManager.getAddressBookFilePath()));
    }

    @Test
    public void loadForStartup_savedContacts_loadsAllContacts() throws Exception {
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);

        StartupLoadResult result = storageManager.loadForStartup();

        assertEquals(StartupLoadResult.Source.STORED, result.source());
        assertEquals(original.getPersonList(), result.contacts());
        assertTrue(result.recoveryArchive().isEmpty());
    }

    @Test
    public void loadForStartup_recoverableFile_repairsConfiguredFile() throws Exception {
        Path source = storageManager.getAddressBookFilePath();
        Files.writeString(source, "{\"persons\": [{\"name\": \"Alice\"}, {}]}");
        byte[] original = Files.readAllBytes(source);

        StartupLoadResult result = storageManager.loadForStartup();

        assertEquals(StartupLoadResult.Source.RECOVERED, result.source());
        assertEquals(1, result.skippedCount());
        assertArrayEquals(original, Files.readAllBytes(result.recoveryArchive().orElseThrow().backupFile()));
        assertEquals(result.contacts(), storageManager.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void loadForStartup_invalidDocument_propagatesFailureAndPreservesFile() throws Exception {
        Path source = storageManager.getAddressBookFilePath();
        Files.writeString(source, "{");
        byte[] original = Files.readAllBytes(source);

        assertThrows(IOException.class, storageManager::loadForStartup);

        assertArrayEquals(original, Files.readAllBytes(source));
        assertFalse(Files.exists(testFolder.resolve("recovery")));
    }

    @Test
    public void getUserPrefsFilePath_returnsConfiguredPath() {
        assertEquals(getTempFilePath("prefs"), storageManager.getUserPrefsFilePath());
    }

}
