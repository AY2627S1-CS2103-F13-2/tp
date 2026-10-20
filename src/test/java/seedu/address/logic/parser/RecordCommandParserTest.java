package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RecordCommand;
import seedu.address.model.person.Name;

public class RecordCommandParserTest {
    private final RecordCommandParser parser = new RecordCommandParser();

    @Test
    public void parse_validName_returnsRecordCommand() {
        assertParseSuccess(parser, "1 i/Jane Doe",
                new RecordCommand(Index.fromOneBased(1), new Name("Jane Doe"), false));
    }

    @Test
    public void parse_confirmedReplacement_returnsConfirmedCommand() {
        assertParseSuccess(parser, "2 i/Jane Doe --confirm",
                new RecordCommand(Index.fromOneBased(2), new Name("Jane Doe"), true));
    }

    @Test
    public void parse_nameWithPunctuation_succeeds() {
        assertParseSuccess(parser, "1 i/O'Brien-Smith",
                new RecordCommand(Index.fromOneBased(1), new Name("O'Brien-Smith"), false));
    }

    @Test
    public void parse_missingOrInvalidName_fails() {
        assertParseFailure(parser, "1", RecordCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, "1 i/ --confirm", RecordCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, "1 Jane Doe",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecordCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 i/Jane& Doe", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidIndex_fails() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecordCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "0 i/Jane Doe", expectedMessage);
        assertParseFailure(parser, "abc i/Jane Doe", expectedMessage);
    }

    @Test
    public void parse_missingOrUnexpectedPrefix_fails() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecordCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 Jane Doe", expectedMessage);
        assertParseFailure(parser, "1 n/Jane Doe", expectedMessage);
        assertParseFailure(parser, "1 extra i/Jane Doe", expectedMessage);
    }

    @Test
    public void parse_duplicateIntroducerPrefix_fails() {
        assertParseFailure(parser, "1 i/Jane Doe i/John Smith",
                seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS + "i/");
    }

}
