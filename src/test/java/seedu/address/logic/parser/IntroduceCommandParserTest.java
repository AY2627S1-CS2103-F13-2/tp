package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.IntroduceCommand;
import seedu.address.model.person.Name;

public class IntroduceCommandParserTest {
    private final IntroduceCommandParser parser = new IntroduceCommandParser();

    @Test
    public void parse_validName_returnsIntroduceCommand() {
        assertParseSuccess(parser, "Jane Smith i/Jane Doe",
                new IntroduceCommand("Jane Smith", new Name("Jane Doe"), false));
    }

    @Test
    public void parse_confirmedReplacement_returnsConfirmedCommand() {
        assertParseSuccess(parser, "Jane Smith i/Jane Doe --confirm",
                new IntroduceCommand("Jane Smith", new Name("Jane Doe"), true));
    }

    @Test
    public void parse_indexTarget_returnsCommand() {
        assertParseSuccess(parser, "2 i/Jane Doe",
                new IntroduceCommand(Index.fromOneBased(2), new Name("Jane Doe"), false));
    }

    @Test
    public void parse_nameWithPunctuation_succeeds() {
        assertParseSuccess(parser, "Jane Smith i/O'Brien-Smith",
                new IntroduceCommand("Jane Smith", new Name("O'Brien-Smith"), false));
    }

    @Test
    public void parse_whitespaceAndPartialTarget_normalisesTargetAndIntroducer() {
        assertParseSuccess(parser, "  Jane   Smith   i/  Jane   Doe   ",
                new IntroduceCommand("Jane Smith", new Name("Jane Doe"), false));
    }

    @Test
    public void parse_confirmationMustBeFinalOption() {
        assertParseSuccess(parser, "Jane Smith i/Jane Doe --confirm later",
                new IntroduceCommand("Jane Smith", new Name("Jane Doe --confirm later"), false));
        assertParseSuccess(parser, "Jane Smith i/Jane Doe --confirm-extra",
                new IntroduceCommand("Jane Smith", new Name("Jane Doe --confirm-extra"), false));
    }

    @Test
    public void parse_missingOrInvalidName_fails() {
        assertParseFailure(parser, "   ", IntroduceCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, "Jane Smith", String.format(
                MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "Jane Smith i/ --confirm", IntroduceCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, "Jane Smith i/   ", IntroduceCommand.MESSAGE_NAME_REQUIRED);
        assertParseFailure(parser, "Jane Smith i/Jane& Doe", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyTargetInput_fails() {
        assertParseFailure(parser, "i/Jane Doe",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_fails() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "0 i/Jane Doe", expectedMessage);
        assertParseFailure(parser, "-2 i/Jane Doe", expectedMessage);
        assertParseFailure(parser, "+2 i/Jane Doe", expectedMessage);
        assertParseFailure(parser, "2 Jane i/Jane Doe", expectedMessage);
    }

    @Test
    public void parse_missingOrUnexpectedPrefix_fails() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroduceCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 Jane Doe", expectedMessage);
        assertParseFailure(parser, "Jane n/Jane Doe", expectedMessage);
        assertParseFailure(parser, "Jane& i/Jane Doe", expectedMessage);
        assertParseFailure(parser, "Jane Smith n/Jane Doe i/Jane Doe", expectedMessage);
    }

    @Test
    public void parse_duplicateIntroducerPrefix_fails() {
        assertParseFailure(parser, "Jane i/Jane Doe i/John Smith",
                seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS + "i/");
    }

}
