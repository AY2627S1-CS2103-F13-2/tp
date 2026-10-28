package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;

/**
 * Parses input arguments and creates a new LogCommand object.
 */
public class LogCommandParser implements Parser<LogCommand> {

    public static final String MESSAGE_FUTURE_DATE =
            "An interaction that has already happened cannot have a future date (%1$s).";

    private final Clock clock;

    /**
     * Creates a parser that treats the system clock's date as today.
     */
    public LogCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser that treats {@code clock}'s date as today.
     */
    public LogCommandParser(Clock clock) {
        requireNonNull(clock);
        this.clock = clock;
    }

    /**
     * Parses the given {@code String} of arguments in the context of the LogCommand
     * and returns a LogCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public LogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_NOTE);
        String preamble = argMultimap.getPreamble().trim();

        if (preamble.isEmpty() || argMultimap.getValue(PREFIX_NOTE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_NOTE);

        Interaction interaction = new Interaction(parseDate(argMultimap), parseNote(argMultimap));

        if (ParserUtil.isIndexInput(preamble)) {
            try {
                Index index = ParserUtil.parseIndex(preamble);
                return new LogCommand(index, interaction);
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE), pe);
            }
        }
        if (!Name.isValidName(preamble)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE));
        }
        return new LogCommand(StringUtil.normalise(preamble), interaction);
    }

    /**
     * Returns the date given with {@code PREFIX_DATE}, or today if there is none.
     * @throws ParseException if the date is not a real date in the accepted format, or is after today
     */
    private LocalDate parseDate(ArgumentMultimap argMultimap) throws ParseException {
        LocalDate today = LocalDate.now(clock);
        if (argMultimap.getValue(PREFIX_DATE).isEmpty()) {
            return today;
        }

        return parsePastDate(argMultimap.getValue(PREFIX_DATE).get(), today);
    }

    /**
     * Returns {@code dateText} as a date no later than {@code today}.
     * @throws ParseException if the date is not a real date in the accepted format, or is after today
     */
    static LocalDate parsePastDate(String dateText, LocalDate today) throws ParseException {
        String trimmedDate = dateText.trim();
        if (!Interaction.isValidDate(trimmedDate)) {
            throw new ParseException(Interaction.MESSAGE_DATE_CONSTRAINTS);
        }
        LocalDate date = LocalDate.parse(trimmedDate);
        if (date.isAfter(today)) {
            throw new ParseException(String.format(MESSAGE_FUTURE_DATE, date));
        }
        return date;
    }

    /**
     * Returns the note given with {@code PREFIX_NOTE}.
     * @throws ParseException if the note is blank
     */
    private static String parseNote(ArgumentMultimap argMultimap) throws ParseException {
        String note = argMultimap.getValue(PREFIX_NOTE).get();
        if (!Interaction.isValidNote(note)) {
            throw new ParseException(Interaction.MESSAGE_NOTE_CONSTRAINTS);
        }
        return note;
    }
}
