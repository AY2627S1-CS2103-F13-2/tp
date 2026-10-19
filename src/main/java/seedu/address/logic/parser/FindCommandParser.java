package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FindNotesCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ContactFieldPredicate;
import seedu.address.model.person.ContactFieldPredicate.Field;
import seedu.address.model.person.Email;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NoteContainsKeywordsPredicate;
import seedu.address.model.person.Phone;

/**
 * Parses input arguments and creates a new FindCommand object.
 */
public class FindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_NO_NOTE_KEYWORDS = "Please give at least one keyword to search the notes for, "
            + "e.g. find /notes internship";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
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

        if (trimmedArgs.equals("/notes") || trimmedArgs.startsWith("/notes ")) {
            String keywords = trimmedArgs.substring("/notes".length()).trim();
            if (keywords.isEmpty()) {
                throw new ParseException(MESSAGE_NO_NOTE_KEYWORDS);
            }
            return new FindNotesCommand(new NoteContainsKeywordsPredicate(List.of(keywords.split("\\s+"))));
        }

        if (trimmedArgs.startsWith("/")) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        String normalizedName = trimmedArgs.replaceAll("\\s+", " ");
        return new FindCommand(new NameContainsKeywordsPredicate(List.of(normalizedName)));
    }
}
