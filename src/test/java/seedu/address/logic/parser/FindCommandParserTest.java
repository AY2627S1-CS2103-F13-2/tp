package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.ContactFieldPredicate;
import seedu.address.model.person.ContactFieldPredicate.Field;
import seedu.address.model.person.Email;
import seedu.address.model.person.EventContainsKeywordsPredicate;
import seedu.address.model.person.FuzzyNamePredicate;
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
    public void parse_validCompany_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new ContactFieldPredicate(Field.COMPANY, "Google"));

        assertParseSuccess(parser, "/company Google", expectedFindCommand);
    }

    @Test
    public void parse_validRole_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new ContactFieldPredicate(Field.ROLE, "Engineer"));

        assertParseSuccess(parser, "/role Engineer", expectedFindCommand);
    }

    @Test
    public void parse_companyAndRole_doesNotThrowParseException() {
        assertDoesNotThrow(() -> parser.parse("/company Google /role Engineer"));
    }

    @Test
    public void parse_companyAndRoleWithMultipleWords_doesNotThrowParseException() {
        assertDoesNotThrow(() ->
                parser.parse("/company Google Singapore /role Software Engineer"));
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
    public void parse_missingCompany_throwsParseException() {
        assertParseFailure(parser, "/company",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingRole_throwsParseException() {
        assertParseFailure(parser, "/role",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateCompany_throwsParseException() {
        assertParseFailure(parser, "/company Google /company Apple",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateRole_throwsParseException() {
        assertParseFailure(parser, "/role Engineer /role Manager",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownSelector_throwsParseException() {
        assertParseFailure(parser, "/unknown Google",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownSelectorAfterCompany_throwsParseException() {
        assertParseFailure(parser, "/company Google /unknown value",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_eventSearch_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EventContainsKeywordsPredicate("NUS Career Fair"));

        assertParseSuccess(
                parser,
                "/event NUS Career Fair",
                expectedFindCommand);
    }

    @Test
    public void parse_eventSearchWithExtraSpaces_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(
                        new EventContainsKeywordsPredicate("NUS Career Fair"));

        assertParseSuccess(
                parser,
                " /event   NUS   Career   Fair ",
                expectedFindCommand);
    }

    @Test
    public void parse_eventSearchWithoutValue_throwsParseException() {
        assertParseFailure(
                parser,
                "/event",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validFuzzyName_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new FuzzyNamePredicate("Alicee"));

        assertParseSuccess(
                parser,
                "/fuzzy Alicee",
                expectedFindCommand);
    }

    @Test
    public void parse_fuzzyNameWithExtraSpaces_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new FuzzyNamePredicate("Alicee"));

        assertParseSuccess(
                parser,
                " /fuzzy   Alicee ",
                expectedFindCommand);
    }

    @Test
    public void parse_missingFuzzyName_throwsParseException() {
        assertParseFailure(
                parser,
                "/fuzzy",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        FindCommand.MESSAGE_USAGE));
    }
}
