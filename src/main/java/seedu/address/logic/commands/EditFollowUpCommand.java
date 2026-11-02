package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Edits an existing follow-up while preserving its ID and status.
 */
public class EditFollowUpCommand extends Command {

    public static final String COMMAND_WORD = "editfollowup";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits a follow-up for a displayed contact.\n"
            + "Parameters: CONTACT_INDEX i/FOLLOW_UP_ID "
            + "[a/DESCRIPTION] [d/DATE]\n"
            + "Example: editfollowup 1 i/2 a/Call client d/2026-11-05";

    public static final String MESSAGE_FOLLOW_UP_NOT_FOUND =
            "The specified follow-up does not exist for this contact.";

    public static final String MESSAGE_DUPLICATE_FOLLOW_UP =
            "Another pending follow-up with the same description already exists.";

    private final Index targetIndex;
    private final int followUpId;
    private final String description;
    private final LocalDate dueDate;
    private final boolean dueDateSpecified;

    /**
     * Creates an edit-follow-up command.
     *
     * @param targetIndex displayed contact index
     * @param followUpId ID of the follow-up to edit
     * @param description new description, or null to retain the current description
     * @param dueDate new due date
     * @param dueDateSpecified whether the due date was provided
     */
    public EditFollowUpCommand(
            Index targetIndex,
            int followUpId,
            String description,
            LocalDate dueDate,
            boolean dueDateSpecified) {
        this.targetIndex = requireNonNull(targetIndex);
        checkArgument(followUpId > 0, "Follow-up IDs must be positive.");
        checkArgument(
                description == null || FollowUp.isValidDescription(description),
                FollowUp.MESSAGE_CONSTRAINTS);

        this.followUpId = followUpId;
        this.description = description == null ? null : description.strip();
        this.dueDate = dueDate;
        this.dueDateSpecified = dueDateSpecified;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> displayedContacts = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= displayedContacts.size()) {
            throw new CommandException(
                    Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person target = displayedContacts.get(targetIndex.getZeroBased());

        FollowUp existing = target.getFollowUps().stream()
                .filter(action -> action.getId() == followUpId)
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        MESSAGE_FOLLOW_UP_NOT_FOUND));

        String updatedDescription = description == null
                ? existing.getDescription()
                : description;

        LocalDate updatedDueDate = dueDateSpecified
                ? dueDate
                : existing.getDueDate().orElse(null);

        boolean duplicateExists = description != null
                && !existing.getDescription().equals(updatedDescription)
                && target.getFollowUps().stream()
                .anyMatch(action -> action.getId() != followUpId
                        && action.getStatus() == FollowUp.Status.PENDING
                        && existing.getStatus() == FollowUp.Status.PENDING
                        && action.getDescription().equals(updatedDescription));

        if (duplicateExists) {
            throw new CommandException(MESSAGE_DUPLICATE_FOLLOW_UP);
        }

        FollowUp replacement = new FollowUp(
                existing.getId(),
                updatedDescription,
                existing.getStatus(),
                updatedDueDate);

        Person updated = target.replaceFollowUp(followUpId, replacement);
        model.setPerson(target, updated);

        return new CommandResult(
                "Updated follow-up for " + updated.getName() + ":\n"
                        + Messages.formatFollowUps(updated));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EditFollowUpCommand otherCommand)) {
            return false;
        }

        return targetIndex.equals(otherCommand.targetIndex)
                && followUpId == otherCommand.followUpId
                && Objects.equals(description, otherCommand.description)
                && Objects.equals(dueDate, otherCommand.dueDate)
                && dueDateSpecified == otherCommand.dueDateSpecified;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("followUpId", followUpId)
                .add("description", description)
                .add("dueDate", dueDate)
                .add("dueDateSpecified", dueDateSpecified)
                .toString();
    }
}
