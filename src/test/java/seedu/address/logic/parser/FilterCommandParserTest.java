package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TagFilterPredicate;
import seedu.address.model.person.TagFilterPredicate.Mode;
import seedu.address.model.tag.Tag;

class FilterCommandParserTest {
    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    void parse_defaultAndExplicitAnd_returnSameCommand() throws Exception {
        FilterCommand expected = command(Mode.AND);
        assertEquals(expected, parser.parse("t/work t/friend"));
        assertEquals(expected, parser.parse(" mode/and t/work t/friend "));
        assertEquals(expected, parser.parse("t/friend mode/AND t/work"));
        assertEquals(expected, parser.parse("t/work t/friend t/work"));
    }

    @Test
    void parse_orAndWhitespace_parsesCaseInsensitiveMode() throws Exception {
        assertEquals(command(Mode.OR), parser.parse("mode/Or t/work t/friend"));
        assertEquals(command(Mode.OR), parser.parse("\t t/work\tmode/oR\t t/friend\n"));
        assertEquals(command(Mode.AND), parser.parse("t/work\nmode/and\nt/friend"));
        assertEquals(command(Mode.OR),
                new ContactFileParser().parseCommand("filter mode/OR t/work t/friend"));
    }

    @Test
    void parse_tagCase_preservesTagIdentity() throws Exception {
        assertEquals(new FilterCommand(new TagFilterPredicate(Set.of(new Tag("Work")), Mode.OR)),
                parser.parse("mode/or t/Work"));
    }

    @Test
    void parse_missingTags_showsUsage() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);
        for (String input : List.of("", " ", "mode/or", "mode/and", "mode/")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertEquals(usage, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void parse_invalidOrRepeatedMode_rejectsWholeRequest() {
        for (String input : List.of("mode/ t/work", "mode/xor t/work", "mode/and or t/work",
                "t/work mode/", "mode/and mode/or t/work", "mode/or t/work mode/or")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertTrue(error.getMessage().contains("mode"), error.getMessage());
        }
    }

    @Test
    void parse_invalidTagOrFormat_rejectsEveryMalformedValue() {
        for (String input : List.of("work", "t/", "t/ ", "t/bad!", "t/work t/", "t/work t/bad-name",
                "t/work friend", "work t/work", "mode/OR t/work t/bad!", "t/work x/value",
                "x/value t/work", "MODE/or t/work", "t/work mode/or unused", "t/workt/friend")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    void parse_unrecognizedPrefix_reportsTheIncorrectPrefix() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse("t/careerfair p/123"));

        assertEquals(
                String.format(ParserUtil.MESSAGE_UNRECOGNIZED_PREFIX, "p/"),
                error.getMessage());
    }

    private static FilterCommand command(Mode mode) {
        return new FilterCommand(new TagFilterPredicate(Set.of(new Tag("work"), new Tag("friend")), mode));
    }
}
