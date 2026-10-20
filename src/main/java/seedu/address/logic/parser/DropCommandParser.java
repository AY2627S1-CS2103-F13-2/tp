package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.DropCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;

/**
 * Parses input arguments and creates a {@code DropCommand}.
 */
public class DropCommandParser implements Parser<DropCommand> {
    private static final Prefix[] FIELD_PREFIXES = {
        CliSyntax.PREFIX_COMPANY, CliSyntax.PREFIX_ROLE, CliSyntax.PREFIX_PHONE,
        CliSyntax.PREFIX_EMAIL, CliSyntax.PREFIX_LINKEDIN, CliSyntax.PREFIX_TAG,
        CliSyntax.PREFIX_INTRODUCER
    };

    @Override
    public DropCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        boolean confirmed = trimmedArgs.endsWith(" " + DropCommand.CONFIRM_OPTION);
        if (confirmed) {
            trimmedArgs = trimmedArgs.substring(0,
                    trimmedArgs.length() - DropCommand.CONFIRM_OPTION.length()).trim();
        }

        int fieldsStart = findFirstFieldPrefix(trimmedArgs);
        if (fieldsStart < 0) {
            throw invalidFormat();
        }
        String identity = trimmedArgs.substring(0, fieldsStart).trim();
        String fieldArgs = trimmedArgs.substring(fieldsStart).trim();
        if (identity.isEmpty()) {
            throw invalidFormat();
        }

        Index index = null;
        String name = null;
        if (ParserUtil.isIndexInput(identity)) {
            try {
                index = ParserUtil.parseIndex(identity);
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DropCommand.MESSAGE_USAGE), pe);
            }
        } else {
            if (!Name.isValidName(identity)) {
                throw invalidFormat();
            }
            name = StringUtil.normalise(identity);
        }

        Prefix[] allPrefixes = new Prefix[FIELD_PREFIXES.length + 1];
        System.arraycopy(FIELD_PREFIXES, 0, allPrefixes, 0, FIELD_PREFIXES.length);
        allPrefixes[FIELD_PREFIXES.length] = CliSyntax.PREFIX_NAME;
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + fieldArgs, allPrefixes);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw invalidFormat();
        }
        if (!argMultimap.getAllValues(CliSyntax.PREFIX_NAME).isEmpty()) {
            throw new ParseException("The contact name cannot be removed.");
        }

        List<DropCommand.FieldValue> values = new ArrayList<>();
        for (DropCommand.Field field : DropCommand.Field.values()) {
            List<String> fieldValues = argMultimap.getAllValues(field.getPrefix());
            if (field != DropCommand.Field.TAG && fieldValues.size() > 1) {
                argMultimap.verifyNoDuplicatePrefixesFor(field.getPrefix());
            }
            for (String value : fieldValues) {
                if (value.isEmpty()) {
                    throw invalidFormat();
                }
                values.add(parseValue(field, value));
            }
        }
        if (values.isEmpty()) {
            throw invalidFormat();
        }

        return index == null
                ? new DropCommand(name, values, confirmed)
                : new DropCommand(index, values, confirmed);
    }

    private static int findFirstFieldPrefix(String args) {
        int first = -1;
        for (Prefix prefix : FIELD_PREFIXES) {
            int current = args.indexOf(" " + prefix);
            if (current >= 0 && (first < 0 || current < first)) {
                first = current + 1;
            }
        }
        int namePrefix = args.indexOf(" " + CliSyntax.PREFIX_NAME);
        if (namePrefix >= 0 && (first < 0 || namePrefix < first)) {
            first = namePrefix + 1;
        }
        return first;
    }

    private static DropCommand.FieldValue parseValue(DropCommand.Field field, String value) throws ParseException {
        Object parsed = switch (field) {
            case COMPANY -> ParserUtil.parseCompany(value);
            case ROLE -> ParserUtil.parseRole(value);
            case PHONE -> ParserUtil.parsePhone(value);
            case EMAIL -> ParserUtil.parseEmail(value);
            case LINKEDIN -> ParserUtil.parseLinkedin(value);
            case TAG -> ParserUtil.parseTag(value);
            case INTRODUCER -> ParserUtil.parseName(value);
        };
        return new DropCommand.FieldValue(field, parsed);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DropCommand.MESSAGE_USAGE));
    }
}
