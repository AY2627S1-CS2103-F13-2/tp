package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonContactFileStorageStartupTest {
    @TempDir
    private Path testFolder;

    @Test
    void startupOperations_useSuppliedLoaderWithoutTouchingFiles() throws Exception {
        Path source = testFolder.resolve("contacts.json");
        RecoveryArchive archive = new RecoveryArchive(
                testFolder.resolve("backup.json"), testFolder.resolve("report.txt"));
        StartupResetPlan plan = new StartupResetPlan(source, archive, new byte[0]);
        StartupLoadResult loaded = new StartupLoadResult(List.of(), source,
                StartupLoadResult.Source.STORED, Optional.empty(), 0);
        StartupLoadResult reset = new StartupLoadResult(List.of(), source,
                StartupLoadResult.Source.RESET, Optional.of(archive), 0);
        IOException failure = new IOException("Invalid file.");
        ContactStartupOperations loader = new ContactStartupOperations() {
            @Override
            public StartupLoadResult load(Path path) {
                assertEquals(source, path);
                return loaded;
            }

            @Override
            public StartupResetPlan prepareStartupReset(Path path, IOException error) {
                assertEquals(source, path);
                assertSame(failure, error);
                return plan;
            }

            @Override
            public StartupLoadResult resetForStartup(Path path, StartupResetPlan prepared) {
                assertEquals(source, path);
                assertSame(plan, prepared);
                return reset;
            }
        };
        JsonContactFileStorage storage = new JsonContactFileStorage(source, loader);

        assertSame(loaded, storage.loadForStartup());
        assertSame(plan, storage.prepareStartupReset(failure));
        assertSame(reset, storage.resetForStartup(plan));
        assertFalse(Files.exists(source));
    }

    @Test
    void loadForStartup_suppliedLoaderFails_propagatesOriginalFailure() {
        IOException failure = new IOException("Injected startup failure.");
        ContactStartupOperations loader = new ContactStartupOperations() {
            @Override
            public StartupLoadResult load(Path path) throws IOException {
                throw failure;
            }

            @Override
            public StartupResetPlan prepareStartupReset(Path path, IOException error) {
                throw new AssertionError("Reset preparation was not requested.");
            }

            @Override
            public StartupLoadResult resetForStartup(Path path, StartupResetPlan plan) {
                throw new AssertionError("Reset was not requested.");
            }
        };
        JsonContactFileStorage storage = new JsonContactFileStorage(testFolder.resolve("contacts.json"), loader);

        assertSame(failure, assertThrows(IOException.class, storage::loadForStartup));
    }
}
