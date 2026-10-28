package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditFollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests the EditFollowUpCommandParser.
 */
public class EditFollowUpCommandParserTest {

    private final EditFollowUpCommandParser parser =
            new EditFollowUpCommandParser();

    @Test
    public void parse_descriptionAndDate_success() {
        EditFollowUpCommand expected =
                new EditFollowUpCommand(
                        Index.fromOneBased(1),
                        2,
                        "Call client",
                        LocalDate.of(2026, 11, 5),
                        true);

        assertParseSuccess(
                parser,
                "1 i/2 a/Call client d/2026-11-05",
                expected);
    }

    @Test
    public void parse_descriptionOnly_success() {
        EditFollowUpCommand expected =
                new EditFollowUpCommand(
                        Index.fromOneBased(1),
                        2,
                        "Updated description",
                        null,
                        false);

        assertParseSuccess(
                parser,
                "1 i/2 a/Updated description",
                expected);
    }

    @Test
    public void parse_dateOnly_success() {
        EditFollowUpCommand expected =
                new EditFollowUpCommand(
                        Index.fromOneBased(1),
                        2,
                        null,
                        LocalDate.of(2026, 11, 5),
                        true);

        for (String date : new String[] {"2026-11-05", "05/11/2026", "5 Nov 2026"}) {
            assertParseSuccess(parser, "1 i/2 d/" + date, expected);
        }
    }

    @Test
    public void parse_clearDate_success() {
        EditFollowUpCommand expected =
                new EditFollowUpCommand(
                        Index.fromOneBased(1),
                        2,
                        null,
                        null,
                        true);

        assertParseSuccess(
                parser,
                "1 i/2 d/",
                expected);
    }

    @Test
    public void parse_missingFollowUpId_failure() {
        assertParseFailure(
                parser,
                "1 a/Updated description",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditFollowUpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingEditedField_failure() {
        assertParseFailure(
                parser,
                "1 i/2",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditFollowUpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidFollowUpId_failure() {
        assertParseFailure(
                parser,
                "1 i/abc a/Updated description",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EditFollowUpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_blankDescription_failure() {
        assertThrows(ParseException.class, () -> parser.parse("1 i/2 a/"));
    }

    @Test
    public void parse_invalidDate_failure() {
        assertThrows(ParseException.class, () -> parser.parse("1 i/2 d/2026-02-30"));
        ParseException error = assertThrows(ParseException.class, () -> parser.parse("1 i/2 d/31/12/1999"));
        assertEquals(DateParserUtil.MESSAGE_DATE_TOO_OLD, error.getMessage());
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertThrows(ParseException.class, () -> parser.parse("1 i/2 a/First a/Second"));
    }
}
