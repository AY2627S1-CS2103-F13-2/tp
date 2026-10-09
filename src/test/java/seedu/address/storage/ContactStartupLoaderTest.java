package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;

class ContactStartupLoaderTest {

    @TempDir
    private Path testFolder;

    @Test
    void load_missingFile_returnsExpectedSamplesWithoutRecovery() throws Exception {
        Path source = testFolder.resolve("addressbook.json");

        StartupLoadResult result = new ContactStartupLoader().load(source);

        assertEquals(StartupLoadResult.Source.SAMPLE, result.source());
        assertEquals(
                SampleDataUtil.getSampleAddressBook().getPersonList(),
                result.contacts());
        assertEquals(source.toAbsolutePath().normalize(), result.sourceFile());
        assertEquals(0, result.skippedCount());
        assertTrue(result.recoveryArchive().isEmpty());

        assertFalse(Files.exists(source));
        assertFalse(Files.exists(testFolder.resolve("recovery")));
    }

    @Test
    void load_missingStorageFolder_returnsSamples() throws Exception {
        Path source = testFolder.resolve("data").resolve("addressbook.json");

        StartupLoadResult result = new ContactStartupLoader().load(source);

        assertEquals(StartupLoadResult.Source.SAMPLE, result.source());
        assertEquals(
                SampleDataUtil.getSampleAddressBook().getPersonList(),
                result.contacts());
        assertTrue(result.recoveryArchive().isEmpty());
        assertFalse(Files.exists(source.getParent()));
    }

    @Test
    void load_validEmptyFile_doesNotUseSamplesOrCreateArchive() throws Exception {
        Path source = writeSource("{\"persons\": []}");
        byte[] original = Files.readAllBytes(source);

        StartupLoadResult result = new ContactStartupLoader().load(source);

        assertEquals(StartupLoadResult.Source.STORED, result.source());
        assertTrue(result.contacts().isEmpty());
        assertFalse(Files.exists(testFolder.resolve("recovery")));
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void load_mixedRecords_backsUpAndInstallsRecoveredContacts() throws Exception {
        Path source = writeSource("""
                {"persons": [
                  {"name": "Charlie"},
                  {"name": "Broken", "email": "invalid"},
                  {"name": "Alice"},
                  {"name": "Charlie"}
                ]}
                """);
        byte[] original = Files.readAllBytes(source);

        StartupLoadResult result = new ContactStartupLoader().load(source);

        assertEquals(StartupLoadResult.Source.RECOVERED, result.source());
        assertEquals(2, result.skippedCount());
        assertEquals(List.of("Charlie", "Alice"),
                result.contacts().stream()
                        .map(person -> person.getName().fullName)
                        .toList());

        RecoveryArchive archive = result.recoveryArchive().orElseThrow();
        assertArrayEquals(original, Files.readAllBytes(archive.backupFile()));

        String report = Files.readString(archive.reportFile());
        assertTrue(report.contains("Record 2"));
        assertTrue(report.contains("Record 4"));
        assertTrue(report.contains("retained record 1"));

        StartupLoadResult secondLoad = new ContactStartupLoader().load(source);
        assertEquals(StartupLoadResult.Source.STORED, secondLoad.source());
        assertEquals(result.contacts(), secondLoad.contacts());
    }

    @Test
    void load_allRecordsInvalid_preservesOriginalAndCreatesReport() throws Exception {
        Path source = writeSource("{\"persons\": [{\"phone\": \"invalid\"}]}");
        byte[] original = Files.readAllBytes(source);

        IOException error = assertThrows(
                IOException.class, () -> new ContactStartupLoader().load(source));

        assertTrue(error.getMessage().contains("No valid contacts"));
        assertTrue(error.getMessage().contains("Recovery report:"));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(findBackup()));
    }

    @Test
    void load_malformedJson_preservesOriginal() throws Exception {
        Path source = writeSource("{");
        byte[] original = Files.readAllBytes(source);

        assertThrows(IOException.class, () -> new ContactStartupLoader().load(source));

        assertArrayEquals(original, Files.readAllBytes(source));
        assertFalse(Files.exists(testFolder.resolve("recovery")));
    }

    @Test
    void load_archiveCannotBeCreated_preservesOriginal() throws Exception {
        Path source = writeSource("""
                {"persons": [{"name": "Alice"}, {"name": ""}]}
                """);
        byte[] original = Files.readAllBytes(source);

        Files.writeString(testFolder.resolve("recovery"), "This is a file.");

        assertThrows(IOException.class, () -> new ContactStartupLoader().load(source));

        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void load_atomicReplacementFails_preservesOriginalAndBackup() throws Exception {
        Path source = writeSource("""
                {"persons": [{"name": "Alice"}, {"name": ""}]}
                """);
        byte[] original = Files.readAllBytes(source);

        ContactStartupLoader loader = new ContactStartupLoader(
                new RecoveryArchiveWriter(), (temporary, target) -> {
                    throw new IOException("Simulated atomic replacement failure.");
                });

        IOException error = assertThrows(IOException.class, () -> loader.load(source));

        assertTrue(error.getMessage().contains("Recovery report:"));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(findBackup()));

        try (Stream<Path> paths = Files.list(testFolder)) {
            assertFalse(paths.anyMatch(path ->
                    path.getFileName().toString().startsWith("addressbook-recovery-")));
        }
    }

    private Path writeSource(String json) throws IOException {
        Path source = testFolder.resolve("addressbook.json");
        Files.writeString(source, json, StandardCharsets.UTF_8);
        return source;
    }

    private Path findBackup() throws IOException {
        try (Stream<Path> paths = Files.walk(testFolder.resolve("recovery"))) {
            return paths.filter(path ->
                    path.getFileName().toString().equals("addressbook.original.json"))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Test
    void load_samplesThenClear_restartLoadsEmptySavedFile() throws Exception {
        Path source = testFolder.resolve("data").resolve("addressbook.json");
        ContactStartupLoader loader = new ContactStartupLoader();

        StartupLoadResult firstLaunch = loader.load(source);
        assertEquals(StartupLoadResult.Source.SAMPLE, firstLaunch.source());
        assertFalse(firstLaunch.contacts().isEmpty());

        AddressBook initialData = new AddressBook();
        initialData.setPersons(firstLaunch.contacts());
        ModelManager model = new ModelManager(initialData, new UserPrefs());

        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(source),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(model, storage);

        logic.execute(ClearCommand.COMMAND_WORD);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(Files.exists(source));

        StartupLoadResult secondLaunch = loader.load(source);

        assertEquals(StartupLoadResult.Source.STORED, secondLaunch.source());
        assertTrue(secondLaunch.contacts().isEmpty());
        assertEquals(0, secondLaunch.skippedCount());
        assertTrue(secondLaunch.recoveryArchive().isEmpty());
        assertFalse(Files.exists(source.getParent().resolve("recovery")));
    }

    @Test
    void load_sourceIsDirectory_throwsInsteadOfUsingSamples() throws Exception {
        Path source = Files.createDirectory(testFolder.resolve("addressbook.json"));

        assertThrows(IOException.class, () -> new ContactStartupLoader().load(source));

        assertTrue(Files.isDirectory(source));
        assertFalse(Files.exists(testFolder.resolve("recovery")));
    }
}
