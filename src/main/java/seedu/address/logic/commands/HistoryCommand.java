package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.UndoManager;
import seedu.address.model.Model;

/**
 * Displays recorded contact changes for the current application session.
 */
public class HistoryCommand extends Command {
    public static final String COMMAND_WORD = "history";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays this session's additions, edits, deletions, drops, clears and introductions.";

    private final UndoManager undoManager;

    /**
     * Creates a history command.
     *
     * @param undoManager Session history to display.
     */
    public HistoryCommand(UndoManager undoManager) {
        requireNonNull(undoManager);
        this.undoManager = undoManager;
    }

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(undoManager.getHistory(), false, false, false);
    }
}
