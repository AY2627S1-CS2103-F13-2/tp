package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.PendingFollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FollowUpFilter;

/**
 * Accepts an optional single status or date selector for the follow-up list.
 */
public class PendingFollowUpsCommandParser implements Parser<PendingFollowUpsCommand> {
    @Override
    public PendingFollowUpsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        FollowUpFilter filter = switch (args.trim().toLowerCase(Locale.ROOT)) {
            case "" -> FollowUpFilter.PENDING;
            case "overdue" -> FollowUpFilter.OVERDUE;
            case "today" -> FollowUpFilter.TODAY;
            case "upcoming" -> FollowUpFilter.UPCOMING;
            case "completed" -> FollowUpFilter.COMPLETED;
            case "undated" -> FollowUpFilter.UNDATED;
            default -> throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    PendingFollowUpsCommand.MESSAGE_USAGE));
        };
        return new PendingFollowUpsCommand(filter);
    }
}
