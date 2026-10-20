package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RecordCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@code record}.
 */
public class RecordCommandParser implements Parser<RecordCommand> {

    @Override
    public RecordCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        String[] parts = trimmedArgs.split("\\s+", 2);
        if (parts.length < 2) {
            throw new ParseException(RecordCommand.MESSAGE_NAME_REQUIRED);
        }

        Index targetIndex;
        try {
            targetIndex = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecordCommand.MESSAGE_USAGE), pe);
        }

        String introducerArgs = parts[1].trim();
        boolean confirmed = false;
        String confirmationSuffix = " " + RecordCommand.CONFIRM_OPTION;
        if (introducerArgs.endsWith(confirmationSuffix)) {
            confirmed = true;
            introducerArgs = introducerArgs.substring(0,
                    introducerArgs.length() - confirmationSuffix.length()).trim();
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + introducerArgs, PREFIX_NAME);
        if (!argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecordCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME);

        String introducerInput = argMultimap.getValue(PREFIX_NAME).orElseThrow();
        if (introducerInput.isEmpty()) {
            throw new ParseException(RecordCommand.MESSAGE_NAME_REQUIRED);
        }

        return new RecordCommand(targetIndex, ParserUtil.parseName(introducerInput), confirmed);
    }
}
