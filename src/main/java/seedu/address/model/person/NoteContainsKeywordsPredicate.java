package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that any of a {@code Person}'s interaction notes contains any of the given keywords, ignoring case.
 */
public class NoteContainsKeywordsPredicate implements Predicate<Person> {

    private final List<String> keywords;

    /**
     * Constructs a predicate that matches persons with a note containing any of {@code keywords}.
     */
    public NoteContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);
        assert !keywords.isEmpty() : "at least one keyword is needed to search notes";
        this.keywords = keywords.stream().map(keyword -> keyword.toLowerCase(Locale.ROOT)).toList();
    }

    @Override
    public boolean test(Person person) {
        return !matchingInteractions(person).isEmpty();
    }

    /**
     * Returns the interactions of {@code person} whose notes contain any of the keywords, in date order.
     */
    public List<Interaction> matchingInteractions(Person person) {
        return person.getInteractions().stream()
                .filter(interaction -> firstMatchIndex(interaction.note) >= 0)
                .toList();
    }

    /**
     * Returns the position of the earliest keyword match in {@code note}, or -1 if no keyword appears in it.
     */
    public int firstMatchIndex(String note) {
        String lowerCaseNote = note.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .mapToInt(lowerCaseNote::indexOf)
                .filter(index -> index >= 0)
                .min()
                .orElse(-1);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NoteContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
