package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DropCommand;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

public class DropCommandParserTest {
    private final DropCommandParser parser = new DropCommandParser();
    private final String invalidFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DropCommand.MESSAGE_USAGE);

    @Test
    public void parse_validIndexWithMultipleTags_returnsDropCommand() {
        assertParseSuccess(parser, "1 t/old t/stale",
                new DropCommand(Index.fromOneBased(1), List.of(
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("old")),
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("stale"))), false));
    }

    @Test
    public void parse_validNameWithEmail_returnsDropCommand() {
        assertParseSuccess(parser, "Jane Doe e/jane@example.com",
                new DropCommand("Jane Doe", List.of(
                        new DropCommand.FieldValue(DropCommand.Field.EMAIL, new Email("jane@example.com"))), false));
    }

    @Test
    public void parse_confirmedDrop_returnsConfirmedCommand() {
        assertParseSuccess(parser, "1 t/old --confirm",
                new DropCommand(Index.fromOneBased(1), List.of(
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("old"))), true));
    }

    @Test
    public void parse_validIntroducer_returnsDropCommand() {
        assertParseSuccess(parser, "Jane Doe i/Old Introducer",
                new DropCommand("Jane Doe", List.of(
                        new DropCommand.FieldValue(DropCommand.Field.INTRODUCER, new Name("Old Introducer"))),
                        false));
    }

    @Test
    public void parse_validAllFieldTypes_returnsDropCommand() {
        assertParseSuccess(parser, "Jane Doe c/Acme r/Engineer p/91234567 e/jane@example.com "
                        + "l/linkedin.com/in/jane t/old i/Old Introducer",
                new DropCommand("Jane Doe", List.of(
                        new DropCommand.FieldValue(DropCommand.Field.COMPANY, new Company("Acme")),
                        new DropCommand.FieldValue(DropCommand.Field.ROLE, new Role("Engineer")),
                        new DropCommand.FieldValue(DropCommand.Field.PHONE, new Phone("91234567")),
                        new DropCommand.FieldValue(DropCommand.Field.EMAIL, new Email("jane@example.com")),
                        new DropCommand.FieldValue(DropCommand.Field.LINKEDIN,
                                new Linkedin("linkedin.com/in/jane")),
                        new DropCommand.FieldValue(DropCommand.Field.TAG, new Tag("old")),
                        new DropCommand.FieldValue(DropCommand.Field.INTRODUCER, new Name("Old Introducer"))),
                        false));
    }

    @Test
    public void parse_validSingleValueSelectors_returnsDropCommand() {
        assertParseSuccess(parser, "1 c/Acme", new DropCommand(Index.fromOneBased(1),
                List.of(new DropCommand.FieldValue(DropCommand.Field.COMPANY, new Company("Acme"))), false));
        assertParseSuccess(parser, "1 r/Engineer", new DropCommand(Index.fromOneBased(1),
                List.of(new DropCommand.FieldValue(DropCommand.Field.ROLE, new Role("Engineer"))), false));
        assertParseSuccess(parser, "1 p/91234567", new DropCommand(Index.fromOneBased(1),
                List.of(new DropCommand.FieldValue(DropCommand.Field.PHONE, new Phone("91234567"))), false));
        assertParseSuccess(parser, "1 l/jane", new DropCommand(Index.fromOneBased(1),
                List.of(new DropCommand.FieldValue(DropCommand.Field.LINKEDIN, new Linkedin("jane"))), false));
    }

    @Test
    public void parse_emptyValues_fails() {
        assertParseFailure(parser, "", invalidFormat);
        assertParseFailure(parser, "1", invalidFormat);
    }

    @Test
    public void parse_invalidOrMissingSelection_fails() {
        assertParseFailure(parser, "   ", invalidFormat);
        assertParseFailure(parser, "1 malformed", invalidFormat);
        assertParseFailure(parser, "Jane& Doe t/old", invalidFormat);
        assertParseFailure(parser, "t/old", invalidFormat);
        assertParseFailure(parser, "1 x/unknown", invalidFormat);
        assertParseFailure(parser, "1 n/Jane Doe",
                "The contact name cannot be removed.");
        assertParseFailure(parser, "1 t/", invalidFormat);
        assertParseFailure(parser, "1 c/", invalidFormat);
        assertParseFailure(parser, "1 e/not-an-email", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 c/ ", invalidFormat);
        assertParseFailure(parser, "1 r/ ", invalidFormat);
        assertParseFailure(parser, "1 p/12", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 l/ ", invalidFormat);
        assertParseFailure(parser, "1 i/Jane& Doe", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 p/123 t/old p/456",
                "Multiple values specified for the following single-valued field(s): p/");
        assertParseFailure(parser, "1 c/Acme c/Other",
                "Multiple values specified for the following single-valued field(s): c/");
        assertParseFailure(parser, "1 i/Jane Doe i/John Smith",
                "Multiple values specified for the following single-valued field(s): i/");
        assertParseFailure(parser, "0 t/old", invalidFormat);
        assertParseFailure(parser, "-1 t/old", invalidFormat);
    }
}
