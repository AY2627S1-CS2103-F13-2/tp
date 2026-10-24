package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class EventContainsKeywordsPredicateTest {

    @Test
    public void test_matchingEventName_returnsTrue() {
        Person person = new PersonBuilder()
                .build()
                .withEvent(new Event("NUS Career Fair", "2026-10-20"));

        EventContainsKeywordsPredicate predicate =
                new EventContainsKeywordsPredicate("career");

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_matchingEventNameIsCaseInsensitive_returnsTrue() {
        Person person = new PersonBuilder()
                .build()
                .withEvent(new Event("NUS Career Fair", "2026-10-20"));

        EventContainsKeywordsPredicate predicate =
                new EventContainsKeywordsPredicate("CAREER FAIR");

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_nonMatchingEventName_returnsFalse() {
        Person person = new PersonBuilder()
                .build()
                .withEvent(new Event("NUS Career Fair", "2026-10-20"));

        EventContainsKeywordsPredicate predicate =
                new EventContainsKeywordsPredicate("conference");

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_personWithoutEvents_returnsFalse() {
        Person person = new PersonBuilder().build();
        EventContainsKeywordsPredicate predicate =
                new EventContainsKeywordsPredicate("career");

        assertFalse(predicate.test(person));
    }

    @Test
    public void equals() {
        EventContainsKeywordsPredicate firstPredicate =
                new EventContainsKeywordsPredicate("career");
        EventContainsKeywordsPredicate secondPredicate =
                new EventContainsKeywordsPredicate("conference");

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(
                new EventContainsKeywordsPredicate("CAREER")));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }

    @Test
    public void toStringMethod() {
        EventContainsKeywordsPredicate predicate =
                new EventContainsKeywordsPredicate("career");

        String expected = EventContainsKeywordsPredicate.class.getCanonicalName()
                + "{query=career}";

        assertEquals(expected, predicate.toString());
    }
}
