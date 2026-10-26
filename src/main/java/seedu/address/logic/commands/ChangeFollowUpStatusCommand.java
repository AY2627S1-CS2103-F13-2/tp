package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Marks a follow-up as completed or reopens it as pending, preserving its ID, description and due date.
 */
public class ChangeFollowUpStatusCommand extends Command {

    public static final String COMMAND_WORD_COMPLETE = "completefollowup";
    public static final String COMMAND_WORD_REOPEN = "reopenfollowup";

    public static final String MESSAGE_USAGE_COMPLETE = COMMAND_WORD_COMPLETE
            + ": Marks a pending follow-up of a displayed contact as completed.\n"
            + "Parameters: CONTACT_INDEX i/FOLLOW_UP_ID\n"
            + "Example: " + COMMAND_WORD_COMPLETE + " 1 i/2";
    public static final String MESSAGE_USAGE_REOPEN = COMMAND_WORD_REOPEN
            + ": Reopens a completed follow-up of a displayed contact as pending.\n"
            + "Parameters: CONTACT_INDEX i/FOLLOW_UP_ID\n"
            + "Example: " + COMMAND_WORD_REOPEN + " 1 i/2";

    public static final String MESSAGE_FOLLOW_UP_NOT_FOUND = EditFollowUpCommand.MESSAGE_FOLLOW_UP_NOT_FOUND;
    public static final String MESSAGE_ALREADY_COMPLETED = "Follow-up #%1$d is already completed.";
    public static final String MESSAGE_ALREADY_PENDING = "Follow-up #%1$d is already pending.";
    public static final String MESSAGE_DUPLICATE_PENDING =
            "Follow-up #%1$d cannot be reopened: pending follow-up #%2$d has the same description.";
    public static final String MESSAGE_COMPLETED = "Completed follow-up for %1$s:\n%2$s";
    public static final String MESSAGE_REOPENED = "Reopened follow-up for %1$s:\n%2$s";

    private final Index targetIndex;
    private final int followUpId;
    private final FollowUp.Status newStatus;

    /**
     * Creates a command that sets the follow-up with {@code followUpId} of the displayed contact at
     * {@code targetIndex} to {@code newStatus}.
     */
    public ChangeFollowUpStatusCommand(Index targetIndex, int followUpId, FollowUp.Status newStatus) {
        this.targetIndex = requireNonNull(targetIndex);
        checkArgument(followUpId > 0, "Follow-up IDs must be positive.");
        this.followUpId = followUpId;
        this.newStatus = requireNonNull(newStatus);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedContacts = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = displayedContacts.get(targetIndex.getZeroBased());
        FollowUp existing = target.getFollowUps().stream()
                .filter(action -> action.getId() == followUpId)
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_FOLLOW_UP_NOT_FOUND));

        if (existing.getStatus() == newStatus) {
            throw new CommandException(String.format(newStatus == FollowUp.Status.COMPLETED
                    ? MESSAGE_ALREADY_COMPLETED : MESSAGE_ALREADY_PENDING, followUpId));
        }
        if (newStatus == FollowUp.Status.PENDING) {
            FollowUp duplicate = target.getFollowUps().stream()
                    .filter(action -> action.getStatus() == FollowUp.Status.PENDING
                            && action.getDescription().equals(existing.getDescription()))
                    .findFirst()
                    .orElse(null);
            if (duplicate != null) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PENDING, followUpId, duplicate.getId()));
            }
        }

        FollowUp replacement = new FollowUp(existing.getId(), existing.getDescription(), newStatus,
                existing.getDueDate().orElse(null));
        Person updated = target.replaceFollowUp(followUpId, replacement);
        model.setPerson(target, updated);
        String format = newStatus == FollowUp.Status.COMPLETED ? MESSAGE_COMPLETED : MESSAGE_REOPENED;
        return new CommandResult(String.format(format, updated.getName(), Messages.formatFollowUp(replacement)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ChangeFollowUpStatusCommand command
                && targetIndex.equals(command.targetIndex)
                && followUpId == command.followUpId
                && newStatus == command.newStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("followUpId", followUpId)
                .add("newStatus", newStatus)
                .toString();
    }
}
