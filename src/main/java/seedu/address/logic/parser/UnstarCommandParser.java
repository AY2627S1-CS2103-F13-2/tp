package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UnstarCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an index or find query into a command to unstar contacts.
 */
public class UnstarCommandParser implements Parser<UnstarCommand> {

    /**
     * Parses a find query or the displayed index of the contact to unstar.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public UnstarCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        // Preserve index validation, including malformed numeric input.
        if (!trimmedArgs.isEmpty() && !ParserUtil.isIndexInput(trimmedArgs)
                && !trimmedArgs.matches("[+-]?\\d+\\.\\d+")) {
            return new UnstarCommand(new FindCommandParser().parse(args));
        }
        try {
            Index index = ParserUtil.parseIndex(args);
            return new UnstarCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnstarCommand.MESSAGE_USAGE), pe);
        }
    }
}
