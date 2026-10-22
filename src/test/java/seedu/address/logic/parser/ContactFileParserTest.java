package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AliasCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.StarCommand;
import seedu.address.logic.commands.StarredCommand;
import seedu.address.logic.commands.UnstarCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class ContactFileParserTest {

    private final ContactFileParser parser = new ContactFileParser();

    @Test
    public void parseCommand_starred() throws Exception {
        assertTrue(parser.parseCommand(StarredCommand.COMMAND_WORD) instanceof StarredCommand);
    }

    @Test
    public void parseCommand_starredWithWhitespace_returnsStarredCommand() throws Exception {
        assertTrue(parser.parseCommand("  starred  ") instanceof StarredCommand);
    }

    @Test
    public void parseCommand_starredWithArguments_ignoresArgumentsLikeList() throws Exception {
        assertTrue(parser.parseCommand(StarredCommand.COMMAND_WORD + " 3") instanceof StarredCommand);
    }

    @Test
    public void parseCommand_star() throws Exception {
        assertEquals(new StarCommand(INDEX_FIRST_PERSON), parser.parseCommand("star 1"));
    }

    @Test
    public void parseCommand_unstar() throws Exception {
        assertEquals(new UnstarCommand(INDEX_FIRST_PERSON), parser.parseCommand("unstar 1"));
    }

    @Test
    public void parseCommand_starInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, StarCommand.MESSAGE_USAGE);
        for (String input : List.of("star", "star 0", "star 1 2")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_unstarInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnstarCommand.MESSAGE_USAGE);
        for (String input : List.of("unstar", "unstar 0", "unstar 1 2")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_alias() throws Exception {
        assertEquals(AliasCommand.add("fn", "find /notes"),
                parser.parseCommand(AliasCommand.COMMAND_WORD + " fn find /notes"));
    }

    @Test
    public void parseCommand_shortcut_expandedBeforeParsing() throws Exception {
        Map<String, String> aliases = Map.of("fl", "find");
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("lim"))),
                parser.parseCommand("FL lim", aliases));
        // built-in commands and unknown words are parsed as usual
        assertTrue(parser.parseCommand("list", aliases) instanceof ListCommand);
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("fx lim", aliases));
    }

    @Test
    public void expandShortcut() {
        Map<String, String> aliases = Map.of("fn", "find /notes", "list", "clear");
        assertEquals("find /notes  internship", ContactFileParser.expandShortcut(" fn  internship  ", aliases));
        assertEquals("find /notes", ContactFileParser.expandShortcut("fn", aliases));
        // a built-in command word is never replaced, even if a shortcut with that name was saved
        assertEquals("list", ContactFileParser.expandShortcut("list", aliases));
        assertEquals("   ", ContactFileParser.expandShortcut("   ", aliases));
        assertEquals("fnx", ContactFileParser.expandShortcut("fnx", aliases));
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
    }

    @Test
    public void parseCommand_clearWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        String input = ClearCommand.COMMAND_WORD + " 3";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_deleteByName() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " Alice Pauline");
        assertEquals(new DeleteCommand("Alice Pauline"), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        String query = "foo bar baz";
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + query);
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of(query))), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(
                new ViewCommand(Index.fromOneBased(1)),
                parser.parseCommand(ViewCommand.COMMAND_WORD + " 1"));
        assertEquals(new ViewCommand("Alice Pauline"), parser.parseCommand("view Alice Pauline"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        Executable unrecognisedInput = () -> parser.parseCommand("");
        assertThrows(ParseException.class,
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE),
            unrecognisedInput);
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void isBuiltInCommandWord_everyParsedCommand_true() {
        // Every command the parser runs is a built-in word, so a shortcut can never hide it.
        for (String word : List.of("add", "edit", "delete", "view", "star", "unstar", "starred", "clear", "filter",
                "find", "followup", "order", "list", "exit", "help", "alias")) {
            assertTrue(ContactFileParser.isBuiltInCommandWord(word), word);
        }
        assertEquals("filter t/work", ContactFileParser.expandShortcut("filter t/work", Map.of("filter", "list")));
    }
}
