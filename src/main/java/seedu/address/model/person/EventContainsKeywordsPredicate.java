package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's events contain a matching event name.
 */
public class EventContainsKeywordsPredicate implements Predicate<Person> {

    private final String query;

    /**
     * Creates a predicate for an event name search.
     *
     * @param query event name to search for
     */
    public EventContainsKeywordsPredicate(String query) {
        requireNonNull(query);
        this.query = StringUtil.normalise(query).toLowerCase();
    }

    @Override
    public boolean test(Person person) {
        return person.getEvents().stream()
                .anyMatch(event -> event.getName()
                        .toLowerCase()
                        .contains(query));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EventContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return query.equals(otherPredicate.query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("query", query)
                .toString();
    }
}
