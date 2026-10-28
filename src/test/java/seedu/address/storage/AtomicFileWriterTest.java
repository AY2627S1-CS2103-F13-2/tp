package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtomicFileWriterTest {
    @TempDir
    private Path testFolder;

    @Test
    void write_success_replacesOnlyAfterVerification() throws Exception {
        Path target = testFolder.resolve("contacts.json");
        Files.writeString(target, "old");
        List<String> stages = new ArrayList<>();
        AtomicReference<Path> candidate = new AtomicReference<>();

        AtomicFileWriter.write(target, ".test-", temporary -> {
            candidate.set(temporary);
            assertEquals(target.getParent(), temporary.getParent());
            Files.writeString(temporary, "new");
            stages.add("write");
        }, temporary -> {
            assertEquals("old", Files.readString(target));
            assertEquals("new", Files.readString(temporary));
            stages.add("verify");
        }, (temporary, destination) -> {
            stages.add("replace");
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        });

        assertEquals(List.of("write", "verify", "replace"), stages);
        assertEquals("new", Files.readString(target));
        assertFalse(Files.exists(candidate.get()));
    }

    @Test
    void write_failureAtEachStage_preservesOriginalAndRemovesCandidate() throws Exception {
        for (int stage = 0; stage < 3; stage++) {
            int failingStage = stage;
            Path target = testFolder.resolve("contacts" + stage + ".json");
            Files.writeString(target, "old");
            IOException failure = new IOException("Stage " + stage);
            AtomicReference<Path> candidate = new AtomicReference<>();

            IOException actual = assertThrows(IOException.class, () -> AtomicFileWriter.write(target, ".test-",
                    temporary -> {
                        candidate.set(temporary);
                        Files.writeString(temporary, "new");
                        if (failingStage == 0) {
                            throw failure;
                        }
                    }, temporary -> {
                        if (failingStage == 1) {
                            throw failure;
                        }
                    }, (temporary, destination) -> {
                        throw failure;
                    }));

            assertSame(failure, actual);
            assertEquals("old", Files.readString(target));
            assertFalse(Files.exists(candidate.get()));
        }
    }

    @Test
    void write_cleanupFails_doesNotHideOriginalFailure() throws Exception {
        Path target = testFolder.resolve("contacts.json");
        Files.writeString(target, "old");
        IOException failure = new IOException("Writing failed.");

        IOException actual = assertThrows(IOException.class, () -> AtomicFileWriter.write(target, ".test-",
                temporary -> {
                    Files.delete(temporary);
                    Files.createDirectory(temporary);
                    Files.writeString(temporary.resolve("child"), "Prevents directory cleanup.");
                    throw failure;
                }, temporary -> { }, (temporary, destination) -> { }));

        assertSame(failure, actual);
        assertEquals("old", Files.readString(target));
    }
}
