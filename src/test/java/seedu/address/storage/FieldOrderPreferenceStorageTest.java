package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.FieldOrder;
import seedu.address.model.UserPrefs;

class FieldOrderPreferenceStorageTest {
    private static final FieldOrder CUSTOM = new FieldOrder(
            List.of("email", "phone", "introducer", "name", "tags", "company", "role", "linkedin", "followups",
                    "interactions"));

    @TempDir
    public Path testFolder;

    @Test
    void save_failuresAtEveryBoundary_preserveActiveBytesAndRemoveTemporaryFile() throws Exception {
        for (String stage : List.of("write", "validate", "different", "missing", "replace")) {
            Path directory = Files.createDirectory(testFolder.resolve(stage));
            Path active = directory.resolve("preferences.json");
            new JsonUserPrefsStorage(active).saveUserPrefs(new UserPrefs());
            byte[] original = Files.readAllBytes(active);
            UserPrefs requested = new UserPrefs();
            requested.setFieldOrder(CUSTOM);
            assertThrows(IOException.class, () -> failingStorage(active, stage).saveUserPrefs(requested));
            assertArrayEquals(original, Files.readAllBytes(active));
            try (var files = Files.list(directory)) {
                assertEquals(List.of(active), files.toList());
            }
        }
    }

    @Test
    void save_missingFile_failedReplacementLeavesNoActiveOrTemporaryFile() throws Exception {
        Path active = testFolder.resolve("preferences.json");
        assertThrows(IOException.class, () -> failingStorage(active, "replace").saveUserPrefs(new UserPrefs()));
        assertFalse(Files.exists(active));
        try (var files = Files.list(testFolder)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void save_success_createsDirectoriesAndPreservesGuiAndOrderOnReplacement() throws Exception {
        Path active = testFolder.resolve("nested/preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(active);
        storage.saveUserPrefs(new UserPrefs());
        UserPrefs requested = new UserPrefs();
        requested.setFieldOrder(CUSTOM);
        requested.setGuiSettings(new GuiSettings(1000, 800, 10, 20));
        storage.saveUserPrefs(requested);
        assertEquals(requested, storage.readUserPrefs().orElseThrow());
        try (var files = Files.list(active.getParent())) {
            assertEquals(List.of(active), files.toList());
        }
    }

    @Test
    void read_legacyPreferences_defaultsOrderWithoutRewritingFile() throws Exception {
        Path file = testFolder.resolve("legacy.json");
        Files.writeString(file, "{\"guiSettings\":{\"windowWidth\":1000,\"windowHeight\":800}}");
        byte[] bytes = Files.readAllBytes(file);
        UserPrefs prefs = new JsonUserPrefsStorage(file).readUserPrefs().orElseThrow();
        assertEquals(FieldOrder.DEFAULT, prefs.getFieldOrder());
        assertEquals(1000, prefs.getGuiSettings().getWindowWidth());
        assertArrayEquals(bytes, Files.readAllBytes(file));
    }

    @Test
    void read_invalidOrder_rejectsMalformedPreferencesWithoutWriting() throws Exception {
        Path file = testFolder.resolve("invalid.json");
        for (String order : List.of("null", "[]", "{}", "true", "\"name\"",
                "[\"name\",\"tags\",\"company\",\"role\",\"phone\",\"email\",\"linkedin\",\"name\"]",
                "[\"name\",\"tags\",\"company\",\"role\",\"phone\",\"email\",\"linkedin\",\"unknown\"]")) {
            String json = "{\"fieldOrder\":" + order + "}";
            Files.writeString(file, json);
            assertThrows(DataLoadingException.class, () -> new JsonUserPrefsStorage(file).readUserPrefs(), order);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    void save_cleanupFailure_doesNotReportSuccessfulReplacementAsFailure() throws Exception {
        Path active = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(active) {
            @Override
            void replaceActiveFile(Path temporary, Path target) throws IOException {
                super.replaceActiveFile(temporary, target);
                Files.createDirectory(temporary);
                Files.writeString(temporary.resolve("blocked"), "Simulated cleanup failure");
            }
        };
        UserPrefs requested = new UserPrefs();
        requested.setFieldOrder(CUSTOM);
        storage.saveUserPrefs(requested);
        assertEquals(requested, storage.readUserPrefs().orElseThrow());
    }

    private JsonUserPrefsStorage failingStorage(Path active, String stage) {
        return new JsonUserPrefsStorage(active) {
            @Override
            void writeCandidate(UserPrefs expected, Path temporary) throws IOException {
                switch (stage) {
                    case "write" -> {
                        Files.writeString(temporary, "partial");
                        throw new IOException("Simulated interrupted write");
                    }
                    case "validate" -> Files.writeString(temporary, "invalid json");
                    case "different" -> super.writeCandidate(new UserPrefs(), temporary);
                    case "missing" -> Files.delete(temporary);
                    default -> super.writeCandidate(expected, temporary);
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
