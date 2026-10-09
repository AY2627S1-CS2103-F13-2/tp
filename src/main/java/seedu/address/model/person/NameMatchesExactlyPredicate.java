package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name} is exactly the given name, ignoring case.
 * Unlike {@link NameContainsKeywordsPredicate}, which is used to search, this is used to
 * single out one person the user has named in full.
 */
public class NameMatchesExactlyPredicate implements Predicate<Person> {
    private final Name name;

    /**
     * Constructs a predicate that matches persons whose name is exactly {@code name}.
     */
    public NameMatchesExactlyPredicate(Name name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public boolean test(Person person) {
        return person.getName().fullName.equalsIgnoreCase(name.fullName);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameMatchesExactlyPredicate otherPredicate)) {
            return false;
        }

        return name.equals(otherPredicate.name);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("name", name).toString();
    }
}
