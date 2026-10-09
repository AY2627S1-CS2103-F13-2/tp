package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameMatchesExactlyPredicateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NameMatchesExactlyPredicate(null));
    }

    @Test
    public void equals() {
        NameMatchesExactlyPredicate firstPredicate = new NameMatchesExactlyPredicate(new Name("Alice Pauline"));
        NameMatchesExactlyPredicate secondPredicate = new NameMatchesExactlyPredicate(new Name("Benson Meier"));

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same name -> returns true
        assertTrue(firstPredicate.equals(new NameMatchesExactlyPredicate(new Name("Alice Pauline"))));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different name -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameMatchesExactly_returnsTrue() {
        // exact match
        NameMatchesExactlyPredicate predicate = new NameMatchesExactlyPredicate(new Name("Alice Pauline"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Pauline").build()));

        // differs only in case
        assertTrue(predicate.test(new PersonBuilder().withName("alice pauline").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("ALICE PAULINE").build()));
    }

    @Test
    public void test_nameDoesNotMatchExactly_returnsFalse() {
        NameMatchesExactlyPredicate predicate = new NameMatchesExactlyPredicate(new Name("Alice Pauline"));

        // only part of the name -> returns false, unlike a search
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Pauline").build()));

        // name is a superset of the given name -> returns false
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Pauline Tan").build()));

        // unrelated name -> returns false
        assertFalse(predicate.test(new PersonBuilder().withName("Benson Meier").build()));
    }

    @Test
    public void toStringMethod() {
        Name name = new Name("Alice Pauline");
        NameMatchesExactlyPredicate predicate = new NameMatchesExactlyPredicate(name);
        String expected = NameMatchesExactlyPredicate.class.getCanonicalName() + "{name=" + name + "}";
        assertEquals(expected, predicate.toString());
    }
}
