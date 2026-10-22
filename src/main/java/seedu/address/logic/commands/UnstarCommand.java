package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Removes the star from a person identified by their displayed index.
 */
public class UnstarCommand extends Command {

    public static final String COMMAND_WORD = "unstar";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the star from the contact identified by the index number "
            + "used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Unstarred contact: %1$s";
    public static final String MESSAGE_NOT_STARRED = "%1$s is not starred.";

    private final Index targetIndex;

    /**
     * Creates an UnstarCommand for the person at the specified displayed index.
     */
    public UnstarCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUnstar = lastShownList.get(targetIndex.getZeroBased());
        if (!personToUnstar.isStarred()) {
            return new CommandResult(String.format(MESSAGE_NOT_STARRED, personToUnstar.getName()));
        }

        Person unstarredPerson = personToUnstar.withStarred(false);
        model.setPerson(personToUnstar, unstarredPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, unstarredPerson.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UnstarCommand otherUnstarCommand)) {
            return false;
        }
        return targetIndex.equals(otherUnstarCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
