package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.FollowUpEntry;
import seedu.address.model.Model;

/**
 * Displays pending follow-ups across all contacts in due-date order.
 */
public class PendingFollowUpsCommand extends Command {
    public static final String COMMAND_WORD = "followups";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists pending follow-ups across all contacts, earliest due dates first and undated actions last.\n"
            + "This command takes no arguments. Then enter a displayed number, another command, or n/cancel to stop.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_EMPTY = "No pending follow-ups remain.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<FollowUpEntry> entries = model.getPendingFollowUps();
        if (entries.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        PendingRequest request = new FollowUpSelectionRequest(entries);
        return new CommandResult(request.getPrompt(), request);
    }
}
