package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's contact field matches a search value.
 */
public class ContactFieldPredicate implements Predicate<Person> {

    /**
     * The contact field to search.
     */
    public enum Field {
        PHONE,
        EMAIL,
        COMPANY,
        ROLE
    }

    private final Field field;
    private final String query;

    /**
     * Creates a predicate for the specified contact field and query.
     *
     * @param field field to search
     * @param query value to search for
     */
    public ContactFieldPredicate(Field field, String query) {
        this.field = requireNonNull(field);
        this.query = requireNonNull(query).trim();
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);

        if (field == Field.PHONE) {
            return person.getPhone()
                    .map(phone -> phone.value.equals(query))
                    .orElse(false);
        }

        if (field == Field.EMAIL) {
            return person.getEmail()
                    .map(email -> email.value.equalsIgnoreCase(query))
                    .orElse(false);
        }

        if (field == Field.COMPANY) {
            return person.getCompany()
                    .map(company -> containsIgnoreCase(company.value))
                    .orElse(false);
        }

        return person.getRole()
                .map(role -> containsIgnoreCase(role.value))
                .orElse(false);
    }

    /**
     * Returns true if the stored value contains the search query,
     * ignoring letter case.
     */
    private boolean containsIgnoreCase(String value) {
        return value.toLowerCase(Locale.ROOT)
                .contains(query.toLowerCase(Locale.ROOT));
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
