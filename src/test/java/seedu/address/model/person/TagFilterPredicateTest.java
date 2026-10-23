package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.TagFilterPredicate.Mode;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

class TagFilterPredicateTest {
    private static final Set<Tag> TAGS = Set.of(new Tag("work"), new Tag("friend"));

    @Test
    void test_and_requiresEveryRequestedTag() {
        TagFilterPredicate predicate = new TagFilterPredicate(TAGS, Mode.AND);
        assertTrue(predicate.test(new PersonBuilder().withTags("friend", "work", "extra").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("work").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("friend").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("other").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags().build()));
    }

    @Test
    void test_or_requiresAtLeastOneRequestedTag() {
        TagFilterPredicate predicate = new TagFilterPredicate(TAGS, Mode.OR);
        assertTrue(predicate.test(new PersonBuilder().withTags("work", "friend").build()));
        assertTrue(predicate.test(new PersonBuilder().withTags("work").build()));
        assertTrue(predicate.test(new PersonBuilder().withTags("friend", "extra").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("other").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags().build()));
    }

    @Test
    void test_singleTag_preservesCaseSensitiveWholeTagEquality() {
        for (Mode mode : Mode.values()) {
            TagFilterPredicate predicate = new TagFilterPredicate(Set.of(new Tag("work")), mode);
            assertTrue(predicate.test(new PersonBuilder().withTags("work").build()));
            assertFalse(predicate.test(new PersonBuilder().withTags("Work").build()));
            assertFalse(predicate.test(new PersonBuilder().withTags("workplace").build()));
            assertFalse(predicate.test(new PersonBuilder().withTags().build()));
        }
    }

    @Test
    void constructor_inputMutation_doesNotChangeFilter() {
        Set<Tag> input = new HashSet<>(TAGS);
        TagFilterPredicate predicate = new TagFilterPredicate(input, Mode.AND);
        input.clear();
        assertTrue(predicate.test(new PersonBuilder().withTags("work", "friend").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("work").build()));
    }

    @Test
    void constructor_invalidArguments_rejectsEmptyAndNullValues() {
        assertThrows(IllegalArgumentException.class, () -> new TagFilterPredicate(Set.of(), Mode.AND));
        assertThrows(NullPointerException.class, () -> new TagFilterPredicate(null, Mode.AND));
        assertThrows(NullPointerException.class, () -> new TagFilterPredicate(TAGS, null));
        Set<Tag> invalid = new HashSet<>();
        invalid.add(null);
        assertThrows(NullPointerException.class, () -> new TagFilterPredicate(invalid, Mode.OR));
        assertThrows(NullPointerException.class, () -> new TagFilterPredicate(TAGS, Mode.AND).test(null));
    }

    @Test
    void equals_modesAndUnorderedTags_defineFilterIdentity() {
        TagFilterPredicate predicate = new TagFilterPredicate(TAGS, Mode.AND);
        TagFilterPredicate copy = new TagFilterPredicate(Set.of(new Tag("friend"), new Tag("work")), Mode.AND);
        assertEquals(predicate, predicate);
        assertEquals(predicate, copy);
        assertEquals(predicate.hashCode(), copy.hashCode());
        assertEquals(Mode.AND, predicate.getMode());
        assertNotEquals(predicate, new TagFilterPredicate(TAGS, Mode.OR));
        assertNotEquals(predicate, new TagFilterPredicate(Set.of(new Tag("work")), Mode.AND));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, TAGS);
        assertTrue(predicate.toString().contains("mode=AND"));
        assertTrue(predicate.toString().contains("[friend]"));
    }
}
