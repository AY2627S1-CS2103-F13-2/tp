package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StartupResetTest {

    @TempDir
    private Path testFolder;

    @Test
    void prepare_sourceChangesDuringArchiving_reportsArchiveAndPreservesNewerFile() throws Exception {
        Path source = writeSource("{");
        byte[] original = Files.readAllBytes(source);
        RecoveryArchive[] preserved = new RecoveryArchive[1];
        ContactStartupLoader loader = new ContactStartupLoader(
                new RecoveryArchiveWriter()::write, (path, bytes, reason) -> {
                    preserved[0] = new RecoveryArchiveWriter().writeStartupFailure(path, bytes, reason);
                    Files.writeString(path, "newer data");
                    return preserved[0];
                }, (temporary, target) -> {
                    throw new AssertionError("Must not replace a changed file");
                });

        IOException error = assertThrows(IOException.class, () ->
                loader.prepareStartupReset(source, new IOException("Invalid JSON")));

        assertEquals("newer data", Files.readString(source));
        assertArrayEquals(original, Files.readAllBytes(preserved[0].backupFile()));
        assertTrue(error.getMessage().contains(preserved[0].backupFile().toString()));
        assertTrue(error.getMessage().contains(preserved[0].reportFile().toString()));
    }

    @Test
    void prepare_injectedArchiveFailure_preservesSourceAndDoesNotReplace() throws Exception {
        Path source = writeSource("{broken");
        byte[] original = Files.readAllBytes(source);
        AtomicBoolean archiveAttempted = new AtomicBoolean();
        ContactStartupLoader loader = new ContactStartupLoader(
                new RecoveryArchiveWriter()::write, (path, bytes, reason) -> {
                    archiveAttempted.set(true);
                    assertArrayEquals(original, bytes);
                    throw new IOException("Report write failed");
                }, (temporary, target) -> {
                    throw new AssertionError("Must not replace without an archive");
                });

        assertThrows(IOException.class, () -> loader.prepareStartupReset(source, new IOException("Invalid JSON")));
        assertTrue(archiveAttempted.get());
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void prepare_malformedDocument_preservesBytesAndExplainsChoiceUnderReports() throws Exception {
        Path source = writeSource("{broken JSON\r\n  original whitespace");
        byte[] original = Files.readAllBytes(source);
        ContactStartupLoader loader = new ContactStartupLoader();
        IOException failure = assertThrows(IOException.class, () -> loader.load(source));

        StartupResetPlan plan = loader.prepareStartupReset(source, failure);

        assertEquals(source.toAbsolutePath().normalize(), plan.sourceFile());
        assertEquals(testFolder.resolve("reports"), plan.archive().backupFile().getParent().getParent());
        assertArrayEquals(original, Files.readAllBytes(plan.archive().backupFile()));
        assertArrayEquals(original, Files.readAllBytes(source));
        String report = Files.readString(plan.archive().reportFile());
        assertTrue(report.contains(failure.getMessage()));
        assertTrue(report.contains("before the user chooses"));
        assertTrue(report.contains("original contact file has been preserved unchanged"));
        assertNoTemporaryFiles();
    }

    @Test
    void reset_invalidDocuments_installsEmptyFileAndKeepsOriginalAcrossRestart() throws Exception {
        ContactStartupLoader loader = new ContactStartupLoader();
        for (String json : List.of("{", "null", "", "{\"persons\": []} {}", "{\"persons\": [{}]}")) {
            Path source = writeSource(json);
            byte[] original = Files.readAllBytes(source);
            IOException failure = assertThrows(IOException.class, () -> loader.load(source));
            StartupResetPlan plan = loader.prepareStartupReset(source, failure);

            StartupLoadResult result = loader.resetForStartup(source, plan);

            assertEquals(StartupLoadResult.Source.RESET, result.source());
            assertTrue(result.contacts().isEmpty());
            assertEquals(0, result.skippedCount());
            assertEquals(plan.archive(), result.recoveryArchive().orElseThrow());
            assertArrayEquals(original, Files.readAllBytes(plan.archive().backupFile()));
            StartupLoadResult restart = loader.load(source);
            assertEquals(StartupLoadResult.Source.STORED, restart.source());
            assertTrue(restart.contacts().isEmpty());
            assertNoTemporaryFiles();
        }
    }

    @Test
    void prepare_reportsCannotBeCreated_keepsActiveFile() throws Exception {
        Path source = writeSource("{");
        byte[] original = Files.readAllBytes(source);
        Files.writeString(testFolder.resolve("reports"), "Existing file.");

        assertThrows(IOException.class, () -> new ContactStartupLoader()
                .prepareStartupReset(source, new IOException("Invalid JSON.")));

        assertArrayEquals(original, Files.readAllBytes(source));
        assertEquals("Existing file.", Files.readString(testFolder.resolve("reports")));
        assertNoTemporaryFiles();
    }

    @Test
    void prepare_sourceUnreadableOrMissing_cannotOfferReset() throws Exception {
        ContactStartupLoader loader = new ContactStartupLoader();
        Path missing = testFolder.resolve("missing.json");
        Path directory = Files.createDirectory(testFolder.resolve("directory.json"));

        assertThrows(IOException.class, () -> loader.prepareStartupReset(missing, new IOException("Missing.")));
        assertThrows(IOException.class, () -> loader.prepareStartupReset(directory, new IOException("Unreadable.")));

        assertFalse(Files.exists(missing));
        assertTrue(Files.isDirectory(directory));
        assertFalse(Files.exists(testFolder.resolve("reports")));
    }

    @Test
    void prepare_sourceAlreadyRepaired_refusesToClearValidContacts() throws Exception {
        Path source = writeSource("{\"persons\": [{\"name\": \"Alice\"}]}");
        byte[] original = Files.readAllBytes(source);

        IOException error = assertThrows(IOException.class, () -> new ContactStartupLoader()
                .prepareStartupReset(source, new IOException("Previous startup failed.")));

        assertTrue(error.getMessage().contains("now valid"));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertFalse(Files.exists(testFolder.resolve("reports")));
    }

    @Test
    void reset_sourceChangesAfterOffer_keepsNewerContactsAndArchivedOriginal() throws Exception {
        Path source = writeSource("{");
        ContactStartupLoader loader = new ContactStartupLoader();
        StartupResetPlan plan = loader.prepareStartupReset(source, new IOException("Invalid JSON."));
        byte[] original = Files.readAllBytes(source);
        Files.writeString(source, "{\"persons\": [{\"name\": \"Bob\"}]}");
        byte[] updated = Files.readAllBytes(source);

        IOException error = assertThrows(IOException.class, () -> loader.resetForStartup(source, plan));

        assertTrue(error.getMessage().contains("changed during recovery"));
        assertTrue(error.getMessage().contains(plan.archive().backupFile().toString()));
        assertArrayEquals(updated, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(plan.archive().backupFile()));
        assertNoTemporaryFiles();
    }

    @Test
    void reset_sourceRemovedAfterOffer_doesNotRecreateIt() throws Exception {
        Path source = writeSource("{");
        ContactStartupLoader loader = new ContactStartupLoader();
        StartupResetPlan plan = loader.prepareStartupReset(source, new IOException("Invalid JSON."));
        Files.delete(source);

        assertThrows(IOException.class, () -> loader.resetForStartup(source, plan));

        assertFalse(Files.exists(source));
        assertArrayEquals(plan.originalBytes(), Files.readAllBytes(plan.archive().backupFile()));
        assertNoTemporaryFiles();
    }

    @Test
    void reset_backupOrReportMissingOrChanged_refusesToClearActiveFile() throws Exception {
        ContactStartupLoader loader = new ContactStartupLoader();
        for (String damage : List.of("missing backup", "changed backup", "missing report", "empty report")) {
            Path source = writeSource("{");
            StartupResetPlan plan = loader.prepareStartupReset(source, new IOException("Invalid JSON."));
            byte[] original = Files.readAllBytes(source);
            switch (damage) {
                case "missing backup":
                    Files.delete(plan.archive().backupFile());
                    break;
                case "changed backup":
                    Files.writeString(plan.archive().backupFile(), "Modified backup.");
                    break;
                case "missing report":
                    Files.delete(plan.archive().reportFile());
                    break;
                default:
                    Files.writeString(plan.archive().reportFile(), " ");
                    break;
            }

            assertThrows(IOException.class, () -> loader.resetForStartup(source, plan), damage);

            assertArrayEquals(original, Files.readAllBytes(source), damage);
            assertNoTemporaryFiles();
        }
    }

    @Test
    void reset_atomicMoveUnavailable_keepsActiveFileAndOriginalBackup() throws Exception {
        Path source = writeSource("{");
        byte[] original = Files.readAllBytes(source);
        AtomicBoolean replaced = new AtomicBoolean();
        ContactStartupLoader loader = new ContactStartupLoader(
                new RecoveryArchiveWriter()::write, (temporary, target) -> {
                    replaced.set(true);
                    throw new AtomicMoveNotSupportedException(temporary.toString(), target.toString(), "Unsupported.");
                });
        StartupResetPlan plan = loader.prepareStartupReset(source, new IOException("Invalid JSON."));

        assertThrows(IOException.class, () -> loader.resetForStartup(source, plan));

        assertTrue(replaced.get());
        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(plan.archive().backupFile()));
        assertNoTemporaryFiles();
    }

    @Test
    void reset_planForDifferentFile_doesNotClearEitherFile() throws Exception {
        Path source = writeSource("{");
        Path other = testFolder.resolve("other.json");
        Files.writeString(other, "{\"persons\": [{\"name\": \"Alice\"}]}");
        byte[] originalOther = Files.readAllBytes(other);
        ContactStartupLoader loader = new ContactStartupLoader();
        StartupResetPlan plan = loader.prepareStartupReset(source, new IOException("Invalid JSON."));

        assertThrows(IOException.class, () -> loader.resetForStartup(other, plan));

        assertArrayEquals(plan.originalBytes(), Files.readAllBytes(source));
        assertArrayEquals(originalOther, Files.readAllBytes(other));
    }

    @Test
    void reset_snapshotCannotBeChangedThroughCallerArray() throws Exception {
        Path source = writeSource("{");
        ContactStartupLoader loader = new ContactStartupLoader();
        StartupResetPlan prepared = loader.prepareStartupReset(source, new IOException("Invalid JSON."));
        byte[] bytes = prepared.originalBytes();
        StartupResetPlan plan = new StartupResetPlan(source, prepared.archive(), bytes);
        bytes[0] = 0;
        byte[] exposed = plan.originalBytes();
        exposed[0] = 0;

        assertArrayEquals(Files.readAllBytes(source), plan.originalBytes());
        assertTrue(loader.resetForStartup(source, plan).contacts().isEmpty());
    }

    private Path writeSource(String json) throws IOException {
        Path source = testFolder.resolve("addressbook.json");
        Files.writeString(source, json);
        return source;
    }

    private void assertNoTemporaryFiles() throws IOException {
        try (Stream<Path> paths = Files.list(testFolder)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString().startsWith("addressbook-recovery-")));
        }
    }
}
