package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     *
     * <p>An argument whose first word is a number is read as an index, as it is for {@code edit};
     * an explicit /contact or /email selector is resolved first, and other input is read as a name.
     * Bare digits therefore stay unambiguously an index.
     *
     * @throws ParseException if the selector or any supplied value is invalid.
     */
    public DeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        if (trimmedArgs.equals("/contact") || trimmedArgs.startsWith("/contact ")) {
            String value = trimmedArgs.substring("/contact".length()).trim();
            return new DeleteCommand(ParserUtil.parsePhone(value));
        }

        if (trimmedArgs.equals("/email") || trimmedArgs.startsWith("/email ")) {
            String value = trimmedArgs.substring("/email".length()).trim();
            if (value.isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
            }
            List<Email> emails = new ArrayList<>();
            for (String email : value.split("\\s+")) {
                emails.add(ParserUtil.parseEmail(email));
            }
            return new DeleteCommand(emails);
        }

        if (ParserUtil.isIndexInput(trimmedArgs)) {
            try {
                return new DeleteCommand(ParserUtil.parseIndex(trimmedArgs));
            } catch (ParseException pe) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
            }
        }

        if (!Name.isValidName(trimmedArgs)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        return new DeleteCommand(StringUtil.normalise(trimmedArgs));
    }

}
