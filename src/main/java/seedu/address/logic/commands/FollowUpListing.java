package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.FollowUpEntry;

/**
 * Structured follow-up results in the same order used by numbered selection.
 *
 * @param heading the active follow-up filter's title
 * @param entries an immutable snapshot of the displayed actions
 */
public record FollowUpListing(String heading, List<FollowUpEntry> entries) {
    /**
     * Copies the listing so presentation cannot alter a pending selection.
     */
    public FollowUpListing {
        requireNonNull(heading);
        entries = List.copyOf(entries);
    }
}
