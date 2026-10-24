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
    public static final String CONFIRM_OPTION = "--confirm";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Previews the most recent edit or deletion. Add " + CONFIRM_OPTION + " to confirm.\n"
            + "Example: " + COMMAND_WORD + " " + CONFIRM_OPTION;

    private final UndoManager undoManager;
    private final boolean confirmed;

    /**
     * Creates an undo command.
     *
     * @param undoManager session history to query or update
     * @param confirmed whether the user confirmed the undo
     */
    public UndoCommand(UndoManager undoManager, boolean confirmed) {
        requireNonNull(undoManager);
        this.undoManager = undoManager;
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return undoManager.executeUndo(model, confirmed);
    }
}
