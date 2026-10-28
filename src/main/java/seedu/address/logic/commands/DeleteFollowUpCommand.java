package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Requests confirmation before removing one action by its stable per-contact ID.
 */
public class DeleteFollowUpCommand extends Command {
    public static final String COMMAND_WORD = "deletefollowup";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Previews a follow-up and asks for y/n confirmation before deleting.\n"
            + "Parameters: CONTACT_INDEX (positive integer) i/FOLLOW_UP_ID (positive integer)\n"
            + "Example: deletefollowup 1 i/2";
    public static final String MESSAGE_NOT_FOUND = "The specified follow-up does not exist for this contact.";
    public static final String MESSAGE_CONTACT_CHANGED = "Contact changed. Start the delete-follow-up request again.";

    private final Index targetIndex;
    private final int followUpId;

    /**
     * Creates a deletion request using a displayed contact index and a stable action ID.
     */
    public DeleteFollowUpCommand(Index targetIndex, int followUpId) {
        this.targetIndex = requireNonNull(targetIndex);
        checkArgument(followUpId > 0, "Follow-up IDs must be positive.");
        this.followUpId = followUpId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (targetIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = model.getFilteredPersonList().get(targetIndex.getZeroBased());
        FollowUp action = target.getFollowUps().stream().filter(candidate -> candidate.getId() == followUpId)
                .findFirst().orElseThrow(() -> new CommandException(MESSAGE_NOT_FOUND));
        PendingRequest request = new ConfirmationRequest("Delete follow-up for " + target.getName() + ":\n"
                + Messages.formatFollowUp(action), new Command() {
                    @Override
                    public CommandResult execute(Model confirmedModel) throws CommandException {
                        if (!confirmedModel.getContactFile().getPersonList().contains(target)) {
                            throw new CommandException(MESSAGE_CONTACT_CHANGED);
                        }
                        Person updated = target.removeFollowUp(followUpId);
                        assert updated.getLastFollowUpId() == target.getLastFollowUpId()
                                : "Deleting an action must retain allocated IDs";
                        confirmedModel.setPerson(target, updated);
                        return new CommandResult("Deleted follow-up #" + followUpId + " for " + target.getName()
                                + ".\nRemaining follow-ups:\n" + Messages.formatFollowUps(updated));
                    }
                });
        return new CommandResult(request.getPrompt(), request);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteFollowUpCommand command && targetIndex.equals(command.targetIndex)
                && followUpId == command.followUpId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("followUpId", followUpId).toString();
    }
}
