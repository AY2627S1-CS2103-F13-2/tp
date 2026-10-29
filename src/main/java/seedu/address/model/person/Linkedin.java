package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's LinkedIn profile in the contact file.
 * Guarantees: immutable; is valid as declared in {@link #isValidLinkedin(String)}
 */
public class Linkedin {

    public static final String MESSAGE_CONSTRAINTS =
            "LinkedIn profiles should be a linkedin.com URL (e.g. linkedin.com/in/johndoe) or a handle of "
            + "3 to 100 letters, digits and hyphens (e.g. john-doe), without spaces.";

    /*
     * A LinkedIn profile is accepted as either a linkedin.com URL, with or without the scheme and a
     * subdomain such as www, or a bare handle, which LinkedIn limits to letters, digits and hyphens.
     */
    public static final String VALIDATION_REGEX =
            "(?i)(https?://)?([a-z0-9-]+\\.)?linkedin\\.com/\\S+|[a-z0-9-]{3,100}";

    public final String value;

    /**
     * Constructs a {@code Linkedin}.
     *
     * @param linkedin A valid LinkedIn profile.
     */
    public Linkedin(String linkedin) {
        requireNonNull(linkedin);
        checkArgument(isValidLinkedin(linkedin), MESSAGE_CONSTRAINTS);
        value = StringUtil.normalise(linkedin);
    }

    /**
     * Returns true if a given string is a valid LinkedIn profile.
     */
    public static boolean isValidLinkedin(String test) {
        return StringUtil.normalise(test).matches(VALIDATION_REGEX);
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
