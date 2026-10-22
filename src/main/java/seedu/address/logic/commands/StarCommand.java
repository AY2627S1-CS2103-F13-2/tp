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
 * Stars a person identified by their displayed index.
 */
public class StarCommand extends Command {

    public static final String COMMAND_WORD = "star";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Stars the contact identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Starred contact: %1$s";
    public static final String MESSAGE_ALREADY_STARRED = "%1$s is already starred.";

    private final Index targetIndex;

    /**
     * Creates a StarCommand for the person at the specified displayed index.
     */
    public StarCommand(Index targetIndex) {
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

        Person personToStar = lastShownList.get(targetIndex.getZeroBased());
        if (personToStar.isStarred()) {
            return new CommandResult(String.format(MESSAGE_ALREADY_STARRED, personToStar.getName()));
        }

        Person starredPerson = personToStar.withStarred(true);
        model.setPerson(personToStar, starredPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, starredPerson.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StarCommand otherStarCommand)) {
            return false;
        }
        return targetIndex.equals(otherStarCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
