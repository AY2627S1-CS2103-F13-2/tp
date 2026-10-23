package seedu.address.model;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Associates a follow-up with its contact for listing and selection.
 *
 * @param contact the contact owning the follow-up
 * @param followUp the associated action
 */
public record FollowUpEntry(Person contact, FollowUp followUp) {
    /**
     * Creates an entry with a contact and action that are both present.
     */
    public FollowUpEntry {
        requireNonNull(contact);
        requireNonNull(followUp);
    }
}
