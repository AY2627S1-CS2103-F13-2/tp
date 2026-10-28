package seedu.address.logic.commands;

import java.util.function.Predicate;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Keeps the contact selected by an interaction command visible even when its notes stop matching a search.
 */
final class InteractionCommandUtil {
    private InteractionCommandUtil() {
    }

    /**
     * Retains the target in the current view before replacing it, preserving displayed contact indexes.
     * The identity check also retains it after later changes such as starring. An explicit search or list
     * command replaces this predicate and evaluates the requested criteria again.
     */
    static void setPersonPreservingView(Model model, Person original, Person updated) {
        Predicate<Person> filter = model.getPersonFilter();
        if (filter != null && !filter.test(updated)) {
            model.updateFilteredPersonList(person -> person.isSamePerson(updated) || filter.test(person));
        }
        model.setPerson(original, updated);
    }
}
