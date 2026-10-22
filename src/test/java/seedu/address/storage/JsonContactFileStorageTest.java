package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.ContactFile;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonContactFileStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonContactFileStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readContactFile_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readContactFile(null));
    }

    private java.util.Optional<ReadOnlyContactFile> readContactFile(String filePath) throws Exception {
        return new JsonContactFileStorage(Paths.get(filePath)).readContactFile(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readContactFile("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readContactFile("notJsonFormatContactFile.json"));
    }

    @Test
    public void readContactFile_nullDocument_throwsDataLoadingException() throws IOException {
        Path source = testFolder.resolve("contactfile.json");
        Files.writeString(source, "null");

        assertThrows(DataLoadingException.class, () -> new JsonContactFileStorage(source).readContactFile());
    }

    @Test
    public void readContactFile_invalidPersonContactFile_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readContactFile("invalidPersonContactFile.json"));
    }

    @Test
    public void readContactFile_invalidAndValidPersonContactFile_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readContactFile("invalidAndValidPersonContactFile.json"));
    }

    @Test
    public void readAndSaveContactFile_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempContactFile.json");
        ContactFile original = getTypicalContactFile();
        JsonContactFileStorage jsonContactFileStorage = new JsonContactFileStorage(filePath);

        // Save in new file and read back
        jsonContactFileStorage.saveContactFile(original, filePath);
        ReadOnlyContactFile readBack = jsonContactFileStorage.readContactFile(filePath).get();
        assertEquals(original, new ContactFile(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonContactFileStorage.saveContactFile(original, filePath);
        readBack = jsonContactFileStorage.readContactFile(filePath).get();
        assertEquals(original, new ContactFile(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonContactFileStorage.saveContactFile(original); // file path not specified
        readBack = jsonContactFileStorage.readContactFile().get(); // file path not specified
        assertEquals(original, new ContactFile(readBack));

    }

    @Test
    public void readAndSaveContactFile_personWithoutOptionalFields_success() throws Exception {
        Path filePath = testFolder.resolve("TempContactFile.json");
        ContactFile original = new ContactFile();
        original.addPerson(NAME_ONLY);
        JsonContactFileStorage jsonContactFileStorage = new JsonContactFileStorage(filePath);

        jsonContactFileStorage.saveContactFile(original, filePath);
        ReadOnlyContactFile readBack = jsonContactFileStorage.readContactFile(filePath).get();
        assertEquals(original, new ContactFile(readBack));
    }

    @Test
    public void readAndSaveContactFile_personWithIntroducer_success() throws Exception {
        Path filePath = testFolder.resolve("TempContactFile.json");
        ContactFile original = new ContactFile();
        original.addPerson(new PersonBuilder(NAME_ONLY).withIntroducer("Jane Doe").build());
        JsonContactFileStorage jsonContactFileStorage = new JsonContactFileStorage(filePath);

        jsonContactFileStorage.saveContactFile(original, filePath);
        ReadOnlyContactFile readBack = jsonContactFileStorage.readContactFile(filePath).get();

        assertEquals(original, new ContactFile(readBack));
    }

    @Test
    public void readAndSaveContactFile_starredStatus_preservedOnReloadAndStartup() throws Exception {
        Path filePath = testFolder.resolve("StarredContactFile.json");
        Person starredAlice = new PersonBuilder(ALICE).withStarred(true).build();
        ContactFile original = new ContactFile();
        original.addPerson(starredAlice);
        original.addPerson(NAME_ONLY);
        JsonContactFileStorage storage = new JsonContactFileStorage(filePath);

        storage.saveContactFile(original);

        assertEquals(original, new ContactFile(storage.readContactFile().orElseThrow()));
        assertEquals(List.of(starredAlice, NAME_ONLY), storage.loadForStartup().contacts());

        original.setPerson(starredAlice, ALICE);
        storage.saveContactFile(original);

        assertEquals(original, new ContactFile(storage.readContactFile().orElseThrow()));
        assertEquals(List.of(ALICE, NAME_ONLY), storage.loadForStartup().contacts());
    }

    @Test
    public void readContactFile_fileWithLegacyAddressField_ignoresUnknownField() throws Exception {
        // Save files written before the address field was removed must still load, minus the address.
        Path filePath = testFolder.resolve("LegacyContactFile.json");
        FileUtil.writeToFile(filePath, "{\"persons\": [ { \"name\": \"Legacy Person\","
                + " \"phone\": \"94351253\", \"email\": \"legacy@example.com\","
                + " \"address\": \"123, Jurong West Ave 6, #08-111\" } ]}");

        ReadOnlyContactFile readBack = new JsonContactFileStorage(filePath).readContactFile(filePath).get();

        Person legacyPerson = readBack.getPersonList().get(0);
        assertEquals("Legacy Person", legacyPerson.getName().fullName);
        assertEquals("legacy@example.com", legacyPerson.getEmail().orElseThrow().value);
        assertEquals(java.util.Optional.empty(), legacyPerson.getCompany());
    }

    @Test
    public void saveContactFile_nullContactFile_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveContactFile(null, "SomeFile.json"));
    }

    /**
     * Saves {@code contactFile} at the specified {@code filePath}.
     */
    private void saveContactFile(ReadOnlyContactFile contactFile, String filePath) {
        try {
            new JsonContactFileStorage(Paths.get(filePath))
                    .saveContactFile(contactFile, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveContactFile_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveContactFile(new ContactFile(), null));
    }
}
