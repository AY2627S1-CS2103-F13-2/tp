package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DropCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.tag.Tag;

public class DropCommandParserTest {
    private final DropCommandParser parser = new DropCommandParser();
    private final String invalidFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DropCommand.MESSAGE_USAGE);

    @Test
    public void parse_validIndexWithMultipleTags_returnsDropCommand() {
        assertParseSuccess(parser, "1 t/old t/stale",
                new DropCommand(Index.fromOneBased(1), List.of(
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("old")),
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("stale"))), false));
    }

    @Test
    public void parse_validNameWithEmailAndConfirmation_returnsConfirmedDropCommand() {
        assertParseSuccess(parser, "Jane Doe e/jane@example.com --confirm",
                new DropCommand("Jane Doe", List.of(
                        new DropCommand.FieldValue(DropCommand.Field.EMAIL, new Email("jane@example.com"))), true));
    }

    @Test
    public void parse_validIntroducer_returnsDropCommand() {
        assertParseSuccess(parser, "Jane Doe i/Old Introducer",
                new DropCommand("Jane Doe", List.of(
                        new DropCommand.FieldValue(DropCommand.Field.INTRODUCER, new Name("Old Introducer"))),
                        false));
    }

    @Test
    public void parse_invalidOrMissingSelection_fails() {
        assertParseFailure(parser, "", invalidFormat);
        assertParseFailure(parser, "1", invalidFormat);
        assertParseFailure(parser, "1 n/Jane Doe",
                "The contact name cannot be removed.");
        assertParseFailure(parser, "1 t/", invalidFormat);
        assertParseFailure(parser, "1 e/not-an-email", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 p/123 t/old p/456",
                "Multiple values specified for the following single-valued field(s): p/");
    }
}
