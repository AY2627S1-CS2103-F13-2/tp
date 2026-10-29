package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LogCommand;
import seedu.address.model.person.Interaction;

public class LogCommandParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

    private final LogCommandParser parser = new LogCommandParser(FIXED_CLOCK);

    @Test
    public void parse_indexWithDateAndNote_success() {
        Interaction interaction = new Interaction(LocalDate.of(2026, 10, 1), "Discussed internships");
        assertParseSuccess(parser, "1 d/2026-10-01 nt/Discussed internships",
                new LogCommand(INDEX_FIRST_PERSON, interaction));
    }

    @Test
    public void parse_nameWithoutDate_usesToday() {
        Interaction interaction = new Interaction(TODAY, "Coffee chat");
        assertParseSuccess(parser, "  John   Lim nt/Coffee chat", new LogCommand("John Lim", interaction));
    }

    @Test
    public void parse_today_success() {
        assertParseSuccess(parser, "1 d/2026-10-15 nt/Coffee chat",
                new LogCommand(INDEX_FIRST_PERSON, new Interaction(TODAY, "Coffee chat")));
    }

    @Test
    public void parse_supportedDateFormats_success() {
        Interaction interaction = new Interaction(LocalDate.of(2026, 10, 1), "Coffee chat");
        for (String date : List.of("2026-10-01", "01/10/2026", "1 Oct 2026", "1 oCT 2026")) {
            assertParseSuccess(parser, "1 d/" + date + " nt/Coffee chat",
                    new LogCommand(INDEX_FIRST_PERSON, interaction));
        }
    }

    @Test
    public void parse_futureDate_failure() {
        assertParseFailure(parser, "1 d/2026-10-16 nt/Coffee chat",
                String.format(LogCommandParser.MESSAGE_FUTURE_DATE, "2026-10-16"));
    }

    @Test
    public void parse_invalidDate_failure() {
        assertParseFailure(parser, "1 d/2026-02-30 nt/Coffee chat", LogCommandParser.MESSAGE_DATE_FORMAT);
        assertParseFailure(parser, "1 d/15-10-2026 nt/Coffee chat", LogCommandParser.MESSAGE_DATE_FORMAT);
        assertParseFailure(parser, "1 d/ nt/Coffee chat", LogCommandParser.MESSAGE_DATE_FORMAT);
    }

    @Test
    public void parse_dateBeforeMinimum_failure() {
        for (String date : List.of("1999-12-31", "31/12/1999", "31 Dec 1999")) {
            assertParseFailure(parser, "1 d/" + date + " nt/Coffee chat", DateParserUtil.MESSAGE_DATE_TOO_OLD);
        }
        assertParseSuccess(parser, "1 d/01/01/2000 nt/Coffee chat",
                new LogCommand(INDEX_FIRST_PERSON, new Interaction(LocalDate.of(2000, 1, 1), "Coffee chat")));
    }

    @Test
    public void parse_blankNote_failure() {
        assertParseFailure(parser, "1 nt/   ", Interaction.MESSAGE_NOTE_CONSTRAINTS);
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, "1 d/2026-10-01", MESSAGE_INVALID_FORMAT); // no note
        assertParseFailure(parser, "nt/Coffee chat", MESSAGE_INVALID_FORMAT); // no contact
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidContact_failure() {
        assertParseFailure(parser, "0 nt/Coffee chat", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 extra nt/Coffee chat", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "John* nt/Coffee chat", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "1 d/2026-10-01 d/2026-10-02 nt/Coffee chat",
                getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }
}
