package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Retains a numbered set of candidates and applies an action to the selected contact.
 */
public final class PersonSelectionRequest implements PendingRequest {

    static final String MESSAGE_SELECTION = "Enter the index of the contact, or n/cancel to stop.";

    private final List<Person> candidates;
    private final SelectionAction selectionAction;

    /** Continues a command after a contact has been chosen. */
    @FunctionalInterface
    public interface SelectionAction {
        CommandResult apply(Person person) throws CommandException;
    }

    /**
     * Copies candidates in display order and retains the action to perform for a selected contact.
     */
    public PersonSelectionRequest(List<Person> candidates, SelectionAction selectionAction) {
        this.candidates = List.copyOf(candidates);
        this.selectionAction = requireNonNull(selectionAction);
    }

    @Override
    public String getPrompt() {
        return candidates.size() + " contacts found:\n" + PersonNameResolver.formatNumberedList(candidates)
                + "\n" + MESSAGE_SELECTION;
    }

    @Override
    public CommandResult respond(String response, Model model) throws CommandException {
        if (StringUtil.isNonZeroUnsignedInteger(response)) {
            int index = Integer.parseInt(response) - 1;
            if (index < candidates.size()) {
                return selectionAction.apply(candidates.get(index));
            }
        }
        return new CommandResult("Enter a number from the displayed candidates.\n" + getPrompt(), this);
    }
}
