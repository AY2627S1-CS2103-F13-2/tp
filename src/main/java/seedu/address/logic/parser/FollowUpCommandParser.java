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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses a displayed index, an action description and an optional trailing due date.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    public static final String MESSAGE_DATE_FORMAT =
            "Due date must be a valid date in yyyy-MM-dd, dd/MM/yyyy or d MMM yyyy format (English month names).";
    private static final Pattern FORMAT = Pattern.compile(
            "(?s)\\s*(?<index>\\S+)\\s+a/(?<description>.*?)(?:\\s+d/(?<dueDate>.*))?");
    private static final Pattern PREFIX = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT));

    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = FORMAT.matcher(args);
        Matcher prefixes = PREFIX.matcher(args);
        int actionCount = 0;
        int dateCount = 0;
        while (prefixes.find()) {
            if (prefixes.group().equals("a/")) {
                actionCount++;
            } else if (prefixes.group().equals("d/")) {
                dateCount++;
            } else {
                throw invalidFormat();
            }
        }
        if (!matcher.matches() || actionCount != 1 || dateCount > 1) {
            throw invalidFormat();
        }
        String description = matcher.group("description").strip();
        if (!FollowUp.isValidDescription(description)) {
            throw new ParseException(FollowUp.MESSAGE_CONSTRAINTS);
        }
        LocalDate dueDate = matcher.group("dueDate") == null ? null : parseDueDate(matcher.group("dueDate").strip());
        return new FollowUpCommand(ParserUtil.parseIndex(matcher.group("index")), description, dueDate);
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
