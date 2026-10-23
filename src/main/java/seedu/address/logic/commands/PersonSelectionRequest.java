package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

import seedu.address.commons.util.StringUtil;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Retains a numbered set of candidates and applies an action to the selected contact.
 */
public final class PersonSelectionRequest implements PendingRequest {

    private final List<Person> candidates;
    private final Function<Person, CommandResult> selectionAction;

    /**
     * Copies candidates in display order and retains the action to perform for a selected contact.
     */
    public PersonSelectionRequest(List<Person> candidates, Function<Person, CommandResult> selectionAction) {
        this.candidates = List.copyOf(candidates);
        this.selectionAction = requireNonNull(selectionAction);
    }

    @Override
    public String getPrompt() {
        return candidates.size() + " contacts found:\n" + PersonNameResolver.formatNumberedList(candidates)
                + "\nEnter a contact number, or n/cancel to stop.";
    }

    @Override
    public CommandResult respond(String response, Model model) {
        if (StringUtil.isNonZeroUnsignedInteger(response)) {
            int index = Integer.parseInt(response) - 1;
            if (index < candidates.size()) {
                return selectionAction.apply(candidates.get(index));
            }
        }
        return new CommandResult("Enter a number from the displayed candidates.\n" + getPrompt(), this);
    }
}
