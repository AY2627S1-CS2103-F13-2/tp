package seedu.address.logic.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses supported command date formats and enforces their lower date bound.
 */
final class DateParserUtil {
    static final LocalDate MINIMUM_DATE = LocalDate.of(2000, 1, 1);
    static final String MESSAGE_DATE_TOO_OLD = "Date must be on or after 2000-01-01.";

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT));

    private DateParserUtil() {
    }

    /**
     * Parses a date in one of the supported formats and enforces the minimum date.
     *
     * @throws ParseException if the value is invalid or earlier than the minimum date
     */
    static LocalDate parseDate(String value, String formatErrorMessage) throws ParseException {
        LocalDate date = null;
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                date = LocalDate.parse(value, format);
                break;
            } catch (DateTimeParseException exception) {
                // Try the next supported format.
            }
        }
        if (date == null) {
            throw new ParseException(formatErrorMessage);
        }
        if (date.isBefore(MINIMUM_DATE)) {
            throw new ParseException(MESSAGE_DATE_TOO_OLD);
        }
        return date;
    }
}
