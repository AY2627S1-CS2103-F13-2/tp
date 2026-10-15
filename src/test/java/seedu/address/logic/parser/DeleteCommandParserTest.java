package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validIndex_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_validName_returnsDeleteCommand() {
        assertParseSuccess(parser, "Alice Pauline", new DeleteCommand("Alice Pauline"));
    }

    @Test
    public void parse_nameWithSurroundingAndRepeatedWhitespace_normalisesName() {
        assertParseSuccess(parser, "   Alice    Pauline  ", new DeleteCommand("Alice Pauline"));
    }

    @Test
    public void parse_partialName_returnsDeleteCommand() {
        // Whether a partial name identifies exactly one contact is for the resolver to decide at
        // execution time, so the parser accepts it.
        assertParseSuccess(parser, "Alice", new DeleteCommand("Alice"));
    }

    @Test
    public void parse_nameWithPunctuationAllowedInNames_returnsDeleteCommand() {
        assertParseSuccess(parser, "Jean-Luc O'Brien", new DeleteCommand("Jean-Luc O'Brien"));
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexWithTrailingText_throwsParseException() {
        // A leading number means the user meant an index, so this is a malformed index, not a name.
        assertParseFailure(parser, "1 abc", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonPositiveIndex_throwsParseException() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidName_throwsParseException() {
        assertParseFailure(parser, "@#$", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-Alice", MESSAGE_INVALID_FORMAT);
    }
}
