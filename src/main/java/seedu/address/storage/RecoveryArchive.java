package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * Identifies the files created for a recovery incident.
 *
 * @param backupFile the unchanged original file
 * @param reportFile the validation report
 */
public record RecoveryArchive(Path backupFile, Path reportFile) {

    /**
     * Creates an archive descriptor.
     */
    public RecoveryArchive {
        requireNonNull(backupFile);
        requireNonNull(reportFile);
    }
}
