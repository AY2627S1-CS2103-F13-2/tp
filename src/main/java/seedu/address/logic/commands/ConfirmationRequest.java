package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Defers an operation until the user replies y. Logic handles n and cancel before dispatch.
 */
public final class ConfirmationRequest implements PendingRequest {

    private final String prompt;
    private final Command confirmedCommand;

    /**
     * Creates a confirmation for an already resolved operation.
     */
    public ConfirmationRequest(String preview, Command confirmedCommand) {
        this(preview, "Confirm?", confirmedCommand);
    }

    /**
     * Creates a confirmation with a question specific to the resolved operation.
     */
    public ConfirmationRequest(String preview, String question, Command confirmedCommand) {
        prompt = requireNonNull(preview) + "\n" + requireNonNull(question) + " (y/n; cancel to stop)";
        this.confirmedCommand = requireNonNull(confirmedCommand);
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public CommandResult respond(String response, Model model) throws CommandException {
        if (response.equalsIgnoreCase("y")) {
            return confirmedCommand.execute(model);
        }
        return new CommandResult("Enter y or n, or cancel.\n" + prompt, this);
    }
}
