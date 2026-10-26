package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import seedu.address.model.FollowUpDateFilter;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.Model;

/**
 * Displays pending follow-ups across all contacts in due-date order.
 */
public class PendingFollowUpsCommand extends Command {
    public static final String COMMAND_WORD = "followups";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists pending follow-ups across all contacts, earliest due dates first and undated actions last.\n"
            + "Parameters: [overdue|today|upcoming]. Upcoming includes all future dates.\n"
            + "Then enter a displayed number, another command, or n/cancel to stop.\n"
            + "Example: " + COMMAND_WORD + " upcoming";
    public static final String MESSAGE_EMPTY = "No pending follow-ups remain.";

    private final FollowUpDateFilter filter;
    private final Clock clock;

    public PendingFollowUpsCommand() {
        this(FollowUpDateFilter.ALL);
    }

    public PendingFollowUpsCommand(FollowUpDateFilter filter) {
        this(filter, Clock.systemDefaultZone());
    }

    PendingFollowUpsCommand(FollowUpDateFilter filter, Clock clock) {
        this.filter = requireNonNull(filter);
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<FollowUpEntry> entries = filter.apply(model.getPendingFollowUps(), LocalDate.now(clock));
        if (entries.isEmpty()) {
            String message = switch (filter) {
                case ALL -> MESSAGE_EMPTY;
                case OVERDUE -> "No overdue follow-ups remain.";
                case TODAY -> "No follow-ups are due today.";
                case UPCOMING -> "No upcoming follow-ups remain.";
            };
            return new CommandResult(message, false, false, false);
        }
        String heading = switch (filter) {
            case ALL -> "Pending follow-ups";
            case OVERDUE -> "Overdue follow-ups";
            case TODAY -> "Follow-ups due today";
            case UPCOMING -> "Upcoming follow-ups";
        };
        PendingRequest request = new FollowUpSelectionRequest(entries, heading);
        return new CommandResult(request.getPrompt(), request);
    }
}
