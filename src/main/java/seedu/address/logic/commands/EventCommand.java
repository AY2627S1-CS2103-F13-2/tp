package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;

/**
 * Adds an event to a contact.
 */
public class EventCommand extends Command {

    public static final String COMMAND_WORD = "event";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an event to a contact.\n"
            + "Parameters: add INDEX ev/EVENT\n"
            + "Example: " + COMMAND_WORD
            + " add 1 ev/NUS Career Fair";

    public static final String MESSAGE_SUCCESS =
            "Added event %1$s to contact: %2$s";

    public static final String MESSAGE_DUPLICATE_EVENT =
            "This contact is already associated with event %1$s.";

    private final Index targetIndex;
    private final Event event;

    /**
     * Creates an EventCommand.
     *
     * @param targetIndex index of the target contact
     * @param event event to add
     */
    public EventCommand(Index targetIndex, Event event) {
        requireNonNull(targetIndex);
        requireNonNull(event);

        this.targetIndex = targetIndex;
        this.event = event;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person targetPerson = lastShownList.get(targetIndex.getZeroBased());

        if (targetPerson.hasEvent(event)) {
            throw new CommandException(
                    String.format(MESSAGE_DUPLICATE_EVENT, event));
        }

        Person updatedPerson = targetPerson.withEvent(event);
        model.setPerson(targetPerson, updatedPerson);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(
                String.format(
                        MESSAGE_SUCCESS,
                        event,
                        Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EventCommand otherEventCommand)) {
            return false;
        }

        return targetIndex.equals(otherEventCommand.targetIndex)
                && event.equals(otherEventCommand.event);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("event", event)
                .toString();
    }
}
