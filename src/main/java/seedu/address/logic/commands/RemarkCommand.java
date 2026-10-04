package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/**
 * Changes the remark of an existing person in the address book.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits the remark of the person identified "
            + "by the index number used in the last person listing. "
            + "Existing remark will be overwritten by the input.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "r/ [REMARK]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + "r/ Likes to swim.";

    public static final String MESSAGE_ADD_REMARK_SUCCESS = "Added remark to person: %1$s; Remark: %2$s";
    public static final String MESSAGE_DELETE_REMARK_SUCCESS = "Removed remark from person: %1$s";

    private final Index index;
    private final Remark remark;

    /**
     * @param index of the person in the filtered person list to edit the remark
     * @param remark of the person to be updated to
     */
    public RemarkCommand(Index index, Remark remark) {
        requireAllNonNull(index, remark);

        this.index = index;
        this.remark = remark;
    }
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = model.getFilteredPersonList().get(index.getZeroBased());
        Person editedPerson = new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), remark, person.getTags());
        model.setPerson(person, editedPerson);
        String message = remark.value.isEmpty() ? MESSAGE_DELETE_REMARK_SUCCESS : MESSAGE_ADD_REMARK_SUCCESS;
        return new CommandResult(String.format(message, Messages.format(editedPerson), remark));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RemarkCommand)) {
            return false;
        }

        RemarkCommand e = (RemarkCommand) other;
        return index.equals(e.index)
                && remark.equals(e.remark);
    }
}
