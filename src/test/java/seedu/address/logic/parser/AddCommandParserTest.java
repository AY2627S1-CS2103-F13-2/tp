package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.COMPANY_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.COMPANY_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_COMPANY_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_LINKEDIN_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.LINKEDIN_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LINKEDIN_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_COMPANY_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINKEDIN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {

    private static final String ALL_FIELDS_BOB =
            NAME_DESC_BOB
                    + COMPANY_DESC_BOB
                    + ROLE_DESC_BOB
                    + PHONE_DESC_BOB
                    + EMAIL_DESC_BOB
                    + LINKEDIN_DESC_BOB;

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson =
                new PersonBuilder(BOB)
                        .withTags(VALID_TAG_FRIEND)
                        .build();

        assertParseSuccess(
                parser,
                PREAMBLE_WHITESPACE
                        + ALL_FIELDS_BOB
                        + TAG_DESC_FRIEND,
                new AddCommand(expectedPerson));

        Person expectedPersonMultipleTags =
                new PersonBuilder(BOB)
                        .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                        .build();

        assertParseSuccess(
                parser,
                ALL_FIELDS_BOB
                        + TAG_DESC_HUSBAND
                        + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_introducerPresent_success() {
        Person expectedPerson =
                new PersonBuilder(BOB)
                        .withIntroducer("O'Brien-Smith")
                        .withTags()
                        .build();

        assertParseSuccess(
                parser,
                ALL_FIELDS_BOB + " i/O'Brien-Smith",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_eventPresent_success() {
        Event event = new Event("NUS Career Fair");

        Person expectedPerson =
                new PersonBuilder()
                        .withName(VALID_NAME_BOB)
                        .withCompany(null)
                        .withRole(null)
                        .withPhone(null)
                        .withEmail(null)
                        .withLinkedin(null)
                        .withTags()
                        .build()
                        .withEvent(event);

        assertParseSuccess(
                parser,
                NAME_DESC_BOB + " ev/NUS Career Fair",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_blankEvent_failure() {
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        AddCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                NAME_DESC_BOB + " ev/",
                expectedMessage);
    }

    @Test
    public void parse_mistypedPrefix_reportsUnknownPrefix() {
        // the mistyped ph/ makes the name invalid, but the message names ph/ as the cause
        assertParseFailure(parser, " n/John ph/123",
                String.format(AddCommandParser.MESSAGE_UNKNOWN_PREFIX, "ph/", AddCommand.MESSAGE_USAGE));

        // a mistyped name prefix is reported instead of the missing name
        assertParseFailure(parser, " nm/John" + PHONE_DESC_BOB,
                String.format(AddCommandParser.MESSAGE_UNKNOWN_PREFIX, "nm/", AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_slashInsideValue_notReportedAsUnknownPrefix() {
        // a slash inside a LinkedIn URL is not preceded by a space, so the input still parses
        Person expectedPerson = new PersonBuilder().withName(VALID_NAME_BOB).withCompany(null).withRole(null)
                .withPhone(null).withEmail(null).withLinkedin("linkedin.com/in/bob").withTags().build();
        assertParseSuccess(parser, NAME_DESC_BOB + " " + PREFIX_LINKEDIN + "linkedin.com/in/bob",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedEvent_failure() {
        String expectedMessage =
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_EVENT_NAME);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + " ev/NUS Career Fair"
                        + " ev/Tech Conference",
                expectedMessage);
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString =
                ALL_FIELDS_BOB + TAG_DESC_FRIEND;

        assertParseFailure(
                parser,
                NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        assertParseFailure(
                parser,
                COMPANY_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_COMPANY));

        assertParseFailure(
                parser,
                ROLE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        assertParseFailure(
                parser,
                PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        assertParseFailure(
                parser,
                EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        assertParseFailure(
                parser,
                LINKEDIN_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LINKEDIN));

        assertParseFailure(
                parser,
                " i/Jane Doe"
                        + validExpectedPersonString
                        + " i/John Smith",
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_INTRODUCER));

        assertParseFailure(
                parser,
                validExpectedPersonString
                        + NAME_DESC_AMY
                        + COMPANY_DESC_AMY
                        + ROLE_DESC_AMY
                        + PHONE_DESC_AMY
                        + EMAIL_DESC_AMY
                        + LINKEDIN_DESC_AMY
                        + validExpectedPersonString
                        + " i/Jane Doe i/John Doe",
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_NAME,
                        PREFIX_COMPANY,
                        PREFIX_ROLE,
                        PREFIX_PHONE,
                        PREFIX_EMAIL,
                        PREFIX_LINKEDIN,
                        PREFIX_INTRODUCER));

        assertParseFailure(
                parser,
                INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        assertParseFailure(
                parser,
                INVALID_COMPANY_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_COMPANY));

        assertParseFailure(
                parser,
                INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        assertParseFailure(
                parser,
                INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        assertParseFailure(
                parser,
                validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        assertParseFailure(
                parser,
                validExpectedPersonString + INVALID_ROLE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        assertParseFailure(
                parser,
                validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        assertParseFailure(
                parser,
                validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        assertParseFailure(
                parser,
                validExpectedPersonString + INVALID_LINKEDIN_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LINKEDIN));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        Person expectedPerson =
                new PersonBuilder(AMY)
                        .withTags()
                        .build();

        assertParseSuccess(
                parser,
                NAME_DESC_AMY
                        + COMPANY_DESC_AMY
                        + ROLE_DESC_AMY
                        + PHONE_DESC_AMY
                        + EMAIL_DESC_AMY
                        + LINKEDIN_DESC_AMY,
                new AddCommand(expectedPerson));

        Person nameOnlyPerson =
                new PersonBuilder()
                        .withName(VALID_NAME_BOB)
                        .withCompany(null)
                        .withRole(null)
                        .withPhone(null)
                        .withEmail(null)
                        .withLinkedin(null)
                        .withTags()
                        .build();

        assertParseSuccess(
                parser,
                NAME_DESC_BOB,
                new AddCommand(nameOnlyPerson));

        Person partialPerson =
                new PersonBuilder()
                        .withName(VALID_NAME_BOB)
                        .withCompany(VALID_COMPANY_BOB)
                        .withEmail(VALID_EMAIL_BOB)
                        .withRole(null)
                        .withPhone(null)
                        .withLinkedin(null)
                        .withTags()
                        .build();

        assertParseSuccess(
                parser,
                NAME_DESC_BOB
                        + COMPANY_DESC_BOB
                        + EMAIL_DESC_BOB,
                new AddCommand(partialPerson));
    }

    @Test
    public void parse_invalidIntroducer_failure() {
        assertParseFailure(
                parser,
                NAME_DESC_BOB + " i/Jane& Smith",
                Name.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB + " i/",
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        AddCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                VALID_NAME_BOB
                        + COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB,
                expectedMessage);

        assertParseFailure(
                parser,
                COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB,
                expectedMessage);

        assertParseFailure(
                parser,
                VALID_NAME_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(
                parser,
                INVALID_NAME_DESC
                        + COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB
                        + TAG_DESC_HUSBAND
                        + TAG_DESC_FRIEND,
                Name.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + INVALID_COMPANY_DESC
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB,
                Company.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + COMPANY_DESC_BOB
                        + INVALID_ROLE_DESC
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB,
                Role.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + INVALID_PHONE_DESC
                        + EMAIL_DESC_BOB
                        + LINKEDIN_DESC_BOB,
                Phone.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + INVALID_EMAIL_DESC
                        + LINKEDIN_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                NAME_DESC_BOB
                        + COMPANY_DESC_BOB
                        + ROLE_DESC_BOB
                        + PHONE_DESC_BOB
                        + INVALID_LINKEDIN_DESC,
                Linkedin.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                ALL_FIELDS_BOB
                        + INVALID_TAG_DESC
                        + VALID_TAG_FRIEND,
                Tag.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                INVALID_NAME_DESC
                        + PHONE_DESC_BOB
                        + EMAIL_DESC_BOB
                        + INVALID_COMPANY_DESC,
                Name.MESSAGE_CONSTRAINTS);

        assertParseFailure(
                parser,
                PREAMBLE_NON_EMPTY
                        + ALL_FIELDS_BOB
                        + TAG_DESC_HUSBAND
                        + TAG_DESC_FRIEND,
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        AddCommand.MESSAGE_USAGE));
    }
}
