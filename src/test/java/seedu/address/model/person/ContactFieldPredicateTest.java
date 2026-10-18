package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ContactFieldPredicateTest {

    @Test
    public void test_phoneMatches_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

        Person person = new PersonBuilder()
                .withPhone("91234567")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_phoneDoesNotMatch_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

        Person person = new PersonBuilder()
                .withPhone("98765432")
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_phoneMissing_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

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

    @Test
    public void test_companyPartialMatch_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.COMPANY,
                        "goo");

        Person person = new PersonBuilder()
                .withCompany("Google")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_companyMatchingIsCaseInsensitive_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.COMPANY,
                        "GOOGLE");

        Person person = new PersonBuilder()
                .withCompany("Google")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_companyDoesNotMatch_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.COMPANY,
                        "Apple");

        Person person = new PersonBuilder()
                .withCompany("Google")
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_companyMissing_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.COMPANY,
                        "Google");

        Person person = new PersonBuilder()
                .withCompany(null)
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_rolePartialMatch_returnsTrue() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.ROLE,
                        "engineer");

        Person person = new PersonBuilder()
                .withRole("Software Engineer")
                .build();

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_roleDoesNotMatch_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.ROLE,
                        "Manager");

        Person person = new PersonBuilder()
                .withRole("Software Engineer")
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_roleMissing_returnsFalse() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.ROLE,
                        "Engineer");

        Person person = new PersonBuilder()
                .withRole(null)
                .build();

        assertFalse(predicate.test(person));
    }

    @Test
    public void equals() {
        ContactFieldPredicate firstPredicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

        ContactFieldPredicate samePredicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

        ContactFieldPredicate differentFieldPredicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.EMAIL,
                        "91234567");

        ContactFieldPredicate differentQueryPredicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "98765432");

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(samePredicate));
        assertFalse(firstPredicate.equals(differentFieldPredicate));
        assertFalse(firstPredicate.equals(differentQueryPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }

    @Test
    public void toStringMethod() {
        ContactFieldPredicate predicate =
                new ContactFieldPredicate(
                        ContactFieldPredicate.Field.PHONE,
                        "91234567");

        String expected = ContactFieldPredicate.class.getCanonicalName()
                + "{field=PHONE, query=91234567}";

        assertEquals(expected, predicate.toString());
    }
}
