package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Writes and verifies a sibling candidate before asking its caller to replace the active file.
 */
final class AtomicFileWriter {
    private static final Logger logger = LogsCenter.getLogger(AtomicFileWriter.class);

    private AtomicFileWriter() {
    }

    static void write(Path target, String prefix, FileOperation writer, FileOperation verifier,
                      Replacement replacement) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), prefix, ".tmp");
        try {
            writer.run(temporary);
            verifier.run(temporary);
            replacement.replace(temporary, target);
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupFailure) {
                logger.warning("Could not remove temporary file " + temporary + ": " + cleanupFailure.getMessage());
            }
        }
    }

    static void replace(Path temporary, Path target) throws IOException {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    @FunctionalInterface
    interface FileOperation {
        void run(Path file) throws IOException;
    }

    @FunctionalInterface
    interface Replacement {
        void replace(Path temporary, Path target) throws IOException;
    }
}
