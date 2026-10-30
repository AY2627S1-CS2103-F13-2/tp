package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINKEDIN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.EditCommand.RemovableField;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object.
 */
public class EditCommandParser implements Parser<EditCommand> {

    public static final String MESSAGE_UNSUPPORTED_PREFIX = "Unsupported prefix for edit: %1$s.";
    public static final String MESSAGE_INTRODUCER_PREFIX =
            "The i/ prefix is not supported by edit. Use introduce NAME i/INTRODUCER to change an introducer.";

    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)[A-Za-z][A-Za-z0-9_-]*/");
    private static final Prefix[] SUPPORTED_PREFIXES = {
        PREFIX_NAME, PREFIX_COMPANY, PREFIX_ROLE, PREFIX_PHONE,
        PREFIX_EMAIL, PREFIX_LINKEDIN, PREFIX_TAG, PREFIX_EVENT_NAME
    };

    /**
     * Parses the given arguments in the context of the EditCommand.
     *
     * @throws ParseException if the user input is invalid
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        rejectUnsupportedPrefixes(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, SUPPORTED_PREFIXES);

        String preamble = argMultimap.getPreamble().trim();
        Index index = null;
        String targetName = null;

        if (ParserUtil.isIndexInput(preamble)) {
            try {
                index = ParserUtil.parseIndex(preamble);
            } catch (ParseException exception) {
                throw invalidFormat(exception);
            }
        } else if (Name.isValidName(preamble)) {
            targetName = preamble;
        } else {
            throw invalidFormat();
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME,
                PREFIX_COMPANY,
                PREFIX_ROLE,
                PREFIX_PHONE,
                PREFIX_EMAIL,
                PREFIX_LINKEDIN,
                PREFIX_EVENT_NAME);

        EditPersonDescriptor descriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(
                    ParserUtil.parseName(
                            argMultimap.getValue(PREFIX_NAME).get()));
        }

        if (isEmptyValue(argMultimap, PREFIX_COMPANY)) {
            descriptor.removeField(RemovableField.COMPANY);
        } else if (argMultimap.getValue(PREFIX_COMPANY).isPresent()) {
            descriptor.setCompany(
                    ParserUtil.parseCompany(
                            argMultimap.getValue(PREFIX_COMPANY).get()));
        }

        if (isEmptyValue(argMultimap, PREFIX_ROLE)) {
            descriptor.removeField(RemovableField.ROLE);
        } else if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            descriptor.setRole(
                    ParserUtil.parseRole(
                            argMultimap.getValue(PREFIX_ROLE).get()));
        }

        if (isEmptyValue(argMultimap, PREFIX_PHONE)) {
            descriptor.removeField(RemovableField.PHONE);
        } else if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            descriptor.setPhone(
                    ParserUtil.parsePhone(
                            argMultimap.getValue(PREFIX_PHONE).get()));
        }

        if (isEmptyValue(argMultimap, PREFIX_EMAIL)) {
            descriptor.removeField(RemovableField.EMAIL);
        } else if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            descriptor.setEmail(
                    ParserUtil.parseEmail(
                            argMultimap.getValue(PREFIX_EMAIL).get()));
        }

        if (isEmptyValue(argMultimap, PREFIX_LINKEDIN)) {
            descriptor.removeField(RemovableField.LINKEDIN);
        } else if (argMultimap.getValue(PREFIX_LINKEDIN).isPresent()) {
            descriptor.setLinkedin(
                    ParserUtil.parseLinkedin(
                            argMultimap.getValue(PREFIX_LINKEDIN).get()));
        }

        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG))
                .ifPresent(descriptor::setTags);

        if (argMultimap.getValue(PREFIX_EVENT_NAME).isPresent()) {
            String eventName =
                    argMultimap.getValue(PREFIX_EVENT_NAME).get();

            if (eventName.isBlank()) {
                throw invalidFormat();
            }

            descriptor.setEvent(new Event(eventName));
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return index != null
                ? new EditCommand(index, descriptor)
                : new EditCommand(targetName, descriptor);
    }

    /**
     * Identifies unsupported prefixes before they can be mistaken for a selector or a field value.
     */
    private static void rejectUnsupportedPrefixes(String args) throws ParseException {
        Matcher matcher = FIELD_PREFIX.matcher(args);
        while (matcher.find()) {
            String prefix = matcher.group();
            if (Arrays.stream(SUPPORTED_PREFIXES).noneMatch(known -> known.getPrefix().equals(prefix))) {
                throw new ParseException(prefix.equals("i/") ? MESSAGE_INTRODUCER_PREFIX
                        : String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix));
            }
        }
    }

    /**
     * Returns true if a prefix is given with no value after it.
     */
    private static boolean isEmptyValue(
            ArgumentMultimap argMultimap,
            Prefix prefix) {
        return argMultimap.getValue(prefix)
                .map(String::isBlank)
                .orElse(false);
    }

    /**
     * Parses tags into a set.
     */
    private Optional<Set<Tag>> parseTagsForEdit(
            Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }

        Collection<String> tagSet =
                tags.size() == 1 && tags.contains("")
                        ? Collections.emptySet()
                        : tags;

        return Optional.of(ParserUtil.parseTags(tagSet));
    }

    /**
     * Creates an invalid-format exception.
     */
    private ParseException invalidFormat() {
        return new ParseException(
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditCommand.MESSAGE_USAGE));
    }

    /**
     * Creates an invalid-format exception with a cause.
     */
    private ParseException invalidFormat(Throwable cause) {
        return new ParseException(
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditCommand.MESSAGE_USAGE),
                cause);
    }
}
