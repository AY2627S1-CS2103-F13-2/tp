package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteFollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class DeleteFollowUpCommandParserTest {
    private final DeleteFollowUpCommandParser parser = new DeleteFollowUpCommandParser();

    @Test
    void parse_validPair_acceptsWhitespaceAndStableId() throws Exception {
        assertEquals(new DeleteFollowUpCommand(Index.fromOneBased(2), 9), parser.parse(" 2  i/9 "));
        assertEquals(new DeleteFollowUpCommand(Index.fromOneBased(1), Integer.MAX_VALUE),
                new ContactFileParser().parseCommand("deletefollowup 1 i/2147483647"));
    }

    @Test
    void parse_invalidPair_rejectsWithoutIgnoringArguments() {
        for (String args : List.of("", "1", "i/2", "0 i/2", "-1 i/2", "1 i/0", "1 i/-2",
                "1 i/2147483648", "2147483648 i/2", "1 i/2 i/3", "1 i/2 a/Call", "1 i/2 extra",
                "1 2", "Alice i/2", "1 i/", "1 i/+2", "1 i/2.0", "1 i/ 2")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
