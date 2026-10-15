package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;

/**
 * Clears the contact file.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Clears every contact from the contact file. Takes no parameters.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Cleared %1$d contact(s). 0 contact(s) remaining.";
    public static final String MESSAGE_EMPTY_CONTACT_FILE = "There are no contacts to clear.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        int clearedCount = model.getContactFile().getPersonList().size();

        if (clearedCount == 0) {
            throw new CommandException(MESSAGE_EMPTY_CONTACT_FILE);
        }

        model.setContactFile(new ContactFile());
        return new CommandResult(String.format(MESSAGE_SUCCESS, clearedCount));
    }

    @Override
    public boolean equals(Object other) {
        // a ClearCommand carries no state, so all instances are interchangeable
        return other == this || other instanceof ClearCommand;
    }
}
