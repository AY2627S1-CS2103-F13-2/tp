package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validRemark_success() {
        assertParseSuccess(parser, "1 r/Likes to swim",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim")));
        assertParseSuccess(parser, "1 r/", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidArguments_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String args : new String[] {"", "1", "0 r/Test", "-1 r/Test", "abc r/Test", "1 extra r/Test"}) {
            assertParseFailure(parser, args, message);
        }
        assertParseFailure(parser, "1 r/First r/Second",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_REMARK));
        assertParseFailure(parser, "1 r/First\nSecond", Remark.MESSAGE_CONSTRAINTS);
    }
}
