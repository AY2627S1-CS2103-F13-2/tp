package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EventCommand;
import seedu.address.model.person.Event;

public class EventCommandParserTest {

    private final EventCommandParser parser = new EventCommandParser();

    @Test
    public void parse_validArguments_returnsEventCommand() {
        EventCommand expected = new EventCommand(
                Index.fromOneBased(1),
                new Event("NUS Career Fair", "2026-10-20"));

        assertParseSuccess(
                parser,
                "add 1 ev/NUS Career Fair d/2026-10-20",
                expected);
    }

    @Test
    public void parse_missingEventName_throwsParseException() {
        assertParseFailure(
                parser,
                "add 1 d/2026-10-20",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingEventDate_throwsParseException() {
        assertParseFailure(
                parser,
                "add 1 ev/NUS Career Fair",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(
                parser,
                "add abc ev/NUS Career Fair d/2026-10-20",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidDate_throwsParseException() {
        assertParseFailure(
                parser,
                "add 1 ev/NUS Career Fair d/2026-02-30",
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        EventCommand.MESSAGE_USAGE));
    }
}
