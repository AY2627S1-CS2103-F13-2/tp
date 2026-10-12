package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ContactFieldPredicateTest {

    @Test
    public void test_phoneMatches_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(ContactFieldPredicate.Field.PHONE, "91234567");

        Person person = new PersonBuilder()
                .withPhone("91234567")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_phoneDoesNotMatch_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(ContactFieldPredicate.Field.PHONE, "91234567");

        Person person = new PersonBuilder()
                .withPhone("98765432")
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_phoneMissing_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(ContactFieldPredicate.Field.PHONE, "91234567");

        Person person = new PersonBuilder().build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_emailMatches_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.EMAIL,
                        "alice@example.com");

        Person person = new PersonBuilder()
                .withEmail("alice@example.com")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_emailMatchingIsCaseInsensitive_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.EMAIL,
                        "alice@example.com");

        Person person = new PersonBuilder()
                .withEmail("Alice@Example.com")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_emailDoesNotMatch_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.EMAIL,
                        "alice@example.com");

        Person person = new PersonBuilder()
                .withEmail("bob@example.com")
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_emailMissing_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.EMAIL,
                        "alice@example.com");

        Person person = new PersonBuilder().build();

        assertFalse(predicate.test(person));
    }
}
