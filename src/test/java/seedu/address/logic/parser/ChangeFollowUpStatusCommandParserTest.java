package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ChangeFollowUpStatusCommand;
import seedu.address.model.person.FollowUp;

public class ChangeFollowUpStatusCommandParserTest {
    private static final String COMPLETE_FAILURE = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            ChangeFollowUpStatusCommand.MESSAGE_USAGE_COMPLETE);
    private static final String REOPEN_FAILURE = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            ChangeFollowUpStatusCommand.MESSAGE_USAGE_REOPEN);

    private final ChangeFollowUpStatusCommandParser completeParser =
            new ChangeFollowUpStatusCommandParser(FollowUp.Status.COMPLETED);
    private final ChangeFollowUpStatusCommandParser reopenParser =
            new ChangeFollowUpStatusCommandParser(FollowUp.Status.PENDING);

    @Test
    public void parse_validArgs_success() {
        assertParseSuccess(completeParser, " 1 i/2",
                new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 2, FollowUp.Status.COMPLETED));
        assertParseSuccess(reopenParser, "  3   i/ 7 ",
                new ChangeFollowUpStatusCommand(Index.fromOneBased(3), 7, FollowUp.Status.PENDING));
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(completeParser, "", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " 1", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " i/2", COMPLETE_FAILURE);
        assertParseFailure(reopenParser, " 1 i/", REOPEN_FAILURE);
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(completeParser, " 0 i/2", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " 1 i/0", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " 1 i/-3", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " Alice i/1", COMPLETE_FAILURE);
        assertParseFailure(completeParser, " 1 i/2 a/Call", COMPLETE_FAILURE);
        assertParseFailure(reopenParser, " 1 2 i/2", REOPEN_FAILURE);
    }

    @Test
    public void parse_repeatedId_failure() {
        assertParseFailure(completeParser, " 1 i/2 i/3",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ENTRY));
    }
}
