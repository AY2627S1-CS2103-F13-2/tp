package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("-peter")); // starts with punctuation
        assertFalse(Name.isValidName("'")); // punctuation only

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Jean-Luc")); // hyphenated
        assertTrue(Name.isValidName("Siti O'Brien")); // apostrophe
        assertTrue(Name.isValidName("A. Kumar")); // initial followed by a full stop

        // valid names using letters outside English (NFR 9)
        assertTrue(Name.isValidName("Jose Garcia"));
        assertTrue(Name.isValidName("Zoe Muller"));
        assertTrue(Name.isValidName("Bjorn Borg"));
    }

    @Test
    public void constructor_valueWithUntidySpacing_isNormalised() {
        assertEquals("John Doe", new Name("  John   Doe  ").fullName);
    }

    @Test
    public void equals_sameNameSpacedDifferently_returnsTrue() {
        assertTrue(new Name("John  Doe").equals(new Name(" John Doe ")));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void hashCode_equalValues_areEqual() {
        assertEquals(new Name("Valid Name").hashCode(), new Name("Valid Name").hashCode());

        // unequal values should not be required to differ, but these ones do
        assertNotEquals(new Name("Valid Name").hashCode(), new Name("Other Valid Name").hashCode());
    }
}
