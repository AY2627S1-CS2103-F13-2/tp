package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

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
 * Records an interaction with a contact, identified by its index in the displayed list or by name.
 */
public class LogCommand extends Command {

    public static final String COMMAND_WORD = "log";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Records an interaction with the person identified "
            + "by the index number used in the displayed person list, or by name. "
            + "The date defaults to today.\n"
            + "Parameters: INDEX (must be a positive integer) or NAME "
            + "[" + PREFIX_DATE + "DATE] "
            + PREFIX_NOTE + "NOTE\n"
            + "Example: " + COMMAND_WORD + " John Lim " + PREFIX_DATE + "2026-10-15 "
            + PREFIX_NOTE + "Discussed backend internships";

    public static final String MESSAGE_SUCCESS = "Logged an interaction with %1$s on %2$s.\nInteraction history:\n%3$s";
    public static final String MESSAGE_DUPLICATE_INTERACTION =
            "This interaction is already recorded for %1$s. No changes were applied.";

    private static final Logger logger = LogsCenter.getLogger(LogCommand.class);

    private final Index index;
    private final String targetName;
    private final Interaction interaction;

    /**
     * Creates a {@code LogCommand} that records {@code interaction} for the person at {@code index}.
     */
    public LogCommand(Index index, Interaction interaction) {
        requireNonNull(index);
        requireNonNull(interaction);
        this.index = index;
        this.targetName = null;
        this.interaction = interaction;
    }

    /**
     * Creates a {@code LogCommand} that records {@code interaction} for the person named {@code targetName}.
     */
    public LogCommand(String targetName, Interaction interaction) {
        requireNonNull(targetName);
        requireNonNull(interaction);
        this.index = null;
        this.targetName = targetName;
        this.interaction = interaction;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person person;
        if (targetName == null) {
            List<Person> lastShownList = model.getFilteredPersonList();
            if (index.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            person = lastShownList.get(index.getZeroBased());
        } else {
            switch (PersonNameResolver.resolve(model, targetName, COMMAND_WORD)) {
                case PersonNameResolver.Outcome.Unique unique -> person = unique.person();
                case PersonNameResolver.Outcome.Several several -> {
                    return new CommandResult(several.message());
                }
            }
        }

        if (person.hasInteraction(interaction)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_INTERACTION, person.getName()));
        }

        Person updatedPerson = person.withInteraction(interaction);
        assert updatedPerson.getInteractions().size() == person.getInteractions().size() + 1;
        model.setPerson(person, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        logger.fine("Logged interaction for " + person.getName() + ": " + interaction);

        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), interaction.date,
                formatHistory(updatedPerson.getInteractions())));
    }

    /**
     * Returns the interactions as numbered lines in the given order, e.g. "1. 2026-10-15: Discussed internships".
     */
    static String formatHistory(List<Interaction> interactions) {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < interactions.size(); i++) {
            if (i > 0) {
                history.append("\n");
            }
            history.append(i + 1).append(". ").append(interactions.get(i));
        }
        return history.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LogCommand otherLogCommand)) {
            return false;
        }

        return Objects.equals(index, otherLogCommand.index)
                && Objects.equals(targetName, otherLogCommand.targetName)
                && interaction.equals(otherLogCommand.interaction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("targetName", targetName)
                .add("interaction", interaction)
                .toString();
    }
}
