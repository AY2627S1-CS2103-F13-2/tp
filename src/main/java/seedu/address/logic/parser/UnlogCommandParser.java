package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.UnlogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;

/**
 * Parses input arguments and creates a new UnlogCommand object.
 */
public class UnlogCommandParser implements Parser<UnlogCommand> {

    public static final String MESSAGE_INVALID_ENTRY =
            "The interaction number should be a positive whole number from the interaction history, e.g. i/1.";

    /**
     * Parses the given {@code String} of arguments in the context of the UnlogCommand
     * and returns an UnlogCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public UnlogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ENTRY);
        String preamble = argMultimap.getPreamble().trim();

        if (preamble.isEmpty() || argMultimap.getValue(PREFIX_ENTRY).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlogCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ENTRY);

        Index entry;
        try {
            entry = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_ENTRY).get());
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_ENTRY, pe);
        }

        if (ParserUtil.isIndexInput(preamble)) {
            try {
                return new UnlogCommand(ParserUtil.parseIndex(preamble), entry);
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlogCommand.MESSAGE_USAGE),
                        pe);
            }
        }
        if (!Name.isValidName(preamble)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlogCommand.MESSAGE_USAGE));
        }
        return new UnlogCommand(StringUtil.normalise(preamble), entry);
    }
}
