package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's LinkedIn profile in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidLinkedin(String)}
 */
public class Linkedin {

    public static final String MESSAGE_CONSTRAINTS =
            "LinkedIn profiles should be a URL or handle without spaces, and should not be blank.";

    /*
     * A LinkedIn profile is accepted as either a full URL or a bare handle, so the only
     * requirement is a non-blank value with no whitespace in it.
     */
    public static final String VALIDATION_REGEX = "\\S+";

    public final String value;

    /**
     * Constructs a {@code Linkedin}.
     *
     * @param linkedin A valid LinkedIn profile.
     */
    public Linkedin(String linkedin) {
        requireNonNull(linkedin);
        checkArgument(isValidLinkedin(linkedin), MESSAGE_CONSTRAINTS);
        value = linkedin;
    }

    /**
     * Returns true if a given string is a valid LinkedIn profile.
     */
    public static boolean isValidLinkedin(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Linkedin otherLinkedin)) {
            return false;
        }

        return value.equals(otherLinkedin.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
