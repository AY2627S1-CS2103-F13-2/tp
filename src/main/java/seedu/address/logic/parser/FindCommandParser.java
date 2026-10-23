package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFieldPredicate;
import seedu.address.model.person.ContactFieldPredicate.Field;
import seedu.address.model.person.Email;
import seedu.address.model.person.EventContainsKeywordsPredicate;
import seedu.address.model.person.FuzzyNamePredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/**
 * Parses input arguments and creates a new FindCommand object.
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a new FindCommand object.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw invalidFormat();
        }

        if (trimmedArgs.equals("/contact") || trimmedArgs.startsWith("/contact ")) {
            String phoneValue = trimmedArgs.substring("/contact".length()).trim();
            Phone phone = ParserUtil.parsePhone(phoneValue);
            return new FindCommand(new ContactFieldPredicate(Field.PHONE, phone.value));
        }

        if (trimmedArgs.equals("/email") || trimmedArgs.startsWith("/email ")) {
            String emailValue = trimmedArgs.substring("/email".length()).trim();
            Email email = ParserUtil.parseEmail(emailValue);
            return new FindCommand(new ContactFieldPredicate(Field.EMAIL, email.value));
        }

        if (trimmedArgs.equals("/event") || trimmedArgs.startsWith("/event ")) {
            String eventValue = trimmedArgs.substring("/event".length()).trim();

            if (eventValue.isEmpty()) {
                throw invalidFormat();
            }

            return new FindCommand(new EventContainsKeywordsPredicate(eventValue));
        }

        if (trimmedArgs.equals("/fuzzy") || trimmedArgs.startsWith("/fuzzy ")) {
            String nameValue = trimmedArgs.substring("/fuzzy".length()).trim();

            if (nameValue.isEmpty()) {
                throw invalidFormat();
            }

            return new FindCommand(new FuzzyNamePredicate(nameValue));
        }

        if (trimmedArgs.startsWith("/company") || trimmedArgs.startsWith("/role")) {
            return parseCompanyOrRoleSearch(trimmedArgs);
        }

        if (trimmedArgs.startsWith("/")) {
            throw invalidFormat();
        }

        String normalizedName = trimmedArgs.replaceAll("\\s+", " ");
        return new FindCommand(new NameContainsKeywordsPredicate(List.of(normalizedName)));
    }

    /**
     * Parses a company-only, role-only, or combined company-and-role search.
     */
    private FindCommand parseCompanyOrRoleSearch(String args) throws ParseException {
        String[] tokens = args.split("\\s+");
        String companyQuery = null;
        String roleQuery = null;

        int index = 0;
        while (index < tokens.length) {
            String selector = tokens[index];

            if (!selector.equals("/company") && !selector.equals("/role")) {
                throw invalidFormat();
            }

            index++;
            StringBuilder value = new StringBuilder();

            while (index < tokens.length && !tokens[index].startsWith("/")) {
                if (value.length() > 0) {
                    value.append(" ");
                }
                value.append(tokens[index]);
                index++;
            }

            if (value.length() == 0) {
                throw invalidFormat();
            }

            if (selector.equals("/company")) {
                if (companyQuery != null) {
                    throw invalidFormat();
                }
                companyQuery = value.toString();
            } else {
                if (roleQuery != null) {
                    throw invalidFormat();
                }
                roleQuery = value.toString();
            }
        }

        Predicate<Person> predicate = null;

        if (companyQuery != null) {
            predicate = new ContactFieldPredicate(Field.COMPANY, companyQuery);
        }

        if (roleQuery != null) {
            Predicate<Person> rolePredicate =
                    new ContactFieldPredicate(Field.ROLE, roleQuery);

            predicate = predicate == null
                    ? rolePredicate
                    : predicate.and(rolePredicate);
        }

        if (predicate == null) {
            throw invalidFormat();
        }

        return new FindCommand(predicate);
    }

    /**
     * Creates a parse exception for an invalid find command.
     */
    private ParseException invalidFormat() {
        return new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }
}
