package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * Identifies the preserved source snapshot used when the user chooses to start anew.
 *
 * @param sourceFile the active contact file
 * @param archive the original backup and failure report
 * @param originalBytes the bytes that must still match the active file before clearing it
 */
public record StartupResetPlan(Path sourceFile, RecoveryArchive archive, byte[] originalBytes) {

    /**
     * Creates an immutable snapshot of the archived contact file.
     */
    public StartupResetPlan {
        sourceFile = requireNonNull(sourceFile).toAbsolutePath().normalize();
        requireNonNull(archive);
        originalBytes = originalBytes.clone();
    }

    @Override
    public byte[] originalBytes() {
        return originalBytes.clone();
    }
}
