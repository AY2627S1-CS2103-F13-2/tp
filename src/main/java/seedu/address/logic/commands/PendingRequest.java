package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a request awaiting a reply. Logic handles cancellation and transaction rollback.
 */
public interface PendingRequest {

    /**
     * Returns the instructions to display while this request is pending.
     */
    String getPrompt();

    /**
     * Processes a non-cancellation reply, returning another pending result or a completed result.
     *
     * @throws CommandException if the confirmed operation cannot be performed.
     */
    CommandResult respond(String response, Model model) throws CommandException;
}
