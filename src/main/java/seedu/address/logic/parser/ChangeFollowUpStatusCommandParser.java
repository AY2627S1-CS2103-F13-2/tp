package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ChangeFollowUpStatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses arguments for the completefollowup and reopenfollowup commands.
 */
public class ChangeFollowUpStatusCommandParser implements Parser<ChangeFollowUpStatusCommand> {

    private final FollowUp.Status newStatus;

    /**
     * Creates a parser for commands that set a follow-up to {@code newStatus}.
     */
    public ChangeFollowUpStatusCommandParser(FollowUp.Status newStatus) {
        this.newStatus = requireNonNull(newStatus);
    }

    /**
     * Parses {@code CONTACT_INDEX i/FOLLOW_UP_ID}.
     *
     * @throws ParseException if the arguments are invalid
     */
    @Override
    public ChangeFollowUpStatusCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ENTRY);
        if (argMultimap.getValue(PREFIX_ENTRY).isEmpty()) {
            throw invalidFormat();
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ENTRY);

        Index targetIndex;
        Index followUpId;
        try {
            targetIndex = ParserUtil.parseIndex(argMultimap.getPreamble());
            followUpId = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_ENTRY).get());
        } catch (ParseException pe) {
            throw invalidFormat();
        }
        return new ChangeFollowUpStatusCommand(targetIndex, followUpId.getOneBased(), newStatus);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                newStatus == FollowUp.Status.COMPLETED
                        ? ChangeFollowUpStatusCommand.MESSAGE_USAGE_COMPLETE
                        : ChangeFollowUpStatusCommand.MESSAGE_USAGE_REOPEN));
    }
}
