package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Startup loading and reset operations required by contact storage.
 */
interface ContactStartupOperations {
    StartupLoadResult load(Path source) throws IOException;

    StartupResetPlan prepareStartupReset(Path source, IOException failure) throws IOException;

    StartupLoadResult resetForStartup(Path source, StartupResetPlan plan) throws IOException;
}
