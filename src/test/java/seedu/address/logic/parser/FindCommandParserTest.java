package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.ContactFieldPredicate;
import seedu.address.model.person.ContactFieldPredicate.Field;
import seedu.address.model.person.Email;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Phone;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice Bob")));

        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);
    }

    @Test
    public void parse_nameWithExtraSpaces_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice Bob")));

        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validPhone_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new ContactFieldPredicate(Field.PHONE, "91234567"));

        assertParseSuccess(parser, "/contact 91234567", expectedFindCommand);
    }

    @Test
    public void parse_validEmail_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new ContactFieldPredicate(Field.EMAIL, "alice@example.com"));

        assertParseSuccess(parser, "/email alice@example.com", expectedFindCommand);
    }

    @Test
    public void parse_missingPhone_throwsParseException() {
        assertParseFailure(parser, "/contact", Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidPhone_throwsParseException() {
        assertParseFailure(parser, "/contact abc", Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingEmail_throwsParseException() {
        assertParseFailure(parser, "/email", Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidEmail_throwsParseException() {
        assertParseFailure(parser, "/email invalid-email", Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownSelector_throwsParseException() {
        assertParseFailure(parser, "/company Google",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }
}
