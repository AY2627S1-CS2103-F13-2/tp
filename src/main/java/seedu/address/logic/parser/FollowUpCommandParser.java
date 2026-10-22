package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses a displayed index, an action description and an optional due date in either parameter order.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    public static final String MESSAGE_DATE_FORMAT =
            "Due date must be a valid date in yyyy-MM-dd, dd/MM/yyyy or d MMM yyyy format (English month names).";
    private static final Pattern PREFIX = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT));

    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<MatchResult> prefixes = PREFIX.matcher(args).results().toList();
        if (prefixes.isEmpty()) {
            throw invalidFormat();
        }
        String description = null;
        String date = null;
        for (int i = 0; i < prefixes.size(); i++) {
            MatchResult prefix = prefixes.get(i);
            int end = i + 1 < prefixes.size() ? prefixes.get(i + 1).start() : args.length();
            String value = args.substring(prefix.end(), end).strip();
            if (prefix.group().equals("a/") && description == null) {
                description = value;
            } else if (prefix.group().equals("d/") && date == null) {
                date = value;
            } else {
                throw invalidFormat();
            }
        }
        if (description == null) {
            throw invalidFormat();
        }
        if (!FollowUp.isValidDescription(description)) {
            throw new ParseException(FollowUp.MESSAGE_CONSTRAINTS);
        }
        LocalDate dueDate = date == null ? null : parseDueDate(date);
        return new FollowUpCommand(ParserUtil.parseIndex(args.substring(0, prefixes.getFirst().start())),
                description, dueDate);
    }

    private static LocalDate parseDueDate(String value) throws ParseException {
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException e) {
                // Try the next supported format; strict parsing rejects impossible dates.
            }
        }
        throw new ParseException(MESSAGE_DATE_FORMAT);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
    }
}
