package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Contains contacts and the completed startup loading outcome.
 *
 * @param contacts the contacts available to the application
 * @param sourceFile the active contact file location
 * @param source how the contacts were obtained
 * @param recoveryArchive the original backup and report created for recovery or a user-requested reset
 * @param skippedCount the number of skipped records
 */
public record StartupLoadResult(
        List<Person> contacts,
        Path sourceFile,
        Source source,
        Optional<RecoveryArchive> recoveryArchive,
        int skippedCount) {

    /**
     * Describes the source of the startup contacts.
     */
    public enum Source {
        STORED,
        SAMPLE,
        RECOVERED,
        RESET
    }

    /**
     * Creates a consistent startup result.
     */
    public StartupLoadResult {
        contacts = List.copyOf(contacts);
        sourceFile = requireNonNull(sourceFile).toAbsolutePath().normalize();
        requireNonNull(source);
        requireNonNull(recoveryArchive);

        if (source == Source.RECOVERED) {
            if (recoveryArchive.isEmpty() || skippedCount < 1 || contacts.isEmpty()) {
                throw new IllegalArgumentException("Invalid recovered startup result.");
            }
        } else if (source == Source.RESET) {
            if (recoveryArchive.isEmpty() || !contacts.isEmpty() || skippedCount != 0) {
                throw new IllegalArgumentException("Invalid reset startup result.");
            }
        } else if (recoveryArchive.isPresent() || skippedCount != 0) {
            throw new IllegalArgumentException(
                    "Only recovered or reset results can contain recovery details.");
        }
    }
}
