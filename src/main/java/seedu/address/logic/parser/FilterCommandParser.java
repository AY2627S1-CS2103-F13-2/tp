package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Locale;
import java.util.Set;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TagFilterPredicate;
import seedu.address.model.person.TagFilterPredicate.Mode;
import seedu.address.model.tag.Tag;

/**
 * Parses one optional matching mode and one or more individually validated tag values.
 */
public class FilterCommandParser implements Parser<FilterCommand> {
    @Override
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // Tags and mode values contain no whitespace; normalize separators for the shared tokenizer.
        String normalized = " " + args.trim().replaceAll("\\s+", " ");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalized, PREFIX_TAG, PREFIX_MODE);
        if (!arguments.getPreamble().isEmpty() || arguments.getAllValues(PREFIX_TAG).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_MODE);
        Mode mode = parseMode(arguments.getValue(PREFIX_MODE).orElse("and"));
        Set<Tag> tags = ParserUtil.parseTags(arguments.getAllValues(PREFIX_TAG));
        return new FilterCommand(new TagFilterPredicate(tags, mode));
    }

    private static Mode parseMode(String value) throws ParseException {
        try {
            return Mode.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ParseException("Matching mode must be 'and' or 'or'.\n" + FilterCommand.MESSAGE_USAGE);
        }
    }
}
