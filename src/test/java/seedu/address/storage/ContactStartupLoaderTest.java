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

class ContactStartupLoaderTest {

    @TempDir
    private Path testFolder;

    @Test
    void load_missingFile_returnsSamplesWithoutCreatingContactFile() throws Exception {
        Path source = testFolder.resolve("addressbook.json");

        StartupLoadResult result = new ContactStartupLoader().load(source);

        assertEquals(StartupLoadResult.Source.SAMPLE, result.source());
        assertFalse(result.contacts().isEmpty());
        assertFalse(Files.exists(source));
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

        IOException error = assertThrows(IOException.class,
                () -> new ContactStartupLoader().load(source));

        assertTrue(error.getMessage().contains("No valid contacts"));
        assertTrue(error.getMessage().contains("Recovery report:"));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(findBackup()));
    }

    @Test
    void load_malformedJson_preservesOriginal() throws Exception {
        Path source = writeSource("{");
        byte[] original = Files.readAllBytes(source);

        assertThrows(IOException.class,
                () -> new ContactStartupLoader().load(source));

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

        assertThrows(IOException.class,
                () -> new ContactStartupLoader().load(source));

        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void load_atomicReplacementFails_preservesOriginalAndBackup() throws Exception {
        Path source = writeSource("""
                {"persons": [{"name": "Alice"}, {"name": ""}]}
                """);
        byte[] original = Files.readAllBytes(source);

        ContactStartupLoader loader = new ContactStartupLoader(
                new RecoveryArchiveWriter(),
                (temporary, target) -> {
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
}