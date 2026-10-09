package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LinkedinTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Linkedin(null));
    }

    @Test
    public void constructor_invalidLinkedin_throwsIllegalArgumentException() {
        String invalidLinkedin = "";
        assertThrows(IllegalArgumentException.class, () -> new Linkedin(invalidLinkedin));
    }

    @Test
    public void isValidLinkedin() {
        // null linkedin -> throws
        assertThrows(NullPointerException.class, () -> Linkedin.isValidLinkedin(null));

        // invalid linkedins
        assertFalse(Linkedin.isValidLinkedin("")); // empty string
        assertFalse(Linkedin.isValidLinkedin(" ")); // spaces only
        assertFalse(Linkedin.isValidLinkedin("linkedin.com/in/john doe")); // contains a space

        // valid linkedins
        assertTrue(Linkedin.isValidLinkedin("linkedin.com/in/johndoe")); // bare profile path
        assertTrue(Linkedin.isValidLinkedin("https://www.linkedin.com/in/johndoe")); // full URL
        assertTrue(Linkedin.isValidLinkedin("johndoe")); // bare handle
    }

    @Test
    public void equals() {
        Linkedin linkedin = new Linkedin("linkedin.com/in/johndoe");

        // same values -> returns true
        assertTrue(linkedin.equals(new Linkedin("linkedin.com/in/johndoe")));

        // same object -> returns true
        assertTrue(linkedin.equals(linkedin));

        // null -> returns false
        assertFalse(linkedin.equals(null));

        // different types -> returns false
        assertFalse(linkedin.equals(5.0f));

        // different values -> returns false
        assertFalse(linkedin.equals(new Linkedin("linkedin.com/in/janedoe")));
    }
}
