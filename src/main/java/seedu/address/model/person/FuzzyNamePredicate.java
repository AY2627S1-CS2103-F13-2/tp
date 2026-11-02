package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's name is approximately similar to a search query.
 */
public class FuzzyNamePredicate implements Predicate<Person> {

    private static final int MAX_DISTANCE = 2;

    private final String query;

    /**
     * Creates a fuzzy name predicate.
     *
     * @param query name to search for
     */
    public FuzzyNamePredicate(String query) {
        requireNonNull(query);
        this.query = normalize(query);
    }

    @Override
    public boolean test(Person person) {
        String personName = normalize(person.getName().fullName);

        if (query.isEmpty()) {
            return false;
        }

        // A query no longer than the allowed distance could be rewritten into any short word,
        // so at least one of its letters must survive for a word to count as a match.
        int maxDistance = Math.min(MAX_DISTANCE, query.length() - 1);
        if (personName.contains(query)
                || levenshteinDistance(personName, query) <= maxDistance) {
            return true;
        }

        return java.util.Arrays.stream(personName.split(" "))
                .anyMatch(word -> levenshteinDistance(word, query) <= maxDistance);
    }

    private static String normalize(String value) {
        return value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private static int levenshteinDistance(String first, String second) {
        int[][] distances = new int[first.length() + 1][second.length() + 1];

        for (int i = 0; i <= first.length(); i++) {
            distances[i][0] = i;
        }

        for (int j = 0; j <= second.length(); j++) {
            distances[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int substitutionCost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;

                distances[i][j] = Math.min(
                        Math.min(
                                distances[i - 1][j] + 1,
                                distances[i][j - 1] + 1),
                        distances[i - 1][j - 1] + substitutionCost);
            }
        }

        return distances[first.length()][second.length()];
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FuzzyNamePredicate otherPredicate)) {
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
