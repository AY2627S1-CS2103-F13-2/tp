package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Resolves a name typed by the user to the one contact it names.
 *
 * <p>Names are matched exactly the way {@code find} matches them, by delegating to
 * {@link NameContainsKeywordsPredicate}, so that naming a contact means the same thing everywhere in
 * Astra. Every command that lets the user name a contact instead of giving its index goes through
 * here, so the rule cannot drift between commands as more of them gain the ability.
 *
 * <p>Because a name need not be given in full, it can match several contacts. When it does, the
 * caller is handed a numbered list of the matches to show the user, rather than being left to act on
 * a guess.
 */
public final class PersonNameResolver {

    public static final String MESSAGE_NAME_NOT_FOUND = "No contact named %1$s found.";
    public static final String MESSAGE_SEVERAL_MATCHES = "%1$d contacts found:\n%2$s\n"
            + "To %3$s one of them, enter the command again with its index in place of the name.";

    private PersonNameResolver() {
        // Utility class; all behaviour is in the static resolve method.
    }

    /**
     * What a name turned out to refer to. A name that matches nothing is reported as a
     * {@code CommandException} instead, since there is no contact for the caller to act on.
     */
    public sealed interface Outcome {

        /** Exactly one contact matched, and it is the one the user meant. */
        record Unique(Person person) implements Outcome {}

        /**
         * Several contacts matched, so the user has to say which. {@code message} lists them and
         * asks the user to run the command again with an index.
         */
        record Several(String message) implements Outcome {}
    }

    /**
     * Returns the contact named by {@code targetName}, searching every contact in the address book
     * rather than only the ones on display, so that naming someone in full reaches them even while a
     * {@code find} filter is hiding them.
     *
     * <p>As a side effect the displayed list is filtered down to the matches, so that the contacts
     * being talked about are the contacts on screen. A caller that goes on to change the address book
     * is expected to restore the full list afterwards; when no contact matches, the full list is
     * restored here, since there is nothing left to show.
     *
     * @param model address book to search, and whose displayed list is filtered to the matches
     * @param targetName name as the user typed it, matched case-insensitively and need not be in full
     * @param commandWord word the user typed, used to tell them how to retry when the name is ambiguous
     * @throws CommandException if no contact matches {@code targetName}
     */
    public static Outcome resolve(Model model, String targetName, String commandWord) throws CommandException {
        requireNonNull(model);
        requireNonNull(targetName);
        requireNonNull(commandWord);

        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(targetName)));
        List<Person> matches = List.copyOf(model.getFilteredPersonList());

        if (matches.isEmpty()) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            throw new CommandException(String.format(MESSAGE_NAME_NOT_FOUND, targetName));
        }

        if (matches.size() > 1) {
            return new Outcome.Several(String.format(MESSAGE_SEVERAL_MATCHES,
                    matches.size(), formatNumberedList(matches), commandWord));
        }

        return new Outcome.Unique(matches.get(0));
    }

    /**
     * Returns the given contacts as numbered lines of their name, company and email, skipping details
     * they do not have, e.g. "1. John Lim - Google - john.lim@example.com", separated by em dashes. Name alone is
     * rarely enough to tell two matches apart, which is the whole reason the list is being shown.
     */
    private static String formatNumberedList(List<Person> persons) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            if (i > 0) {
                list.append("\n");
            }
            list.append(i + 1).append(". ").append(person.getName());
            person.getCompany().ifPresent(company -> list.append(" \u2014 ").append(company));
            person.getEmail().ifPresent(email -> list.append(" \u2014 ").append(email));
        }
        return list.toString();
    }
}
