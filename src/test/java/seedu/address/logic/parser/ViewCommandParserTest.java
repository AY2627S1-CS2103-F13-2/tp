package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validIndex_returnsViewCommand() {
        assertParseSuccess(parser, "1",
                new ViewCommand(Index.fromOneBased(1)));
    }

    @Test
    public void parse_validName_returnsViewCommand() {
        for (String name : List.of("Alice Pauline", "paULine", "Jean-Luc", "O'Brien", "A. Kumar", "李 明")) {
            assertParseSuccess(parser, name, new ViewCommand(name));
        }
    }

    @Test
    public void parse_paddedSelectors_normalizesWhitespace() {
        assertParseSuccess(parser, "  1  ", new ViewCommand(Index.fromOneBased(1)));
        assertParseSuccess(parser, "  aLiCe \t  pAuLiNe  ", new ViewCommand("aLiCe pAuLiNe"));
    }

    @Test
    public void parse_invalidSelectors_throwsParseException() {
        for (String selector : List.of("", " \t ", "0", "-1", "+1", "2147483648", "1 Alice",
                "1 2", "@#$", "n/Alice", "Alice t/friends", "-Alice", "Alice /email alice@example.com")) {
            assertParseFailure(parser, selector,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
