package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnstarCommand;

public class UnstarCommandParserTest {

    private final UnstarCommandParser parser = new UnstarCommandParser();

    @Test
    public void parse_validIndex_returnsUnstarCommand() {
        for (String input : List.of("1", " 1 ", "\t1\n")) {
            assertParseSuccess(parser, input, new UnstarCommand(INDEX_FIRST_PERSON));
        }
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnstarCommand.MESSAGE_USAGE);
        for (String input : List.of("", " ", "0", "-1", "+1", "1.5", "2147483648", "Alice", "1 2", "1 /name Bob")) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
