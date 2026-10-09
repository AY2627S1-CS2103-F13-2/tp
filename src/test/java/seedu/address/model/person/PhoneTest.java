package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // fewer than 3 digits
        assertFalse(Phone.isValidPhone("+ ()")); // separators but no digits at all
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("(6591234567")); // unclosed parenthesis
        assertFalse(Phone.isValidPhone("6591234567)")); // unopened parenthesis
        assertFalse(Phone.isValidPhone(")65(91234567")); // parentheses in the wrong order

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 digits
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces as grouping
        assertTrue(Phone.isValidPhone("9312-1534")); // hyphens as grouping
        assertTrue(Phone.isValidPhone("+65 9312 1534")); // country code
        assertTrue(Phone.isValidPhone("(65) 9312 1534")); // balanced parentheses
        assertTrue(Phone.isValidPhone("((65)) 9312 1534")); // nested balanced parentheses
    }

    @Test
    public void constructor_valueWithUntidySpacing_isNormalised() {
        assertEquals("9312 1534", new Phone("  9312   1534  ").value);
    }

    @Test
    public void equals_sameNumberSpacedDifferently_returnsTrue() {
        assertTrue(new Phone("9312  1534").equals(new Phone(" 9312 1534 ")));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}
