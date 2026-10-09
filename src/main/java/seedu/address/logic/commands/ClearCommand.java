package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Clears every contact from the address book. Takes no parameters.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Cleared %1$d contact(s). 0 contact(s) remaining.";
    public static final String MESSAGE_EMPTY_ADDRESS_BOOK = "There are no contacts to clear.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        int clearedCount = model.getAddressBook().getPersonList().size();

        if (clearedCount == 0) {
            throw new CommandException(MESSAGE_EMPTY_ADDRESS_BOOK);
        }

        model.setAddressBook(new AddressBook());
        return new CommandResult(String.format(MESSAGE_SUCCESS, clearedCount));
    }

    @Override
    public boolean equals(Object other) {
        // a ClearCommand carries no state, so all instances are interchangeable
        return other == this || other instanceof ClearCommand;
    }
}
