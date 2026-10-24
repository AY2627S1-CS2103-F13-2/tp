package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.StarCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an index or find query into a command to star contacts.
 */
public class StarCommandParser implements Parser<StarCommand> {

    /**
     * Parses a find query or the displayed index of the contact to star.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public StarCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        // Preserve index validation, including malformed numeric input.
        if (!trimmedArgs.isEmpty() && !ParserUtil.isIndexInput(trimmedArgs)
                && !trimmedArgs.matches("[+-]?\\d+\\.\\d+")) {
            return new StarCommand(new FindCommandParser().parse(args));
        }
        try {
            Index index = ParserUtil.parseIndex(args);
            return new StarCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, StarCommand.MESSAGE_USAGE), pe);
        }
    }
}
