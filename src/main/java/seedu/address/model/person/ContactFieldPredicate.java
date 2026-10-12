package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's phone number or email matches a search value exactly.
 */
public class ContactFieldPredicate implements Predicate<Person> {

    /**
     * The contact field to search.
     */
    public enum Field {
        PHONE,
        EMAIL
    }

    private final Field field;
    private final String query;

    /**
     * Creates a predicate for the specified contact field and query.
     *
     * @param field field to search
     * @param query normalized value to search for
     */
    public ContactFieldPredicate(Field field, String query) {
        this.field = requireNonNull(field);
        this.query = requireNonNull(query);
    }

    @Override
    public boolean test(Person person) {
        if (field == Field.PHONE) {
            return person.getPhone()
                    .map(phone -> phone.value.equals(query))
                    .orElse(false);
        }

        return person.getEmail()
                .map(email -> email.value.equalsIgnoreCase(query))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ContactFieldPredicate otherPredicate)) {
            return false;
        }

        return field == otherPredicate.field
                && Objects.equals(query, otherPredicate.query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .add("query", query)
                .toString();
    }
}
