package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Records a pending follow-up for a displayed contact, confirming pending duplicates first.
 */
public class FollowUpCommand extends Command {
    public static final String COMMAND_WORD = "followup";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records a pending follow-up for the contact at the displayed index.\n"
            + "Parameters: INDEX (positive integer) a/DESCRIPTION (not blank)\n"
            + "Example: followup 1 a/Send portfolio\n"
            + "An identical pending action asks Add another? Reply y, n, or cancel.";
    public static final String MESSAGE_CONTACT_CHANGED = "Contact changed. Start the follow-up request again.";
    public static final String MESSAGE_IDS_EXHAUSTED = "No more follow-up IDs are available for this contact.";

    private final Index targetIndex;
    private final String description;

    /**
     * Creates a follow-up request with a displayed index and a non-blank description.
     */
    public FollowUpCommand(Index targetIndex, String description) {
        this.targetIndex = requireNonNull(targetIndex);
        checkArgument(FollowUp.isValidDescription(description), FollowUp.MESSAGE_CONSTRAINTS);
        this.description = description.strip();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (targetIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = model.getFilteredPersonList().get(targetIndex.getZeroBased());
        Optional<FollowUp> duplicate = target.getFollowUps().stream()
                .filter(action -> action.getStatus() == FollowUp.Status.PENDING
                        && action.getDescription().equals(description))
                .findFirst();
        Command addition = new AddFollowUpCommand(target, description);
        if (duplicate.isPresent()) {
            PendingRequest request = new ConfirmationRequest("Pending action for " + target.getName()
                    + " already exists:\n" + duplicate.orElseThrow(), "Add another?", addition);
            return new CommandResult(request.getPrompt(), request);
        }
        return addition.execute(model);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FollowUpCommand command && targetIndex.equals(command.targetIndex)
                && description.equals(command.description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).add("description", description).toString();
    }

    /**
     * Appends to the resolved contact rather than reinterpreting a displayed index on confirmation.
     */
    private static final class AddFollowUpCommand extends Command {
        private final Person target;
        private final String description;

        private AddFollowUpCommand(Person target, String description) {
            this.target = target;
            this.description = description;
        }

        @Override
        public CommandResult execute(Model model) throws CommandException {
            if (!model.getContactFile().getPersonList().contains(target)) {
                throw new CommandException(MESSAGE_CONTACT_CHANGED);
            }
            int greatestId = target.getFollowUps().stream().mapToInt(FollowUp::getId).max().orElse(0);
            if (greatestId == Integer.MAX_VALUE) {
                throw new CommandException(MESSAGE_IDS_EXHAUSTED);
            }
            Person updated = target.appendFollowUp(new FollowUp(greatestId + 1, description, FollowUp.Status.PENDING));
            model.setPerson(target, updated);
            return new CommandResult("Updated follow-ups for " + updated.getName() + ":\n"
                    + Messages.formatFollowUps(updated));
        }
    }
}
