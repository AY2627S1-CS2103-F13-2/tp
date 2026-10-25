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

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditFollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses arguments for the editfollowup command.
 */
public class EditFollowUpCommandParser
        implements Parser<EditFollowUpCommand> {

    private static final Pattern PREFIX =
            Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern(
                    "dd/MM/uuuu",
                    Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMM uuuu")
                    .toFormatter(Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT));

    /**
     * Parses the arguments for an editfollowup command.
     *
     * @throws ParseException if the arguments are invalid
     */
    @Override
    public EditFollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);

        List<MatchResult> prefixes =
                PREFIX.matcher(args).results().toList();

        if (prefixes.isEmpty()) {
            throw invalidFormat();
        }

        String preamble =
                args.substring(0, prefixes.getFirst().start()).strip();

        Index targetIndex;
        try {
            targetIndex = ParserUtil.parseIndex(preamble);
        } catch (ParseException exception) {
            throw invalidFormat();
        }

        Integer followUpId = null;
        String description = null;
        LocalDate dueDate = null;
        boolean dueDateSpecified = false;

        for (int index = 0; index < prefixes.size(); index++) {
            MatchResult prefix = prefixes.get(index);
            int end = index + 1 < prefixes.size()
                    ? prefixes.get(index + 1).start()
                    : args.length();

            String value = args.substring(prefix.end(), end).strip();

            if ("i/".equals(prefix.group())) {
                if (followUpId != null || value.isBlank()) {
                    throw invalidFormat();
                }

                followUpId = parseFollowUpId(value);
            } else if ("a/".equals(prefix.group())) {
                if (description != null
                        || !FollowUp.isValidDescription(value)) {
                    throw new ParseException(
                            FollowUp.MESSAGE_CONSTRAINTS);
                }

                description = value;
            } else if ("d/".equals(prefix.group())) {
                if (dueDateSpecified) {
                    throw invalidFormat();
                }

                dueDateSpecified = true;
                dueDate = value.isBlank()
                        ? null
                        : parseDueDate(value);
            } else {
                throw invalidFormat();
            }
        }

        if (followUpId == null
                || (description == null && !dueDateSpecified)) {
            throw invalidFormat();
        }

        return new EditFollowUpCommand(
                targetIndex,
                followUpId,
                description,
                dueDate,
                dueDateSpecified);
    }

    /**
     * Parses a positive follow-up ID.
     */
    private static int parseFollowUpId(String value)
            throws ParseException {
        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw invalidFormat();
            }

            return id;
        } catch (NumberFormatException exception) {
            throw invalidFormat();
        }
    }

    /**
     * Parses a supported due-date format.
     */
    private static LocalDate parseDueDate(String value)
            throws ParseException {
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException exception) {
                // Try the next supported date format.
            }
        }

        throw new ParseException(
                FollowUpCommandParser.MESSAGE_DATE_FORMAT);
    }

    /**
     * Creates an invalid-format exception.
     */
    private static ParseException invalidFormat() {
        return new ParseException(
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditFollowUpCommand.MESSAGE_USAGE));
    }
}
