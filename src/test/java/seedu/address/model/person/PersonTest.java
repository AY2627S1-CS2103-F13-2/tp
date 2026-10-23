package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_COMPANY_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_existingSignature_defaultsToUnstarred() {
        Person person = new Person(NAME_ONLY.getName(), null, null, null, null, null, NAME_ONLY.getTags());
        assertFalse(person.isStarred());
        assertEquals(NAME_ONLY, person);
    }

    @Test
    public void starredStatus_changesDataButNotIdentity() {
        Person starredAlice = new PersonBuilder(ALICE).withStarred(true).build();
        Person starredAliceCopy = new PersonBuilder(starredAlice).build();

        assertTrue(starredAlice.isStarred());
        assertFalse(ALICE.isStarred());
        assertTrue(ALICE.isSamePerson(starredAlice));
        assertTrue(starredAlice.isSamePerson(ALICE));
        assertNotEquals(ALICE, starredAlice);
        assertNotEquals(starredAlice, ALICE);
        assertEquals(starredAlice, starredAliceCopy);
        assertEquals(starredAlice.hashCode(), starredAliceCopy.hashCode());
        assertEquals(ALICE, new PersonBuilder(starredAlice).withStarred(false).build());
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and email presence -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withCompany(VALID_COMPANY_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns true,
        // because outer spaces are normalised away before the names are compared
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void hasSamePhone() {
        // same object -> returns true
        assertTrue(ALICE.hasSamePhone(ALICE));

        // null -> returns false
        assertFalse(ALICE.hasSamePhone(null));

        // different phone -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.hasSamePhone(editedAlice));

        // same phone, different other fields -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertTrue(ALICE.hasSamePhone(editedAlice));
    }

    @Test
    public void hasSameEmail() {
        // same object -> returns true
        assertTrue(ALICE.hasSameEmail(ALICE));

        // null -> returns false
        assertFalse(ALICE.hasSameEmail(null));

        // different email -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.hasSameEmail(editedAlice));

        // same email, different other fields -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertTrue(ALICE.hasSameEmail(editedAlice));

        // same email with different letter case -> returns true
        editedAlice = new PersonBuilder(ALICE).withEmail(ALICE.getEmail().orElseThrow().value.toUpperCase()).build();
        assertTrue(ALICE.hasSameEmail(editedAlice));

        // both emails absent -> returns false
        Person personWithoutEmail = new PersonBuilder().withName(VALID_NAME_BOB).withEmail(null).build();
        Person anotherPersonWithoutEmail = new PersonBuilder(ALICE).withEmail(null).build();
        assertFalse(personWithoutEmail.hasSameEmail(anotherPersonWithoutEmail));
        assertFalse(personWithoutEmail.hasSameEmail(personWithoutEmail));

        // one email absent -> returns false
        assertFalse(personWithoutEmail.hasSameEmail(ALICE));
        assertFalse(ALICE.hasSameEmail(personWithoutEmail));
    }

    @Test
    public void hasSamePhone_missingValues_returnsFalse() {
        Person first = new PersonBuilder(ALICE).withPhone(null).build();
        Person second = new PersonBuilder(BOB).withPhone(null).build();
        assertFalse(first.hasSamePhone(second));
        assertFalse(first.hasSamePhone(first));
        assertFalse(first.hasSamePhone(ALICE));
        assertFalse(ALICE.hasSamePhone(first));
    }

    @Test
    public void equals_groupedPhone_hashCodesMatch() {
        Person grouped = new PersonBuilder(ALICE).withPhone("+65 (9312)-1534").build();
        Person compact = new PersonBuilder(ALICE).withPhone("+6593121534").build();
        assertEquals(grouped, compact);
        assertEquals(grouped.hashCode(), compact.hashCode());
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withCompany(VALID_COMPANY_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different role -> returns false
        editedAlice = new PersonBuilder(ALICE).withRole(VALID_ROLE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different linkedin -> returns false
        editedAlice = new PersonBuilder(ALICE).withLinkedin(VALID_LINKEDIN_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // field cleared on one side only -> returns false
        editedAlice = new PersonBuilder(ALICE).withCompany(null).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void optionalFields_absent_returnEmpty() {
        assertEquals(Optional.empty(), NAME_ONLY.getCompany());
        assertEquals(Optional.empty(), NAME_ONLY.getRole());
        assertEquals(Optional.empty(), NAME_ONLY.getPhone());
        assertEquals(Optional.empty(), NAME_ONLY.getEmail());
        assertEquals(Optional.empty(), NAME_ONLY.getLinkedin());
        assertTrue(NAME_ONLY.getTags().isEmpty());
    }

    @Test
    public void isSamePerson_sameNameOnlyPersons_returnsTrue() {
        // Identical contacts match even when they only have a name.
        Person otherNameOnly = new PersonBuilder(NAME_ONLY).build();
        assertTrue(NAME_ONLY.isSamePerson(otherNameOnly));
    }

    @Test
    public void isSamePerson_otherPersonHasNoEmail_returnsFalse() {
        Person withEmail = new PersonBuilder(ALICE).build();
        Person withoutEmail = new PersonBuilder(ALICE).withEmail(null).build();
        assertFalse(withEmail.isSamePerson(withoutEmail));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName()
                + ", company=" + ALICE.getCompany().orElse(null)
                + ", role=" + ALICE.getRole().orElse(null)
                + ", phone=" + ALICE.getPhone().orElse(null)
                + ", email=" + ALICE.getEmail().orElse(null)
                + ", linkedin=" + ALICE.getLinkedin().orElse(null)
                + ", tags=" + ALICE.getTags() + ", followUps=" + ALICE.getFollowUps()
                + ", starred=" + ALICE.isStarred() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void hashCode_equalPersons_areEqual() {
        assertEquals(ALICE.hashCode(), new PersonBuilder(ALICE).build().hashCode());

        // a person differing only in an absent optional field hashes differently
        assertNotEquals(ALICE.hashCode(), new PersonBuilder(ALICE).withCompany(null).build().hashCode());
    }
}
