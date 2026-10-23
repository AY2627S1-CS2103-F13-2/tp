package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ContactFile;
import seedu.address.model.ReadOnlyContactFile;

/**
 * Verifies that failures at each atomic save boundary preserve the active file.
 */
public class AtomicContactSaveTest {

    @TempDir
    public Path testFolder;

    @Test
    public void save_existingFile_failuresPreserveOriginalBytes() throws Exception {
        for (String stage : List.of("write", "validate", "replace")) {
            Path directory = Files.createDirectory(testFolder.resolve(stage));
            Path active = directory.resolve("contacts.json");
            new JsonContactFileStorage(active).saveContactFile(getTypicalContactFile());
            byte[] original = Files.readAllBytes(active);
            JsonContactFileStorage storage = failingStorage(active, stage);

            assertThrows(IOException.class, () -> storage.saveContactFile(new ContactFile()));
            assertArrayEquals(original, Files.readAllBytes(active));
            assertEquals(getTypicalContactFile(), storage.readContactFile().orElseThrow());
            try (var files = Files.list(directory)) {
                assertEquals(List.of(active), files.toList());
            }
        }
    }

    @Test
    public void save_missingFile_failedReplacementDoesNotCreateActiveFile() throws Exception {
        Path active = testFolder.resolve("contacts.json");
        assertThrows(IOException.class, () -> failingStorage(active, "replace")
                .saveContactFile(getTypicalContactFile()));
        assertFalse(Files.exists(active));
        try (var files = Files.list(testFolder)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    public void save_validButDifferentCandidate_rejectsBeforeReplacement() throws Exception {
        Path active = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(active).saveContactFile(getTypicalContactFile());
        byte[] original = Files.readAllBytes(active);
        JsonContactFileStorage storage = new JsonContactFileStorage(active) {
            @Override
            void writeCandidate(ReadOnlyContactFile contactFile, Path temporary) throws IOException {
                super.writeCandidate(getTypicalContactFile(), temporary);
            }
        };
        assertThrows(IOException.class, () -> storage.saveContactFile(new ContactFile()));
        assertArrayEquals(original, Files.readAllBytes(active));
    }

    @Test
    public void save_success_replacesActiveFileWithoutTemporaryFiles() throws Exception {
        Path active = testFolder.resolve("nested/contacts.json");
        JsonContactFileStorage storage = new JsonContactFileStorage(active);
        storage.saveContactFile(getTypicalContactFile());
        storage.saveContactFile(new ContactFile());
        assertEquals(new ContactFile(), storage.readContactFile().orElseThrow());
        try (var files = Files.list(active.getParent())) {
            assertEquals(List.of(active), files.toList());
        }
    }

    private JsonContactFileStorage failingStorage(Path active, String stage) {
        return new JsonContactFileStorage(active) {
            @Override
            void writeCandidate(ReadOnlyContactFile contactFile, Path temporary) throws IOException {
                if (stage.equals("write")) {
                    Files.writeString(temporary, "partial");
                    throw new IOException("Simulated interrupted write");
                }
                if (stage.equals("validate")) {
                    Files.writeString(temporary, "invalid json");
                } else {
                    super.writeCandidate(contactFile, temporary);
                }
            }

            @Override
            void replaceActiveFile(Path temporary, Path target) throws IOException {
                if (stage.equals("replace")) {
                    throw new IOException("Simulated replacement failure");
                }
                super.replaceActiveFile(temporary, target);
            }
        };
    }
}
