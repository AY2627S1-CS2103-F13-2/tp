package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameMatchesExactlyPredicate;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified either by their displayed index or by their full name.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by the index number used in the displayed person list, "
            + "or the person with the given full name.\n"
            + "Parameters: INDEX (must be a positive integer) or " + PREFIX_NAME + "NAME\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_EMPTY_ADDRESS_BOOK = "There are no contacts to delete.";
    public static final String MESSAGE_NAME_NOT_FOUND =
            "No contact named %1$s was found. Names must be given in full, but are not case-sensitive.";

    private final Index targetIndex;
    private final Name targetName;

    /**
     * Creates a {@code DeleteCommand} that deletes the person at {@code targetIndex}
     * of the currently displayed list.
     */
    public DeleteCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.targetName = null;
    }

    /**
     * Creates a {@code DeleteCommand} that deletes the person whose full name is {@code targetName}.
     */
    public DeleteCommand(Name targetName) {
        requireNonNull(targetName);
        this.targetIndex = null;
        this.targetName = targetName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.getAddressBook().getPersonList().isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_ADDRESS_BOOK);
        }

        Person personToDelete = (targetName != null) ? findByName(model) : findByIndex(model);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    /**
     * Returns the person at the target index of the currently displayed list.
     *
     * @throws CommandException if the index is beyond the end of that list.
     */
    private Person findByIndex(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(targetIndex.getZeroBased());
    }

    /**
     * Returns the person with the target name. The whole address book is searched rather than the
     * displayed list, so naming someone in full deletes them even while a {@code find} filter is active.
     * At most one person can match, because the address book does not allow two persons with the same name.
     *
     * @throws CommandException if no person has that name.
     */
    private Person findByName(Model model) throws CommandException {
        NameMatchesExactlyPredicate predicate = new NameMatchesExactlyPredicate(targetName);
        Optional<Person> match = model.getAddressBook().getPersonList().stream()
                .filter(predicate)
                .findFirst();
        return match.orElseThrow(() ->
                new CommandException(String.format(MESSAGE_NAME_NOT_FOUND, targetName)));
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
