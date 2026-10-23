package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class FollowUpCommandParserTest {
    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    @Test
    void parse_supportedDates_producesSameCommand() throws Exception {
        FollowUpCommand expected = new FollowUpCommand(Index.fromOneBased(1), "Call", LocalDate.of(2026, 10, 25));
        for (String date : List.of("2026-10-25", "25/10/2026", "25 Oct 2026", "25 oCT 2026")) {
            assertEquals(expected, parser.parse("1 a/Call d/" + date));
            assertEquals(expected, parser.parse("1 d/" + date + " a/Call"));
        }
        assertEquals(expected, parser.parse("\t1\ta/ Call \td/ 25 Oct 2026  "));
        assertEquals(expected, parser.parse("\t1\td/ 25 Oct 2026 \ta/ Call  "));
        assertEquals(expected, new ContactFileParser().parseCommand("followup 1 d/25/10/2026 a/Call"));
        assertEquals(expected, new ContactFileParser().parseCommand("followup 1 a/Call d/25/10/2026"));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Call", LocalDate.of(2028, 2, 29)),
                parser.parse("1 a/Call d/29/02/2028"));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Call", LocalDate.of(2026, 1, 5)),
                parser.parse("1 a/Call d/5 Jan 2026"));
    }

    @Test
    void parse_invalidDates_reportsSupportedFormats() {
        for (String date : List.of("", " ", "2026-02-29", "31/04/2026", "29 Feb 2026", "2026-13-01",
                "00/10/2026", "25/00/2026", "2026-10-00", "25 Foo 2026", "25 October 2026",
                "10/25/2026", "2026-1-5", "5/1/2026", "2026-10-25T12:00:00", "tomorrow", "25 Oct 26")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse("1 a/Call d/" + date), date);
            assertEquals(FollowUpCommandParser.MESSAGE_DATE_FORMAT, error.getMessage());
            ParseException dateFirst = assertThrows(ParseException.class, () ->
                    parser.parse("1 d/" + date + " a/Call"), date);
            assertEquals(FollowUpCommandParser.MESSAGE_DATE_FORMAT, dateFirst.getMessage());
        }
    }

    @Test
    void parse_malformedDatedArguments_rejectsRequest() {
        for (String input : List.of("1 a/Call d/2026-10-25 d/2026-11-01", "1 d/2026-10-25 a/Call d/2026-11-01",
                "1 d/2026-10-25 d/2026-11-01 a/Call", "1 d/2026-10-25 a/Call a/Other",
                "1 d/2026-10-25 a/", "1 d/2026-10-25 x/value a/Call",
                "1 a/ d/2026-10-25", "1 a/Call D/2026-10-25", "1 a/Call d/2026-10-25 x/value",
                "1 d/2026-10-25", "1 a/Call a/Other d/2026-10-25", "0 a/Call d/2026-10-25")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    void parse_validDescription_preservesPunctuationAndInternalSpacing() throws Exception {
        assertEquals(new FollowUpCommand(Index.fromOneBased(2), "Send  portfolio: https://example.com/a/b"),
                parser.parse(" 2 a/  Send  portfolio: https://example.com/a/b  "));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Call"), parser.parse("\t1\ta/Call"));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Send portfolio"),
                new ContactFileParser().parseCommand("followup 1 a/Send portfolio"));
        assertEquals(new FollowUpCommand(Index.fromOneBased(1), "Call  back: https://example.com/a/b",
                LocalDate.of(2026, 10, 30)),
                parser.parse("1 d/2026-10-30 a/Call  back: https://example.com/a/b"));
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
