package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class FollowUpCommandParserTest {
    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    @Test
    void parse_validDescription_preservesPunctuationAndInternalSpacing() throws Exception {
        assertEquals(new FollowUpCommand(Index.fromOneBased(2), "Send  portfolio: https://example.com/a/b"),
                parser.parse(" 2 a/  Send  portfolio: https://example.com/a/b  "));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Call"), parser.parse("\t1\ta/Call"));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Send portfolio"),
                new ContactFileParser().parseCommand("followup 1 a/Send portfolio"));
    }

    @Test
    void parse_malformedArguments_rejectsWithoutIgnoringPrefixes() {
        for (String input : List.of("", "1", "a/Call", "1 a/", "1 a/ ", "0 a/Call", "-1 a/Call",
                "1.5 a/Call", "999999999999999 a/Call", "Alice a/Call", "1 extra a/Call", "1a/Call",
                "1 a/Call a/Send", "1 a/Call p/12345", "1 a/Call confirm/yes", "1 a/Call x/value",
                "1 n/Alice a/Call", "1 A/Call")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
