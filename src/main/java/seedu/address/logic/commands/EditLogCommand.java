package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ENTRY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.LocalDate;
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
 * Corrects the date or note of one logged interaction of a contact, identified by its index in the displayed
 * list or by name.
 */
public class EditLogCommand extends Command {

    public static final String COMMAND_WORD = "editlog";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the date or note of an interaction you logged. "
            + "ENTRY is the interaction's number in the person's interaction history, as shown by view or log. "
            + "At least one of DATE or NOTE must be given.\n"
            + "Parameters: INDEX (must be a positive integer) or NAME "
            + PREFIX_ENTRY + "ENTRY "
            + "[" + PREFIX_DATE + "DATE] "
            + "[" + PREFIX_NOTE + "NOTE]\n"
            + "Date formats: yyyy-MM-dd, dd/MM/yyyy or d MMM yyyy (English month names); "
            + "dates must be on or after 2000-01-01.\n"
            + "Example: " + COMMAND_WORD + " John Lim " + PREFIX_ENTRY + "2 "
            + PREFIX_NOTE + "Discussed frontend internships";

    public static final String MESSAGE_SUCCESS = "Edited interaction %1$d of %2$s: %3$s\nInteraction history:\n%4$s";
    public static final String MESSAGE_NO_INTERACTIONS = "%1$s has no logged interactions to edit.";
    public static final String MESSAGE_NOT_CHANGED = "Interaction %1$d of %2$s already reads: %3$s";
    public static final String MESSAGE_DUPLICATE_INTERACTION =
            "%1$s already has this interaction recorded: %2$s. No changes were applied.";

    private static final Logger logger = LogsCenter.getLogger(EditLogCommand.class);

    private final Index index;
    private final String targetName;
    private final Index entry;
    private final LocalDate date;
    private final String note;

    /**
     * Creates an {@code EditLogCommand} for interaction number {@code entry} of the person at {@code index}.
     * A null {@code date} or {@code note} keeps the current value; at least one must be given.
     */
    public EditLogCommand(Index index, Index entry, LocalDate date, String note) {
        this(requireNonNull(index), null, entry, date, note);
    }

    /**
     * Creates an {@code EditLogCommand} for interaction number {@code entry} of the person named
     * {@code targetName}. A null {@code date} or {@code note} keeps the current value; at least one must be given.
     */
    public EditLogCommand(String targetName, Index entry, LocalDate date, String note) {
        this(null, requireNonNull(targetName), entry, date, note);
    }

    private EditLogCommand(Index index, String targetName, Index entry, LocalDate date, String note) {
        requireNonNull(entry);
        checkArgument(date != null || note != null, "An interaction edit must change the date or the note.");
        checkArgument(note == null || Interaction.isValidNote(note), Interaction.MESSAGE_NOTE_CONSTRAINTS);
        this.index = index;
        this.targetName = targetName;
        this.entry = entry;
        this.date = date;
        this.note = note;
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
            throw new CommandException(String.format(UnlogCommand.MESSAGE_INVALID_ENTRY, entry.getOneBased(),
                    person.getName(), interactions.size()));
        }

        Interaction existing = interactions.get(entry.getZeroBased());
        Interaction edited = new Interaction(date == null ? existing.getDate() : date,
                note == null ? existing.getNote() : note);
        if (edited.equals(existing)) {
            throw new CommandException(String.format(MESSAGE_NOT_CHANGED, entry.getOneBased(), person.getName(),
                    existing));
        }
        for (int i = 0; i < interactions.size(); i++) {
            if (i != entry.getZeroBased() && interactions.get(i).isSameInteraction(edited)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_INTERACTION, person.getName(),
                        interactions.get(i)));
            }
        }

        List<Interaction> updatedInteractions = new ArrayList<>(interactions);
        updatedInteractions.set(entry.getZeroBased(), edited);
        Person updatedPerson = person.withInteractions(updatedInteractions);
        InteractionCommandUtil.setPersonPreservingView(model, person, updatedPerson);
        logger.fine("Edited interaction of " + person.getName() + ": " + existing + " -> " + edited);

        // The history is kept in date order, so a new date can move the interaction to another number.
        int newEntry = updatedPerson.getInteractions().indexOf(edited) + 1;
        return new CommandResult(String.format(MESSAGE_SUCCESS, newEntry, person.getName(), edited,
                Messages.formatInteractions(updatedPerson.getInteractions())));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditLogCommand otherEditLogCommand)) {
            return false;
        }

        return Objects.equals(index, otherEditLogCommand.index)
                && Objects.equals(targetName, otherEditLogCommand.targetName)
                && entry.equals(otherEditLogCommand.entry)
                && Objects.equals(date, otherEditLogCommand.date)
                && Objects.equals(note, otherEditLogCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("targetName", targetName)
                .add("entry", entry)
                .add("date", date)
                .add("note", note)
                .toString();
    }
}
