package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_UNRECOGNIZED_PREFIX = "Unknown prefix \"%1$s\". Check it for typos.";

    /*
     * A word ending in a slash, such as "ph/", inside a value that can never contain a slash is a
     * mistyped prefix that the tokenizer could not recognize and so left inside the previous value.
     */
    private static final Pattern PREFIX_LIKE_WORD = Pattern.compile("(?:^|\\s)(\\p{Alpha}+/)");

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Returns true if {@code input} is the user identifying a contact by index rather than by name,
     * i.e. its first word is a number. Commands that accept either form share this test so that the
     * same input is never read as an index by one command and as a name by another.
     */
    public static boolean isIndexInput(String input) {
        requireNonNull(input);
        String firstWord = input.trim().split("\\s+")[0];
        return firstWord.matches("[+-]?\\d+");
    }

    /**
     * Returns a message naming the first prefix-like word in the invalid {@code value}, or
     * {@code constraintsMessage} if there is none. Only use this for fields whose values cannot
     * contain a slash, so that a prefix-like word can only be a mistyped prefix.
     */
    private static String getUnrecognizedPrefixMessage(String value, String constraintsMessage) {
        Matcher matcher = PREFIX_LIKE_WORD.matcher(value);
        return matcher.find()
                ? String.format(MESSAGE_UNRECOGNIZED_PREFIX, matcher.group(1))
                : constraintsMessage;
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(getUnrecognizedPrefixMessage(trimmedName, Name.MESSAGE_CONSTRAINTS));
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(getUnrecognizedPrefixMessage(trimmedPhone, Phone.MESSAGE_CONSTRAINTS));
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String company} into a {@code Company}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code company} is invalid.
     */
    public static Company parseCompany(String company) throws ParseException {
        requireNonNull(company);
        String trimmedCompany = company.trim();
        if (!Company.isValidCompany(trimmedCompany)) {
            throw new ParseException(Company.MESSAGE_CONSTRAINTS);
        }
        return new Company(trimmedCompany);
    }

    /**
     * Parses a {@code String role} into a {@code Role}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code role} is invalid.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        String trimmedRole = role.trim();
        if (!Role.isValidRole(trimmedRole)) {
            throw new ParseException(Role.MESSAGE_CONSTRAINTS);
        }
        return new Role(trimmedRole);
    }

    /**
     * Parses a {@code String linkedin} into a {@code Linkedin}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code linkedin} is invalid.
     */
    public static Linkedin parseLinkedin(String linkedin) throws ParseException {
        requireNonNull(linkedin);
        String trimmedLinkedin = linkedin.trim();
        if (!Linkedin.isValidLinkedin(trimmedLinkedin)) {
            throw new ParseException(Linkedin.MESSAGE_CONSTRAINTS);
        }
        return new Linkedin(trimmedLinkedin);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(getUnrecognizedPrefixMessage(trimmedEmail, Email.MESSAGE_CONSTRAINTS));
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(getUnrecognizedPrefixMessage(trimmedTag, Tag.MESSAGE_CONSTRAINTS));
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
