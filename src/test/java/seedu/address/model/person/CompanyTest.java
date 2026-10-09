package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CompanyTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Company(null));
    }

    @Test
    public void constructor_invalidCompany_throwsIllegalArgumentException() {
        String invalidCompany = "";
        assertThrows(IllegalArgumentException.class, () -> new Company(invalidCompany));
    }

    @Test
    public void isValidCompany() {
        // null company -> throws
        assertThrows(NullPointerException.class, () -> Company.isValidCompany(null));

        // invalid companys
        assertFalse(Company.isValidCompany("")); // empty string
        assertFalse(Company.isValidCompany(" ")); // spaces only

        // valid companys
        assertTrue(Company.isValidCompany("Acme Labs"));
        assertTrue(Company.isValidCompany("-")); // one character
        assertTrue(Company.isValidCompany("Senior Engineer, Platform & Infrastructure")); // long value
    }

    @Test
    public void equals() {
        Company company = new Company("Valid Company");

        // same values -> returns true
        assertTrue(company.equals(new Company("Valid Company")));

        // same object -> returns true
        assertTrue(company.equals(company));

        // null -> returns false
        assertFalse(company.equals(null));

        // different types -> returns false
        assertFalse(company.equals(5.0f));

        // different values -> returns false
        assertFalse(company.equals(new Company("Other Company")));
    }

    @Test
    public void hashCode_equalValues_areEqual() {
        assertEquals(new Company("Valid Company").hashCode(), new Company("Valid Company").hashCode());

        // unequal values should not be required to differ, but these ones do
        assertNotEquals(new Company("Valid Company").hashCode(), new Company("Other Company").hashCode());
    }
}
