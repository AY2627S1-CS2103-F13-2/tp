package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an immutable follow-up action with an ID unique within its contact.
 */
public final class FollowUp {
    public static final String MESSAGE_CONSTRAINTS = "Follow-up descriptions must not be blank.";

    private final int id;
    private final String description;
    private final Status status;
    private final LocalDate dueDate;
    private final Clock clock;
    private boolean isOverdue;

    /**
     * Creates an action with a positive ID, a trimmed description, and its stored status.
     */
    public FollowUp(int id, String description, Status status) {
        this(id, description, status, null);
    }

    /**
     * Creates an action with an optional due date. A null date means no due date.
     */
    public FollowUp(int id, String description, Status status, LocalDate dueDate) {
        this(id, description, status, dueDate, Clock.systemDefaultZone());
    }

    /**
     * Creates an action using the supplied clock for overdue detection.
     */
    FollowUp(int id, String description, Status status, LocalDate dueDate, Clock clock) {
        checkArgument(id > 0, "Follow-up IDs must be positive.");
        requireNonNull(description);
        checkArgument(isValidDescription(description), MESSAGE_CONSTRAINTS);
        this.id = id;
        this.description = description.strip();
        this.status = requireNonNull(status);
        this.dueDate = dueDate;
        this.clock = requireNonNull(clock);
        refreshOverdue();
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<LocalDate> getDueDate() {
        return Optional.ofNullable(dueDate);
    }

    /**
     * Refreshes and returns the overdue field so actions remain accurate across day changes.
     * Only pending actions with a due date before today are overdue.
     */
    public boolean isOverdue() {
        refreshOverdue();
        return isOverdue;
    }

    private void refreshOverdue() {
        isOverdue = status == Status.PENDING && dueDate != null && dueDate.isBefore(LocalDate.now(clock));
    }

    /**
     * Returns whether a description contains at least one non-whitespace character.
     */
    public static boolean isValidDescription(String description) {
        return description != null && !description.isBlank();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FollowUp action && id == action.id
                && description.equals(action.description) && status == action.status
                && Objects.equals(dueDate, action.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, status, dueDate);
    }

    @Override
    public String toString() {
        return "#" + id + " [" + status + "] " + description;
    }

    /**
     * Distinguishes pending actions from retained completed history.
     */
    public enum Status {
        PENDING, COMPLETED
    }
}
