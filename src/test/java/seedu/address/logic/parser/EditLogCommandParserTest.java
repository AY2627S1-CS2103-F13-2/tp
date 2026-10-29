package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditLogCommand;
import seedu.address.model.person.Interaction;

public class EditLogCommandParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLogCommand.MESSAGE_USAGE);

    private final EditLogCommandParser parser = new EditLogCommandParser(FIXED_CLOCK);

    @Test
    public void parse_indexWithDateAndNote_success() {
        assertParseSuccess(parser, "1 i/2 d/2026-10-01 nt/Discussed internships",
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, LocalDate.of(2026, 10, 1),
                        "Discussed internships"));
    }

    @Test
    public void parse_nameWithNoteOnly_success() {
        assertParseSuccess(parser, "  John   Lim i/1 nt/Coffee chat",
                new EditLogCommand("John Lim", INDEX_FIRST_PERSON, null, "Coffee chat"));
    }

    @Test
    public void parse_dateOnlyToday_success() {
        assertParseSuccess(parser, "1 i/1 d/2026-10-15",
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, TODAY, null));
    }

    @Test
    public void parse_defaultClock_acceptsPastDate() {
        assertParseSuccess(new EditLogCommandParser(), "1 i/1 d/2020-01-01",
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, LocalDate.of(2020, 1, 1), null));
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "i/1 nt/Coffee chat", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 nt/Coffee chat", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 i/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidEntry_failure() {
        assertParseFailure(parser, "1 i/0 nt/Coffee chat", UnlogCommandParser.MESSAGE_INVALID_ENTRY);
        assertParseFailure(parser, "1 i/abc nt/Coffee chat", UnlogCommandParser.MESSAGE_INVALID_ENTRY);
    }

    @Test
    public void parse_invalidDateOrNote_failure() {
        assertParseFailure(parser, "1 i/1 d/2026-10-16",
                String.format(LogCommandParser.MESSAGE_FUTURE_DATE, "2026-10-16"));
        assertParseFailure(parser, "1 i/1 d/2026-02-30", LogCommandParser.MESSAGE_DATE_FORMAT);
        assertParseFailure(parser, "1 i/1 nt/   ", Interaction.MESSAGE_NOTE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTarget_failure() {
        assertParseFailure(parser, "0 i/1 nt/Coffee chat", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "J@ne i/1 nt/Coffee chat", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "1 i/1 nt/a nt/b", getErrorMessageForDuplicatePrefixes(PREFIX_NOTE));
    }
}
