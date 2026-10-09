package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final int MINIMUM_DIGITS = 3;

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should contain at least " + MINIMUM_DIGITS + " digits, and may also use spaces, "
            + "hyphens, a leading plus sign and balanced parentheses.";

    /*
     * A number exchanged at a networking event is written however the person says it, so country
     * codes and grouping characters are accepted alongside the digits. Anything else, such as a
     * letter, is rejected.
     */
    public static final String VALIDATION_REGEX = "[\\p{N}() +-]+";

    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = StringUtil.normalise(phone);
    }

    /**
     * Returns true if a given string is a valid phone number, once normalised.
     */
    public static boolean isValidPhone(String test) {
        String normalised = StringUtil.normalise(test);
        return normalised.matches(VALIDATION_REGEX)
                && countDigits(normalised) >= MINIMUM_DIGITS
                && hasBalancedParentheses(normalised);
    }

    /**
     * Returns the number of digits in {@code test}.
     */
    private static int countDigits(String test) {
        return (int) test.chars().filter(Character::isDigit).count();
    }

    /**
     * Returns true if every opening parenthesis in {@code test} is matched by a later closing one.
     */
    private static boolean hasBalancedParentheses(String test) {
        int depth = 0;
        for (int i = 0; i < test.length(); i++) {
            char current = test.charAt(i);
            if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0;
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
