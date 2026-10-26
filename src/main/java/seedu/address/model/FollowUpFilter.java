package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.model.person.FollowUp;

/**
 * Selects follow-ups by status and date, preserving the supplied list order.
 */
public enum FollowUpFilter {
    PENDING,
    OVERDUE,
    TODAY,
    UPCOMING,
    COMPLETED,
    UNDATED;

    /**
     * Returns an unmodifiable snapshot. Upcoming has no upper date limit.
     */
    public List<FollowUpEntry> apply(List<FollowUpEntry> entries, LocalDate today) {
        requireNonNull(entries);
        requireNonNull(today);
        return entries.stream().filter(entry -> matches(entry.followUp(), today)).toList();
    }

    private boolean matches(FollowUp action, LocalDate today) {
        boolean pending = action.getStatus() == FollowUp.Status.PENDING;
        LocalDate date = action.getDueDate().orElse(null);
        return switch (this) {
            case PENDING -> pending;
            case OVERDUE -> pending && date != null && date.isBefore(today);
            case TODAY -> pending && date != null && date.equals(today);
            case UPCOMING -> pending && date != null && date.isAfter(today);
            case COMPLETED -> action.getStatus() == FollowUp.Status.COMPLETED;
            case UNDATED -> pending && date == null;
        };
    }
}
