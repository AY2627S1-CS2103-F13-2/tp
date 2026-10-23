package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.UndoManager;
import seedu.address.model.Model;

/**
 * Displays edit and deletion history for the current application session.
 */
public class HistoryCommand extends Command {
    public static final String COMMAND_WORD = "history";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Displays the edit and deletion history.";

    private final UndoManager undoManager;

    /**
     * Creates a history command.
     *
     * @param undoManager session history to display
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
