package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Removes one logged interaction from a contact, identified by its index in the displayed list or by name.
 */
public class UnlogCommand extends Command {

    public static final String COMMAND_WORD = "unlog";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes an interaction you logged by mistake, "
            + "after showing it and asking for y/n confirmation. "
            + "ENTRY is the interaction's number in the person's interaction history, as shown by view or log.\n"
            + "Parameters: INDEX (must be a positive integer) or NAME "
            + PREFIX_ENTRY + "ENTRY\n"
            + "Example: " + COMMAND_WORD + " John Lim " + PREFIX_ENTRY + "2";

    public static final String MESSAGE_PREVIEW = "Interaction %1$d of %2$s: %3$s";
    public static final String MESSAGE_CONFIRM_QUESTION = "Remove this interaction?";
    public static final String MESSAGE_CONTACT_CHANGED = "Contacts changed. Start the unlog request again.";
    public static final String MESSAGE_SUCCESS = "Removed interaction %1$s from %2$s.\n%3$s";
    public static final String MESSAGE_REMAINING_HISTORY = "Interaction history:\n%1$s";
    public static final String MESSAGE_NO_HISTORY_LEFT = "%1$s has no logged interactions left.";
    public static final String MESSAGE_NO_INTERACTIONS = "%1$s has no logged interactions to remove.";
    public static final String MESSAGE_INVALID_ENTRY =
            "There is no interaction %1$d. %2$s has %3$d logged interaction(s).";

    private static final Logger logger = LogsCenter.getLogger(UnlogCommand.class);

    private final Index index;
    private final String targetName;
    private final Index entry;

    /**
     * Creates an {@code UnlogCommand} that removes interaction number {@code entry} from the person at {@code index}.
     */
    public UnlogCommand(Index index, Index entry) {
        requireNonNull(index);
        requireNonNull(entry);
        this.index = index;
        this.targetName = null;
        this.entry = entry;
    }

    /**
     * Creates an {@code UnlogCommand} that removes interaction number {@code entry} from the person named
     * {@code targetName}.
     */
    public UnlogCommand(String targetName, Index entry) {
        requireNonNull(targetName);
        requireNonNull(entry);
        this.index = null;
        this.targetName = targetName;
        this.entry = entry;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person person;
        switch (PersonNameResolver.resolveTarget(model, index, targetName, COMMAND_WORD)) {
            case PersonNameResolver.Outcome.Unique unique -> person = unique.person();
            case PersonNameResolver.Outcome.Several several -> {
                return new CommandResult(several.message());
            }
        }

        List<Interaction> interactions = person.getInteractions();
        if (interactions.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_INTERACTIONS, person.getName()));
        }
        if (entry.getZeroBased() >= interactions.size()) {
            throw new CommandException(String.format(MESSAGE_INVALID_ENTRY, entry.getOneBased(), person.getName(),
                    interactions.size()));
        }

        // Removing an interaction cannot be undone, and entry numbers shift as the history is kept in
        // date order, so show the interaction and ask before removing it, as delete and clear do.
        Interaction toRemove = interactions.get(entry.getZeroBased());
        PendingRequest request = new ConfirmationRequest(
                String.format(MESSAGE_PREVIEW, entry.getOneBased(), person.getName(), toRemove),
                MESSAGE_CONFIRM_QUESTION, new Command() {
                    @Override
                    public CommandResult execute(Model confirmedModel) throws CommandException {
                        return removeInteraction(confirmedModel, person);
                    }
                });
        return new CommandResult(request.getPrompt(), request);
    }

    /**
     * Removes interaction number {@code entry} from {@code person}, who must still be stored unchanged.
     */
    private CommandResult removeInteraction(Model model, Person person) throws CommandException {
        if (!model.getContactFile().getPersonList().contains(person)) {
            throw new CommandException(MESSAGE_CONTACT_CHANGED);
        }

        List<Interaction> interactions = new ArrayList<>(person.getInteractions());
        Interaction removed = interactions.remove(entry.getZeroBased());
        Person updatedPerson = person.withInteractions(interactions);
        // Removing an interaction does not change who matches the current filter, so the displayed
        // list and its indexes are left as they are.
        model.setPerson(person, updatedPerson);
        logger.fine("Removed interaction from " + person.getName() + ": " + removed);

        String history = interactions.isEmpty()
                ? String.format(MESSAGE_NO_HISTORY_LEFT, person.getName())
                : String.format(MESSAGE_REMAINING_HISTORY, Messages.formatInteractions(interactions));
        return new CommandResult(String.format(MESSAGE_SUCCESS, removed, person.getName(), history));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UnlogCommand otherUnlogCommand)) {
            return false;
        }

        return Objects.equals(index, otherUnlogCommand.index)
                && Objects.equals(targetName, otherUnlogCommand.targetName)
                && entry.equals(otherUnlogCommand.entry);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("targetName", targetName)
                .add("entry", entry)
                .toString();
    }
}
