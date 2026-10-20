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
    private static final Prefix[] SELECTION_PREFIXES = createSelectionPrefixes();

    @Override
    public DropCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ParsedArguments parsed = removeConfirmationOption(args.trim());
        Target target = parseTarget(parsed.arguments());
        List<DropCommand.FieldValue> values = parseValues(parsed.arguments().substring(target.fieldsStart()));
        return target.index() == null
                ? new DropCommand(target.name(), values, parsed.confirmed())
                : new DropCommand(target.index(), values, parsed.confirmed());
    }

    private static ParsedArguments removeConfirmationOption(String args) {
        String suffix = " " + DropCommand.CONFIRM_OPTION;
        boolean confirmed = args.endsWith(suffix);
        String arguments = confirmed ? args.substring(0, args.length() - suffix.length()).trim() : args;
        return new ParsedArguments(arguments, confirmed);
    }

    private static Target parseTarget(String args) throws ParseException {
        int fieldsStart = findFirstSelectionPrefix(args);
        if (fieldsStart < 0) {
            throw invalidFormat();
        }
        String identity = args.substring(0, fieldsStart).trim();
        if (identity.isEmpty()) {
            throw invalidFormat();
        }
        if (ParserUtil.isIndexInput(identity)) {
            return new Target(parseIndex(identity), null, fieldsStart);
        }
        if (!Name.isValidName(identity)) {
            throw invalidFormat();
        }
        return new Target(null, StringUtil.normalise(identity), fieldsStart);
    }

    private static Index parseIndex(String identity) throws ParseException {
        try {
            return ParserUtil.parseIndex(identity);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DropCommand.MESSAGE_USAGE), pe);
        }
    }

    private static List<DropCommand.FieldValue> parseValues(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args, SELECTION_PREFIXES);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw invalidFormat();
        }
        if (!argMultimap.getAllValues(CliSyntax.PREFIX_NAME).isEmpty()) {
            throw new ParseException("The contact name cannot be removed.");
        }
        return parseFieldValues(argMultimap);
    }

    private static List<DropCommand.FieldValue> parseFieldValues(ArgumentMultimap argMultimap)
            throws ParseException {
        List<DropCommand.FieldValue> values = new ArrayList<>();
        for (DropCommand.Field field : DropCommand.Field.values()) {
            if (field != DropCommand.Field.TAG) {
                argMultimap.verifyNoDuplicatePrefixesFor(field.getPrefix());
            }
            for (String value : argMultimap.getAllValues(field.getPrefix())) {
                if (value.isEmpty()) {
                    throw invalidFormat();
                }
                values.add(parseValue(field, value));
            }
        }
        return values;
    }

    private static int findFirstSelectionPrefix(String args) {
        int first = -1;
        for (Prefix prefix : SELECTION_PREFIXES) {
            int current = args.indexOf(" " + prefix);
            if (current >= 0 && (first < 0 || current < first)) {
                first = current + 1;
            }
        }
        return first;
    }

    private static Prefix[] createSelectionPrefixes() {
        Prefix[] prefixes = new Prefix[FIELD_PREFIXES.length + 1];
        System.arraycopy(FIELD_PREFIXES, 0, prefixes, 0, FIELD_PREFIXES.length);
        prefixes[FIELD_PREFIXES.length] = CliSyntax.PREFIX_NAME;
        return prefixes;
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

    private record ParsedArguments(String arguments, boolean confirmed) {}

    private record Target(Index index, String name, int fieldsStart) {}
}
