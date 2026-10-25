package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINKEDIN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object.
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given arguments in the context of the AddCommand.
     *
     * @throws ParseException if the input is invalid
     */
    public AddCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                args,
                PREFIX_NAME,
                PREFIX_COMPANY,
                PREFIX_ROLE,
                PREFIX_PHONE,
                PREFIX_EMAIL,
                PREFIX_LINKEDIN,
                PREFIX_INTRODUCER,
                PREFIX_TAG,
                PREFIX_EVENT_NAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw invalidFormat();
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME,
                PREFIX_COMPANY,
                PREFIX_ROLE,
                PREFIX_PHONE,
                PREFIX_EMAIL,
                PREFIX_LINKEDIN,
                PREFIX_INTRODUCER,
                PREFIX_EVENT_NAME);

        Name name = ParserUtil.parseName(
                argMultimap.getValue(PREFIX_NAME).get());

        Company company = null;
        if (argMultimap.getValue(PREFIX_COMPANY).isPresent()) {
            company = ParserUtil.parseCompany(
                    argMultimap.getValue(PREFIX_COMPANY).get());
        }

        Role role = null;
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            role = ParserUtil.parseRole(
                    argMultimap.getValue(PREFIX_ROLE).get());
        }

        Phone phone = null;
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            phone = ParserUtil.parsePhone(
                    argMultimap.getValue(PREFIX_PHONE).get());
        }

        Email email = null;
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            email = ParserUtil.parseEmail(
                    argMultimap.getValue(PREFIX_EMAIL).get());
        }

        Linkedin linkedin = null;
        if (argMultimap.getValue(PREFIX_LINKEDIN).isPresent()) {
            linkedin = ParserUtil.parseLinkedin(
                    argMultimap.getValue(PREFIX_LINKEDIN).get());
        }

        Name introducer = null;
        if (argMultimap.getValue(PREFIX_INTRODUCER).isPresent()) {
            introducer = ParserUtil.parseName(
                    argMultimap.getValue(PREFIX_INTRODUCER).get());
        }

        Set<Tag> tagList = ParserUtil.parseTags(
                argMultimap.getAllValues(PREFIX_TAG));

        Set<Event> events = Set.of();
        if (argMultimap.getValue(PREFIX_EVENT_NAME).isPresent()) {
            String eventName = argMultimap.getValue(PREFIX_EVENT_NAME).get();

            try {
                events = Set.of(new Event(eventName));
            } catch (IllegalArgumentException exception) {
                throw new ParseException(
                        String.format(
                                MESSAGE_INVALID_COMMAND_FORMAT,
                                AddCommand.MESSAGE_USAGE),
                        exception);
            }
        }

        Person person = new Person(
                name,
                company,
                role,
                phone,
                email,
                linkedin,
                introducer,
                tagList,
                events,
                List.of(),
                false,
                List.of());

        return new AddCommand(person);
    }

    /**
     * Returns true if all required prefixes are present.
     */
    private static boolean arePrefixesPresent(
            ArgumentMultimap argumentMultimap,
            Prefix... prefixes) {
        return Stream.of(prefixes)
                .allMatch(prefix ->
                        argumentMultimap.getValue(prefix).isPresent());
    }

    /**
     * Creates an invalid-format parse exception.
     */
    private static ParseException invalidFormat() {
        return new ParseException(
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        AddCommand.MESSAGE_USAGE));
    }
}
