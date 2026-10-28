package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.model.person.FollowUp;

/**
 * Selects pending follow-ups relative to one date, preserving the supplied list order.
 */
public enum FollowUpDateFilter {
    ALL,
    OVERDUE,
    TODAY,
    UPCOMING;

    /**
     * Returns an unmodifiable snapshot. Upcoming has no upper date limit.
     */
    public List<FollowUpEntry> apply(List<FollowUpEntry> entries, LocalDate today) {
        requireNonNull(entries);
        requireNonNull(today);
        return entries.stream().filter(entry -> matches(entry.followUp(), today)).toList();
    }

    private boolean matches(FollowUp action, LocalDate today) {
        if (action.getStatus() != FollowUp.Status.PENDING) {
            return false;
        }
        LocalDate date = action.getDueDate().orElse(null);
        return switch (this) {
            case ALL -> true;
            case OVERDUE -> date != null && date.isBefore(today);
            case TODAY -> date != null && date.equals(today);
            case UPCOMING -> date != null && date.isAfter(today);
        };
    }
}
