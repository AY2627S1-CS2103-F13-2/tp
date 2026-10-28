package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.UndoManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Confirms or previews reversal of the most recent edit or deletion.
 */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Previews the most recent edit or deletion and asks for y/n confirmation.\n"
            + "Example: " + COMMAND_WORD;

    private final UndoManager undoManager;

    /**
     * Creates a command to preview and confirm undoing the latest supported change.
     *
     * @param undoManager session history to query or update
     */
    public UndoCommand(UndoManager undoManager) {
        requireNonNull(undoManager);
        this.undoManager = undoManager;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return undoManager.executeUndo();
    }
}
