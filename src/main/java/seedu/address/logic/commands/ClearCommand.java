package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;

/**
 * Requests confirmation before clearing all stored contacts.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the total stored count and asks for y/n confirmation before clearing. Takes no parameters.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Cleared %1$d contact(s). 0 contact(s) remaining.";
    public static final String MESSAGE_EMPTY_CONTACT_FILE = "There are no contacts to clear.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ContactFile original = new ContactFile(model.getContactFile());
        int clearedCount = original.getPersonList().size();
        if (clearedCount == 0) {
            throw new CommandException(MESSAGE_EMPTY_CONTACT_FILE);
        }
        PendingRequest request = new ConfirmationRequest("Clear all " + clearedCount + " contact(s)?",
                new Command() {
                    @Override
                    public CommandResult execute(Model confirmedModel) throws CommandException {
                        if (!original.equals(confirmedModel.getContactFile())) {
                            throw new CommandException("Contacts changed. Start the clear request again.");
                        }
                        confirmedModel.setContactFile(new ContactFile());
                        confirmedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
                        return new CommandResult(String.format(MESSAGE_SUCCESS, clearedCount));
                    }
                });
        return new CommandResult(request.getPrompt(), request);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClearCommand;
    }
}
