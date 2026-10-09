package seedu.address.storage;

import java.util.List;

import seedu.address.model.person.Person;

/**
 * Contains the outcome of validating contact records for recovery.
 * Creating this result does not write files or confirm that recovery was saved.
 *
 * @param recoveredContacts valid contacts in their original relative order
 * @param skippedContacts skipped records in their original order
 */
public record ContactRecoveryResult(
        List<Person> recoveredContacts,
        List<SkippedContact> skippedContacts) {

    /**
     * Creates a recovery result with immutable copies of both lists.
     */
    public ContactRecoveryResult {
        recoveredContacts = List.copyOf(recoveredContacts);
        skippedContacts = List.copyOf(skippedContacts);
    }

    /**
     * Returns the number of valid contacts retained.
     */
    public int getRecoveredCount() {
        return recoveredContacts.size();
    }

    /**
     * Returns the number of records skipped.
     */
    public int getSkippedCount() {
        return skippedContacts.size();
    }
}
