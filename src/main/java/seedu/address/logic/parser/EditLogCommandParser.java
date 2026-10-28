package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;

/**
 * Parses input arguments and creates a new EditLogCommand object.
 */
public class EditLogCommandParser implements Parser<EditLogCommand> {

    private final Clock clock;

    /**
     * Creates a parser that treats the system clock's date as today.
     */
    public EditLogCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser that treats {@code clock}'s date as today.
     */
    public EditLogCommandParser(Clock clock) {
        requireNonNull(clock);
        this.clock = clock;
    }

    /**
     * Parses the given {@code String} of arguments in the context of the EditLogCommand
     * and returns an EditLogCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public EditLogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ENTRY, PREFIX_DATE, PREFIX_NOTE);
        String preamble = argMultimap.getPreamble().trim();

        if (preamble.isEmpty() || argMultimap.getValue(PREFIX_ENTRY).isEmpty()
                || (argMultimap.getValue(PREFIX_DATE).isEmpty() && argMultimap.getValue(PREFIX_NOTE).isEmpty())) {
            throw invalidFormat();
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ENTRY, PREFIX_DATE, PREFIX_NOTE);

        Index entry;
        try {
            entry = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_ENTRY).get());
        } catch (ParseException pe) {
            throw new ParseException(UnlogCommandParser.MESSAGE_INVALID_ENTRY, pe);
        }

        LocalDate date = argMultimap.getValue(PREFIX_DATE).isEmpty() ? null
                : LogCommandParser.parsePastDate(argMultimap.getValue(PREFIX_DATE).get(), LocalDate.now(clock));
        String note = argMultimap.getValue(PREFIX_NOTE).orElse(null);
        if (note != null && !Interaction.isValidNote(note)) {
            throw new ParseException(Interaction.MESSAGE_NOTE_CONSTRAINTS);
        }

        if (ParserUtil.isIndexInput(preamble)) {
            try {
                return new EditLogCommand(ParserUtil.parseIndex(preamble), entry, date, note);
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLogCommand.MESSAGE_USAGE),
                        pe);
            }
        }
        if (!Name.isValidName(preamble)) {
            throw invalidFormat();
        }
        return new EditLogCommand(StringUtil.normalise(preamble), entry, date, note);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLogCommand.MESSAGE_USAGE));
    }
}
