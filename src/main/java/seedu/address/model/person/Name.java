package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain letters, numbers, spaces, hyphens, apostrophes and full stops, "
            + "and should not be blank.";

    /*
     * \p{L} and \p{N} match letters and numbers in any language, not just English, so that names
     * such as "Nurul Aisyah" and "Jose Garcia" are accepted (NFR 9). Hyphens, apostrophes and full
     * stops are allowed inside a name for the likes of "Jean-Luc", "O'Brien" and "A. Kumar", but a
     * name must still start with a letter or a number.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{N} '.-]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = StringUtil.normalise(name);
    }

    /**
     * Returns true if a given string is a valid name, once normalised.
     */
    public static boolean isValidName(String test) {
        return StringUtil.normalise(test).matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
