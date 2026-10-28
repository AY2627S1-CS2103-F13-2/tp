package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Removes the star from a person identified by their displayed index or a find query.
 */
public class UnstarCommand extends Command {

    public static final String COMMAND_WORD = "unstar";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the star from the contact identified by the index number "
            + "used in the displayed person list, or by a find query.\n"
            + "Parameters: INDEX (must be a positive integer) or QUERY (same criteria as find)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Unstarred contact: %1$s";
    public static final String MESSAGE_NOT_STARRED = "%1$s is not starred.";

    public static final String MESSAGE_MULTIPLE_MATCHES =
            "Multiple contacts match. Please retry with unstar INDEX using the displayed list.";

    private final Index targetIndex;
    private final FindCommand findCommand;

    /**
     * Creates an UnstarCommand for the person at the specified displayed index.
     */
    public UnstarCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.findCommand = null;
    }

    /**
     * Creates a command for the unique contact matching the supplied find command.
     */
    public UnstarCommand(FindCommand findCommand) {
        this.targetIndex = null;
        this.findCommand = requireNonNull(findCommand);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Index resolvedIndex = targetIndex;
        if (findCommand != null) {
            CommandResult findResult = findCommand.execute(model);
            if (FindCommand.MESSAGE_CONTACT_NOT_FOUND.equals(findResult.getFeedbackToUser())) {
                return findResult;
            }
            int matchCount = model.getFilteredPersonList().size();
            if (matchCount == 0) {
                return findResult;
            }
            if (matchCount > 1) {
                return new CommandResult(findResult.getFeedbackToUser() + "\n" + MESSAGE_MULTIPLE_MATCHES);
            }
            resolvedIndex = Index.fromOneBased(1);
        }
        List<Person> lastShownList = model.getFilteredPersonList();
        if (resolvedIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUnstar = lastShownList.get(resolvedIndex.getZeroBased());
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
        return Objects.equals(targetIndex, otherUnstarCommand.targetIndex)
                && Objects.equals(findCommand, otherUnstarCommand.findCommand);
    }

    @Override
    public String toString() {
        if (findCommand != null) {
            return new ToStringBuilder(this).add("findCommand", findCommand).toString();
        }
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
