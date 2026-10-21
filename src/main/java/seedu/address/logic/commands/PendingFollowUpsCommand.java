package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.logic.Messages;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.Model;

/**
 * Displays pending follow-ups across all contacts in due-date order.
 */
public class PendingFollowUpsCommand extends Command {
    public static final String COMMAND_WORD = "followups";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists pending follow-ups across all contacts, earliest due dates first and undated actions last.\n"
            + "This command takes no arguments.\nExample: " + COMMAND_WORD;
    public static final String MESSAGE_EMPTY = "No pending follow-ups remain.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<FollowUpEntry> entries = model.getPendingFollowUps();
        if (entries.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        String rows = IntStream.range(0, entries.size()).mapToObj(index -> {
            FollowUpEntry entry = entries.get(index);
            return (index + 1) + ". " + entry.contact().getName() + " | " + Messages.formatFollowUp(entry.followUp());
        }).collect(Collectors.joining("\n"));
        return new CommandResult("Pending follow-ups (" + entries.size() + "):\n" + rows);
    }
}
