package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AliasCommand;

public class AliasCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AliasCommand.MESSAGE_USAGE);
    private static final String TARGETS = String.join(", ", ContactFileParser.targetCommandWords());

    private final AliasCommandParser parser = new AliasCommandParser();

    @Test
    public void parse_noArguments_returnsList() {
        assertParseSuccess(parser, "   ", AliasCommand.list());
    }

    @Test
    public void parse_shortcutAndCommand_returnsAdd() {
        assertParseSuccess(parser, " fn find /notes", AliasCommand.add("fn", "find /notes"));
        // shortcut names are stored in lower case; extra spaces around the parts are ignored
        assertParseSuccess(parser, "  FN2   find /email  ", AliasCommand.add("fn2", "find /email"));
    }

    @Test
    public void parse_replace_returnsReplace() {
        assertParseSuccess(parser, " /replace fn find /email", AliasCommand.replace("fn", "find /email"));
    }

    @Test
    public void parse_remove_returnsRemove() {
        assertParseSuccess(parser, " /remove Fn", AliasCommand.remove("fn"));
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, " fn", INVALID_FORMAT);
        assertParseFailure(parser, " /replace", INVALID_FORMAT);
        assertParseFailure(parser, " /replace fn", INVALID_FORMAT);
        assertParseFailure(parser, " /remove", INVALID_FORMAT);
        assertParseFailure(parser, " /remove fn extra", INVALID_FORMAT);
    }

    @Test
    public void parse_invalidShortcutName_failure() {
        assertParseFailure(parser, " 2fn find /notes",
                String.format(AliasCommandParser.MESSAGE_INVALID_SHORTCUT, "2fn"));
        assertParseFailure(parser, " f-n find /notes",
                String.format(AliasCommandParser.MESSAGE_INVALID_SHORTCUT, "f-n"));
        assertParseFailure(parser, " /other fn find /notes",
                String.format(AliasCommandParser.MESSAGE_INVALID_SHORTCUT, "/other"));
        assertParseFailure(parser, " /remove f-n",
                String.format(AliasCommandParser.MESSAGE_INVALID_SHORTCUT, "f-n"));
    }

    @Test
    public void parse_builtInCommandAsShortcut_failure() {
        assertParseFailure(parser, " list find /notes", String.format(AliasCommandParser.MESSAGE_BUILT_IN, "list"));
        assertParseFailure(parser, " DELETE find /notes",
                String.format(AliasCommandParser.MESSAGE_BUILT_IN, "delete"));
        assertParseFailure(parser, " /replace alias list",
                String.format(AliasCommandParser.MESSAGE_BUILT_IN, "alias"));
    }

    @Test
    public void parse_expansionNotACommand_failure() {
        assertParseFailure(parser, " fn search /notes",
                String.format(AliasCommandParser.MESSAGE_UNKNOWN_TARGET, TARGETS, "search"));
        // shortcuts cannot stand for other shortcuts or for alias itself
        assertParseFailure(parser, " fn alias x list",
                String.format(AliasCommandParser.MESSAGE_UNKNOWN_TARGET, TARGETS, "alias"));
    }
}
