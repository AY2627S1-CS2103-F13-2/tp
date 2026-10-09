package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotEquals(first.backupFile(), second.backupFile());

        String report = Files.readString(first.reportFile());
        assertTrue(report.contains("2026-10-09T14:30:52+08:00"));
        assertTrue(report.contains("Record 1: Missing name."));
        assertTrue(report.contains("Skipped records: 1"));
        assertTrue(report.contains("will not replace the active file"));
    }
}
