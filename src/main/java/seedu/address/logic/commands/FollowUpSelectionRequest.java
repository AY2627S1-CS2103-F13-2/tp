package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.Model;

/**
 * Retains the displayed follow-up order until the user selects an entry or cancels.
 */
final class FollowUpSelectionRequest implements PendingRequest {
    static final String MESSAGE_SELECTION = "Enter a follow-up number, or n/cancel to stop.";
    static final String MESSAGE_INVALID = "Enter a number from the displayed follow-ups.";
    static final String MESSAGE_CHANGED = "The selected contact or follow-up changed. Run followups again.";

    private final List<FollowUpEntry> entries;

    /**
     * Copies a nonempty list of entries in the order displayed to the user.
     */
    FollowUpSelectionRequest(List<FollowUpEntry> entries) {
        this.entries = List.copyOf(entries);
        checkArgument(!this.entries.isEmpty(), "A follow-up selection requires at least one entry.");
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getPrompt() {
        String rows = IntStream.range(0, entries.size()).mapToObj(index -> {
            FollowUpEntry entry = entries.get(index);
            return (index + 1) + ". " + entry.contact().getName() + " | " + Messages.formatFollowUp(entry.followUp());
        }).collect(Collectors.joining("\n"));
        return "Pending follow-ups (" + entries.size() + "):\n" + rows + "\n" + MESSAGE_SELECTION;
    }

    @Override
    public CommandResult respond(String response, Model model) throws CommandException {
        requireNonNull(response);
        requireNonNull(model);
        if (StringUtil.isNonZeroUnsignedInteger(response)) {
            int index = Integer.parseInt(response) - 1;
            if (index < entries.size()) {
                FollowUpEntry selected = entries.get(index);
                if (!model.getContactFile().getPersonList().contains(selected.contact())) {
                    throw new CommandException(MESSAGE_CHANGED);
                }
                return CommandResult.createFollowUpDetails(
                        Messages.formatFollowUpDetails(selected.contact(), selected.followUp(), model.getFieldOrder()),
                        selected.contact(), selected.followUp());
            }
        }
        return new CommandResult(MESSAGE_INVALID + "\n" + getPrompt(), this);
    }
}
