package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PendingFollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

class PendingFollowUpsCommandParserTest {
    private final PendingFollowUpsCommandParser parser = new PendingFollowUpsCommandParser();

    @Test
    void parse_optionalSelector_acceptsWhitespaceAndLetterCase() throws Exception {
        Map<String, String> expected = Map.of("", PendingFollowUpsCommand.MESSAGE_EMPTY,
                "overdue", "No overdue follow-ups remain.", "today", "No follow-ups are due today.",
                "upcoming", "No upcoming follow-ups remain.");
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), parser.parse(" \t" + entry.getKey() + " \t")
                    .execute(new ModelManager()).getFeedbackToUser());
        }
        assertEquals("No follow-ups are due today.",
                parser.parse("ToDaY").execute(new ModelManager()).getFeedbackToUser());
    }

    @Test
    void parse_unknownOrExtraArguments_showsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PendingFollowUpsCommand.MESSAGE_USAGE);
        for (String input : List.of("unknown", "today upcoming", "today today", "upcoming extra", "1", "d/DATE")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input));
            assertEquals(expected, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
