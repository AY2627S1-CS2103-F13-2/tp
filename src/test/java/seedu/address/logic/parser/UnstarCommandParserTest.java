package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnstarCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class UnstarCommandParserTest {

    private final UnstarCommandParser parser = new UnstarCommandParser();

    @Test
    public void parse_validIndex_returnsUnstarCommand() {
        for (String input : List.of("1", " 1 ", "\t1\n")) {
            assertParseSuccess(parser, input, new UnstarCommand(INDEX_FIRST_PERSON));
        }
    }

    @Test
    public void parse_queries_reusesFindParser() throws Exception {
        for (String query : List.of("Alice", "  aLiCe   Pauline ", "/email alice@example.com",
                "/contact 94351253")) {
            assertParseSuccess(parser, query,
                    new UnstarCommand(new FindCommandParser().parse(query)));
        }
    }

    @Test
    public void parse_invalidQueries_returnsFindFeedback() {
        for (String query : List.of("/unknown Alice", "/email invalid", "/contact invalid", "/email", "/contact")) {
            ParseException expected = assertThrows(ParseException.class, () -> new FindCommandParser().parse(query));
            assertParseFailure(parser, query, expected.getMessage());
        }
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnstarCommand.MESSAGE_USAGE);
        for (String input : List.of("", " ", "0", "-1", "+1", "1.5", "2147483648", "1 2", "1 /name Bob")) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
