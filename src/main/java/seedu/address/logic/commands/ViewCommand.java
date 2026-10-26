package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays all stored information about a person identified by displayed index or name.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_CONTACT_CHANGED = "Contact changed. Run view again.";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays all information about the person identified by the index number "
            + "used in the displayed person list, or by a partial, case-insensitive name.\n"
            + "Parameters: INDEX (must be a positive integer) or NAME\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "Example: " + COMMAND_WORD + " Alice Pauline";

    // Exactly one selector is set. A resolved contact bypasses name/index lookup.
    private final Index targetIndex;
    private final String targetName;
    private final Person targetPerson;

    /**
     * Creates a ViewCommand to display the person at the specified index.
     *
     * @param targetIndex Index of the person in the filtered person list to view.
     */
    public ViewCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.targetName = null;
        this.targetPerson = null;
    }

    /**
     * Creates a ViewCommand to display a person using shared partial-name matching.
     *
     * @param targetName Name to resolve across all stored contacts.
     */
    public ViewCommand(String targetName) {
        this.targetIndex = null;
        this.targetName = requireNonNull(targetName);
        this.targetPerson = null;
    }

    private ViewCommand(Person targetPerson) {
        this.targetIndex = null;
        this.targetName = null;
        this.targetPerson = requireNonNull(targetPerson);
    }

    /**
     * Views an already selected contact without repeating lookup or changing the contact filter.
     */
    public static ViewCommand forPerson(Person person) {
        return new ViewCommand(person);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToView = targetPerson;
        if (targetPerson != null) {
            if (!model.getContactFile().getPersonList().contains(targetPerson)) {
                throw new CommandException(MESSAGE_CONTACT_CHANGED);
            }
        } else {
            switch (PersonNameResolver.resolveTarget(model, targetIndex, targetName, COMMAND_WORD)) {
                case PersonNameResolver.Outcome.Unique unique -> personToView = unique.person();
                case PersonNameResolver.Outcome.Several several -> {
                    return new CommandResult(several.message());
                }
            }
        }

        return CommandResult.createPersonDetails(Messages.format(personToView, model.getFieldOrder()), personToView);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherViewCommand.targetIndex)
                && Objects.equals(targetName, otherViewCommand.targetName)
                && Objects.equals(targetPerson, otherViewCommand.targetPerson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetName", targetName)
                .add("targetPerson", targetPerson)
                .toString();
    }
}
