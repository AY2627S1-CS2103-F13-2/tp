package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Matches a contact against one non-empty immutable set of case-sensitive tags.
 */
public final class TagFilterPredicate implements Predicate<Person> {
    private final Set<Tag> tags;
    private final Mode mode;

    /**
     * Creates a filter requiring all tags for AND or at least one tag for OR.
     */
    public TagFilterPredicate(Set<Tag> tags, Mode mode) {
        this.tags = Set.copyOf(tags);
        checkArgument(!this.tags.isEmpty(), "At least one tag is required.");
        this.mode = requireNonNull(mode);
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return mode == Mode.AND ? person.getTags().containsAll(tags)
                : tags.stream().anyMatch(person.getTags()::contains);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TagFilterPredicate predicate && tags.equals(predicate.tags) && mode == predicate.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, mode);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("tags", tags).add("mode", mode).toString();
    }

    /**
     * Defines whether every requested tag or any requested tag must match.
     */
    public enum Mode {
        AND, OR
    }
}
