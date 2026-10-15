package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a person from the contact file, identified either by displayed index or by name.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by the index number used in the displayed person list, "
            + "or by name.\n"
            + "Parameters: INDEX (must be a positive integer) or NAME\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "Example: " + COMMAND_WORD + " John Doe";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_EMPTY_CONTACT_FILE = "There are no contacts to delete.";

    /** Exactly one of these two is set; {@code targetName} is set when the user named the contact. */
    private final Index targetIndex;
    private final String targetName;

    /**
     * @param targetIndex index of the person in the displayed person list to delete
     */
    public DeleteCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.targetName = null;
    }

    /**
     * @param targetName name of the person to delete, matched the same way {@code find} matches names
     */
    public DeleteCommand(String targetName) {
        requireNonNull(targetName);
        this.targetIndex = null;
        this.targetName = targetName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Checked before the target is resolved, so that an empty contact file is reported as such
        // rather than as a bad index or a missing name (UC02 extension 1a).
        if (model.getContactFile().getPersonList().isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_CONTACT_FILE);
        }

        Person personToDelete;
        if (targetName == null) {
            List<Person> lastShownList = model.getFilteredPersonList();
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            personToDelete = lastShownList.get(targetIndex.getZeroBased());
        } else {
            switch (PersonNameResolver.resolve(model, targetName, COMMAND_WORD)) {
                case PersonNameResolver.Outcome.Unique unique -> personToDelete = unique.person();
                case PersonNameResolver.Outcome.Several several -> {
                    return new CommandResult(several.message());
                }
            }
        }

        model.deletePerson(personToDelete);

        if (targetName != null) {
            // Resolving the name narrowed the displayed list to the contact just deleted, which would
            // otherwise leave the user staring at an empty list. Deleting by index happens within the
            // list the user is already looking at, so that view is left alone.
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        }

        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeleteCommand.targetIndex)
                && Objects.equals(targetName, otherDeleteCommand.targetName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetName", targetName)
                .toString();
    }
}
