package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FuzzyNamePredicateTest {

    @Test
    public void test_approximatelyMatchingName_returnsTrue() {
        Person person = new PersonBuilder()
                .withName("Alicia Tan")
                .build();

        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("Alicee");

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_approximatelyMatchingFullName_returnsTrue() {
        Person person = new PersonBuilder()
                .withName("Alice Pauline")
                .build();

        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("Alicee Pauline");

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_matchingNameIsCaseInsensitive_returnsTrue() {
        Person person = new PersonBuilder()
                .withName("Alicia Tan")
                .build();

        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("ALICIA");

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_nonMatchingName_returnsFalse() {
        Person person = new PersonBuilder()
                .withName("Alicia Tan")
                .build();

        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("Zachary");

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_emptyQuery_returnsFalse() {
        Person person = new PersonBuilder()
                .withName("Alicia Tan")
                .build();

        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("");

        assertFalse(predicate.test(person));
    }

    @Test
    public void equals() {
        FuzzyNamePredicate firstPredicate =
                new FuzzyNamePredicate("Alicia");
        FuzzyNamePredicate secondPredicate =
                new FuzzyNamePredicate("Zachary");

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(
                new FuzzyNamePredicate("ALICIA")));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }

    @Test
    public void toStringMethod() {
        FuzzyNamePredicate predicate =
                new FuzzyNamePredicate("Alicia");

        String expected = FuzzyNamePredicate.class.getCanonicalName()
                + "{query=alicia}";

        assertEquals(expected, predicate.toString());
    }
}
