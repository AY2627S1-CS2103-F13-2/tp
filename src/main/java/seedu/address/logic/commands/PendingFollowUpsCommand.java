package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import seedu.address.model.FollowUpEntry;
import seedu.address.model.FollowUpFilter;
import seedu.address.model.Model;

/**
 * Displays follow-ups across all contacts by status or due date.
 */
public class PendingFollowUpsCommand extends Command {
    public static final String COMMAND_WORD = "followups";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists follow-ups across all contacts, earliest due dates first and undated actions last.\n"
            + "Parameters: [overdue|today|upcoming|completed|undated]. Default: all pending actions.\n"
            + "Upcoming includes all future dates; undated includes only pending actions.\n"
            + "Then enter a displayed number, another command, or n/cancel to stop.\n"
            + "Example: " + COMMAND_WORD + " upcoming";
    public static final String MESSAGE_EMPTY = "No pending follow-ups remain.";

    private final FollowUpFilter filter;
    private final Clock clock;

    public PendingFollowUpsCommand() {
        this(FollowUpFilter.PENDING);
    }

    public PendingFollowUpsCommand(FollowUpFilter filter) {
        this(filter, Clock.systemDefaultZone());
    }

    PendingFollowUpsCommand(FollowUpFilter filter, Clock clock) {
        this.filter = requireNonNull(filter);
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<FollowUpEntry> entries = filter.apply(model.getFollowUps(), LocalDate.now(clock));
        String heading = switch (filter) {
            case PENDING -> "Pending follow-ups";
            case OVERDUE -> "Overdue follow-ups";
            case TODAY -> "Follow-ups due today";
            case UPCOMING -> "Upcoming follow-ups";
            case COMPLETED -> "Completed follow-ups";
            case UNDATED -> "Undated pending follow-ups";
        };
        FollowUpListing listing = new FollowUpListing(heading, entries);
        if (entries.isEmpty()) {
            String message = switch (filter) {
                case PENDING -> MESSAGE_EMPTY;
                case OVERDUE -> "No overdue follow-ups remain.";
                case TODAY -> "No follow-ups are due today.";
                case UPCOMING -> "No upcoming follow-ups remain.";
                case COMPLETED -> "No completed follow-ups found.";
                case UNDATED -> "No undated pending follow-ups remain.";
            };
            return new CommandResult(message, false, false, false).withFollowUpListing(listing);
        }
        PendingRequest request = new FollowUpSelectionRequest(entries, heading);
        return new CommandResult(request.getPrompt(), request).withFollowUpListing(listing);
    }
}
