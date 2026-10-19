package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Defines, replaces, removes or lists command shortcuts.
 * A shortcut is a single word that Astra expands into a longer command before running it.
 */
public class AliasCommand extends Command {

    public static final String COMMAND_WORD = "alias";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Defines a shortcut for a command you use often.\n"
            + "Parameters: SHORTCUT COMMAND...\n"
            + "Example: " + COMMAND_WORD + " fn find /notes\n"
            + "Other forms: " + COMMAND_WORD + " (lists your shortcuts), "
            + COMMAND_WORD + " /replace SHORTCUT COMMAND..., " + COMMAND_WORD + " /remove SHORTCUT";

    public static final String MESSAGE_SAVED = "Saved shortcut %1$s. Entering %1$s now runs: %2$s";
    public static final String MESSAGE_REPLACED = "Replaced shortcut %1$s.\nIt used to run: %2$s\nIt now runs: %3$s";
    public static final String MESSAGE_REMOVED = "Removed shortcut %1$s (it ran: %2$s).";
    public static final String MESSAGE_ALREADY_EXISTS = "Shortcut %1$s already runs: %2$s\n"
            + "No changes were applied. To replace it, enter: " + COMMAND_WORD + " /replace %1$s %3$s";
    public static final String MESSAGE_NOT_FOUND = "There is no shortcut named %1$s.";
    public static final String MESSAGE_NO_SHORTCUTS = "You have no shortcuts yet. Define one with e.g. "
            + COMMAND_WORD + " fn find /notes";
    public static final String MESSAGE_LIST = "Your shortcuts:\n%1$s";

    /**
     * What the command does with the shortcut.
     */
    public enum Action { LIST, ADD, REPLACE, REMOVE }

    private static final Logger logger = LogsCenter.getLogger(AliasCommand.class);

    private final Action action;
    private final String shortcut;
    private final String expansion;

    private AliasCommand(Action action, String shortcut, String expansion) {
        this.action = action;
        this.shortcut = shortcut;
        this.expansion = expansion;
    }

    /**
     * Returns a command that lists all shortcuts.
     */
    public static AliasCommand list() {
        return new AliasCommand(Action.LIST, null, null);
    }

    /**
     * Returns a command that defines {@code shortcut} as {@code expansion}, refusing to replace an existing one.
     */
    public static AliasCommand add(String shortcut, String expansion) {
        requireNonNull(shortcut);
        requireNonNull(expansion);
        return new AliasCommand(Action.ADD, shortcut, expansion);
    }

    /**
     * Returns a command that defines {@code shortcut} as {@code expansion}, replacing any existing expansion.
     */
    public static AliasCommand replace(String shortcut, String expansion) {
        requireNonNull(shortcut);
        requireNonNull(expansion);
        return new AliasCommand(Action.REPLACE, shortcut, expansion);
    }

    /**
     * Returns a command that removes {@code shortcut}.
     */
    public static AliasCommand remove(String shortcut) {
        requireNonNull(shortcut);
        return new AliasCommand(Action.REMOVE, shortcut, null);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Map<String, String> aliases = model.getCommandAliases();
        String current = shortcut == null ? null : aliases.get(shortcut);

        switch (action) {
            case LIST:
                if (aliases.isEmpty()) {
                    return new CommandResult(MESSAGE_NO_SHORTCUTS);
                }
                return new CommandResult(String.format(MESSAGE_LIST, formatAliases(aliases)));
            case REMOVE:
                if (current == null) {
                    throw new CommandException(String.format(MESSAGE_NOT_FOUND, shortcut));
                }
                model.removeCommandAlias(shortcut);
                logger.fine("Removed shortcut " + shortcut);
                return new CommandResult(String.format(MESSAGE_REMOVED, shortcut, current));
            case ADD:
                if (current != null) {
                    // Never overwrite silently: show what the shortcut runs now and how to replace it.
                    throw new CommandException(String.format(MESSAGE_ALREADY_EXISTS, shortcut, current, expansion));
                }
                return save(model);
            case REPLACE:
                if (current == null) {
                    return save(model);
                }
                model.setCommandAlias(shortcut, expansion);
                logger.fine("Replaced shortcut " + shortcut + " with: " + expansion);
                return new CommandResult(String.format(MESSAGE_REPLACED, shortcut, current, expansion));
            default:
                throw new AssertionError("Unknown alias action: " + action);
        }
    }

    private CommandResult save(Model model) {
        model.setCommandAlias(shortcut, expansion);
        assert expansion.equals(model.getCommandAliases().get(shortcut));
        logger.fine("Saved shortcut " + shortcut + " for: " + expansion);
        return new CommandResult(String.format(MESSAGE_SAVED, shortcut, expansion));
    }

    private static String formatAliases(Map<String, String> aliases) {
        return aliases.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AliasCommand otherCommand)) {
            return false;
        }

        return action == otherCommand.action
                && Objects.equals(shortcut, otherCommand.shortcut)
                && Objects.equals(expansion, otherCommand.expansion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, shortcut, expansion);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("action", action)
                .add("shortcut", shortcut)
                .add("expansion", expansion)
                .toString();
    }
}
