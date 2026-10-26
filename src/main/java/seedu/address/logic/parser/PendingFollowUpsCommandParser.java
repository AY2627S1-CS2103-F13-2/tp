package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.PendingFollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FollowUpDateFilter;

/**
 * Accepts an optional single date selector for the pending follow-up list.
 */
public class PendingFollowUpsCommandParser implements Parser<PendingFollowUpsCommand> {
    @Override
    public PendingFollowUpsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        FollowUpDateFilter filter = switch (args.trim().toLowerCase(Locale.ROOT)) {
            case "" -> FollowUpDateFilter.ALL;
            case "overdue" -> FollowUpDateFilter.OVERDUE;
            case "today" -> FollowUpDateFilter.TODAY;
            case "upcoming" -> FollowUpDateFilter.UPCOMING;
            default -> throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    PendingFollowUpsCommand.MESSAGE_USAGE));
        };
        return new PendingFollowUpsCommand(filter);
    }
}
