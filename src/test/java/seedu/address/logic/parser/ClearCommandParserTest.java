package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);

    private ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_noArgs_returnsClearCommand() {
        assertParseSuccess(parser, "", new ClearCommand());
    }

    @Test
    public void parse_whitespaceOnly_returnsClearCommand() {
        assertParseSuccess(parser, "   ", new ClearCommand());
    }

    @Test
    public void parse_extraArgs_throwsParseException() {
        assertParseFailure(parser, " 3", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " all", MESSAGE_INVALID_FORMAT);
    }

}
