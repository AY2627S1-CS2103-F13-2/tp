package seedu.address.storage;

import static java.util.Objects.requireNonNull;

/**
 * Describes a contact record skipped during recovery.
 *
 * @param recordNumber the record's one-based position in the original file
 * @param reason the reason the record could not be retained
 */
public record SkippedContact(int recordNumber, String reason) {

    /**
     * Creates details for a skipped record.
     */
    public SkippedContact {
        if (recordNumber < 1) {
            throw new IllegalArgumentException("Record number must be positive.");
        }
        requireNonNull(reason);
        if (reason.isBlank()) {
            throw new IllegalArgumentException("A skipped record must have a reason.");
        }
    }
}