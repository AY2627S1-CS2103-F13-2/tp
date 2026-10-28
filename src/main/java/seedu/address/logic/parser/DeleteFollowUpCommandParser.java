package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteFollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an exact contact-index and follow-up-ID pair for deletion.
 */
public class DeleteFollowUpCommandParser implements Parser<DeleteFollowUpCommand> {
    private static final Pattern ARGUMENTS = Pattern.compile("\\s*([0-9]+)\\s+i/([0-9]+)\\s*");

    @Override
    public DeleteFollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = ARGUMENTS.matcher(args);
        if (matcher.matches()) {
            try {
                Index targetIndex = ParserUtil.parseIndex(matcher.group(1));
                Index actionId = ParserUtil.parseIndex(matcher.group(2));
                return new DeleteFollowUpCommand(targetIndex, actionId.getOneBased());
            } catch (ParseException e) {
                throw invalidFormat();
            }
        }
        throw invalidFormat();
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteFollowUpCommand.MESSAGE_USAGE));
    }
}
