package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnlogCommand;

public class UnlogCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlogCommand.MESSAGE_USAGE);

    private final UnlogCommandParser parser = new UnlogCommandParser();

    @Test
    public void parse_indexAndEntry_success() {
        assertParseSuccess(parser, " 1 i/2", new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)));
    }

    @Test
    public void parse_nameAndEntry_success() {
        assertParseSuccess(parser, "  john   lim  i/ 1 ", new UnlogCommand("john lim", Index.fromOneBased(1)));
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, " i/1", INVALID_FORMAT);
        assertParseFailure(parser, " 1", INVALID_FORMAT);
        assertParseFailure(parser, "", INVALID_FORMAT);
    }

    @Test
    public void parse_invalidEntry_failure() {
        assertParseFailure(parser, " 1 i/0", UnlogCommandParser.MESSAGE_INVALID_ENTRY);
        assertParseFailure(parser, " 1 i/two", UnlogCommandParser.MESSAGE_INVALID_ENTRY);
        assertParseFailure(parser, " 1 i/", UnlogCommandParser.MESSAGE_INVALID_ENTRY);
    }

    @Test
    public void parse_invalidPerson_failure() {
        assertParseFailure(parser, " 0 i/1", INVALID_FORMAT);
        assertParseFailure(parser, " John@Lim i/1", INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateEntry_failure() {
        assertParseFailure(parser, " 1 i/1 i/2", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ENTRY));
    }
}
