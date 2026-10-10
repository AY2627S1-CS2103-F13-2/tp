package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RecoveryArchiveWriterTest {

    @TempDir
    private Path testFolder;

    @Test
    void writeStartupFailure_sameTimestamp_preservesDistinctIncidentsAndExactBytes() throws Exception {
        RecoveryArchiveWriter writer = new RecoveryArchiveWriter(
                Clock.fixed(Instant.parse("2026-10-09T06:30:52Z"), ZoneOffset.UTC));
        Path source = testFolder.resolve("addressbook.json");
        byte[] original = {0, (byte) 0xff, 13, 10};
        Files.write(source, original);

        RecoveryArchive first = writer.writeStartupFailure(source, original, "Malformed document");
        RecoveryArchive second = writer.writeStartupFailure(source, original, "Malformed document");

        assertNotEquals(first.backupFile().getParent(), second.backupFile().getParent());
        for (RecoveryArchive archive : List.of(first, second)) {
            assertEquals(testFolder.resolve("reports"), archive.backupFile().getParent().getParent());
            assertTrue(archive.backupFile().getParent().getFileName().toString().startsWith("2026-10-09_063052_"));
            assertArrayEquals(original, Files.readAllBytes(archive.backupFile()));
            assertTrue(Files.readString(archive.reportFile()).contains("Malformed document"));
        }
        assertArrayEquals(original, Files.readAllBytes(source));
    }

    @Test
    void write_preservesBytesAndCreatesUniqueIncidents() throws Exception {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-09T06:30:52Z"), ZoneOffset.ofHours(8));
        RecoveryArchiveWriter writer = new RecoveryArchiveWriter(clock);

        byte[] original = "original bytes\r\n  unchanged"
                .getBytes(StandardCharsets.UTF_8);
        ContactRecoveryResult result = new ContactRecoveryResult(
                List.of(), List.of(new SkippedContact(1, "Missing name.")));

        RecoveryArchive first = writer.write(
                testFolder.resolve("addressbook.json"), original, result);
        RecoveryArchive second = writer.write(
                testFolder.resolve("addressbook.json"), original, result);

        assertArrayEquals(original, Files.readAllBytes(first.backupFile()));
        assertArrayEquals(original, Files.readAllBytes(second.backupFile()));
        assertNotEquals(first.backupFile(), second.backupFile());

        String report = Files.readString(first.reportFile());
        assertTrue(report.contains("2026-10-09T14:30:52+08:00"));
        assertTrue(report.contains("Record 1: Missing name."));
        assertTrue(report.contains("Skipped records: 1"));
        assertTrue(report.contains("will not replace the active file"));
    }

    @Test
    void write_partialRecoveryReport_containsCountsPathsAndEveryReason() throws Exception {
        Path source = testFolder.resolve("addressbook.json");
        byte[] original = {0, 1, 2, (byte) 0xff, 13, 10};
        Files.write(source, original);
        ContactRecoveryResult result = new ContactRecoveryResult(List.of(ALICE), List.of(
                new SkippedContact(2, "phone: invalid; email: invalid"),
                new SkippedContact(3, "Duplicate contact; conflicts with retained record 1.")));

        RecoveryArchive archive = new RecoveryArchiveWriter().write(source, original, result);

        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(archive.backupFile()));
        assertEquals(archive.backupFile().getParent(), archive.reportFile().getParent());
        String report = Files.readString(archive.reportFile());
        assertTrue(report.contains("Source: " + source.toAbsolutePath().normalize()));
        assertTrue(report.contains("Original backup: " + archive.backupFile()));
        assertTrue(report.contains("Records inspected: 3"));
        assertTrue(report.contains("Valid contacts available for recovery: 1"));
        assertTrue(report.contains("Skipped records: 2"));
        assertTrue(report.contains("Record 2: phone: invalid; email: invalid"));
        assertTrue(report.contains("Record 3: Duplicate contact; conflicts with retained record 1."));
        assertTrue(report.contains("Recovery is complete only when Astra reports successful recovery."));
        assertFalse(report.contains("No valid contacts were found"));
    }

    @Test
    void write_recoveryLocationIsFile_preservesSourceAndExistingFile() throws Exception {
        Path source = testFolder.resolve("addressbook.json");
        byte[] original = "original bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(source, original);
        Path recoveryRoot = testFolder.resolve("recovery");
        Files.writeString(recoveryRoot, "existing file");
        ContactRecoveryResult result = new ContactRecoveryResult(List.of(), List.of(new SkippedContact(1, "Invalid.")));

        assertThrows(IOException.class, () -> new RecoveryArchiveWriter().write(source, original, result));

        assertArrayEquals(original, Files.readAllBytes(source));
        assertEquals("existing file", Files.readString(recoveryRoot));
    }
}
