package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses one displayed index and one action description, rejecting extra command prefixes.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    private static final Pattern FORMAT = Pattern.compile("(?s)\\s*(?<index>\\S+)\\s+a/(?<description>.*)");
    private static final Pattern PREFIX = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");

    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = FORMAT.matcher(args);
        Matcher prefixes = PREFIX.matcher(args);
        int actionCount = 0;
        while (prefixes.find()) {
            if (!prefixes.group().equals("a/")) {
                throw invalidFormat();
            }
            actionCount++;
        }
        if (!matcher.matches() || actionCount != 1) {
            throw invalidFormat();
        }
        String description = matcher.group("description").strip();
        if (!FollowUp.isValidDescription(description)) {
            throw new ParseException(FollowUp.MESSAGE_CONSTRAINTS);
        }
        return new FollowUpCommand(ParserUtil.parseIndex(matcher.group("index")), description);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
    }
}
