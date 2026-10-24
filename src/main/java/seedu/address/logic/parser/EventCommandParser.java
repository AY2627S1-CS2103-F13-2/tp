package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Event;

/**
 * Parses arguments for the event command.
 */
public class EventCommandParser implements Parser<EventCommand> {

    /**
     * Parses event command arguments.
     *
     * @throws ParseException if the arguments are invalid
     */
    @Override
    public EventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_EVENT_NAME, PREFIX_EVENT_DATE);

        String preamble = argMultimap.getPreamble().trim();
        String[] preambleParts = preamble.split("\\s+");

        if (preambleParts.length != 2 || !preambleParts[0].equals("add")) {
            throw invalidFormat();
        }

        Index targetIndex;
        try {
            targetIndex = ParserUtil.parseIndex(preambleParts[1]);
        } catch (ParseException exception) {
            throw invalidFormat(exception);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_EVENT_NAME, PREFIX_EVENT_DATE);

        if (argMultimap.getValue(PREFIX_EVENT_NAME).isEmpty()
                || argMultimap.getValue(PREFIX_EVENT_DATE).isEmpty()) {
            throw invalidFormat();
        }

        String eventName = argMultimap.getValue(PREFIX_EVENT_NAME).get();
        String eventDate = argMultimap.getValue(PREFIX_EVENT_DATE).get();

        try {
            Event event = new Event(eventName, eventDate);
            return new EventCommand(targetIndex, event);
        } catch (IllegalArgumentException exception) {
            throw invalidFormat(exception);
        }
    }

    /**
     * Creates an invalid-format parse exception.
     */
    private ParseException invalidFormat() {
        return new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EventCommand.MESSAGE_USAGE));
    }

    /**
     * Creates an invalid-format parse exception with a cause.
     */
    private ParseException invalidFormat(Throwable cause) {
        return new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EventCommand.MESSAGE_USAGE),
                cause);
    }
}
