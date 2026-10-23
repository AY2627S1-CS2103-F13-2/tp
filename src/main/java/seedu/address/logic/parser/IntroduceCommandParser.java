package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.IntroduceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;

/**
 * Parses arguments for {@code introduce}.
 */
public class IntroduceCommandParser implements Parser<IntroduceCommand> {

    @Override
    public IntroduceCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(IntroduceCommand.MESSAGE_NAME_REQUIRED);
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(trimmedArgs, PREFIX_INTRODUCER);
        if (argMultimap.getValue(PREFIX_INTRODUCER).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_INTRODUCER);

        String introducerInput = argMultimap.getValue(PREFIX_INTRODUCER).orElseThrow();
        if (introducerInput.isEmpty()) {
            throw new ParseException(IntroduceCommand.MESSAGE_NAME_REQUIRED);
        }

        String targetInput = argMultimap.getPreamble();
        Name parsedIntroducer = ParserUtil.parseName(introducerInput);

        if (ParserUtil.isIndexInput(targetInput)) {
            try {
                Index targetIndex = ParserUtil.parseIndex(targetInput);
                return new IntroduceCommand(targetIndex, parsedIntroducer, false);
            } catch (ParseException pe) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE), pe);
            }
        }

        if (!Name.isValidName(targetInput)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE));
        }
        return new IntroduceCommand(StringUtil.normalise(targetInput), parsedIntroducer, false);
    }
}
