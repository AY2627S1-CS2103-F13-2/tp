package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.UndoManager;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.IntroduceCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class ContactFileParserTest {

    private final ContactFileParser parser = new ContactFileParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
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
    }

    @Test
    public void parseCommand_introduce() throws Exception {
        assertEquals(new IntroduceCommand("Jane Smith", new Name("Jane Doe"), false),
                parser.parseCommand(IntroduceCommand.COMMAND_WORD + " Jane Smith i/Jane Doe"));
    }

    @Test
    public void parseCommand_undoWithoutArgumentsAndWithConfirmOption() throws Exception {
        UndoManager undoManager = new UndoManager();
        ContactFileParser parserWithUndoManager = new ContactFileParser(undoManager);
        Person person = new PersonBuilder().build();
        String feedback = "Deleted contact.";
        undoManager.recordChange(new DeleteCommand(Index.fromOneBased(1)), List.of(person), List.of(), feedback);
        Model model = new ModelManager(new ContactFile(), new UserPrefs());

        UndoCommand previewCommand = (UndoCommand) parserWithUndoManager.parseCommand(UndoCommand.COMMAND_WORD);
        assertEquals(String.format(UndoManager.MESSAGE_CONFIRM_UNDO, "deletion", feedback),
                previewCommand.execute(model).getFeedbackToUser());

        UndoCommand confirmCommand = (UndoCommand) parserWithUndoManager.parseCommand(
                UndoCommand.COMMAND_WORD + " " + UndoCommand.CONFIRM_OPTION);
        assertEquals(String.format(UndoManager.MESSAGE_UNDO_SUCCESS, Messages.format(person)),
                confirmCommand.execute(model).getFeedbackToUser());
    }

    @Test
    public void parseCommand_undoWithInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE);
        Executable invalidUndoOption = () -> parser.parseCommand(UndoCommand.COMMAND_WORD + " invalid");
        Executable extraUndoArgument = () -> parser.parseCommand(
                UndoCommand.COMMAND_WORD + " " + UndoCommand.CONFIRM_OPTION + " extra");
        assertThrows(ParseException.class, expectedMessage, invalidUndoOption);
        assertThrows(ParseException.class, expectedMessage, extraUndoArgument);
    }

    @Test
    public void parseCommand_historyWithoutArguments() throws Exception {
        HistoryCommand command = (HistoryCommand) parser.parseCommand(HistoryCommand.COMMAND_WORD);
        Model model = new ModelManager(new ContactFile(), new UserPrefs());
        assertEquals(UndoManager.MESSAGE_HISTORY_EMPTY, command.execute(model).getFeedbackToUser());
    }

    @Test
    public void parseCommand_historyWithArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HistoryCommand.MESSAGE_USAGE);
        Executable historyWithArguments = () -> parser.parseCommand(HistoryCommand.COMMAND_WORD + " extra");
        assertThrows(ParseException.class, expectedMessage, historyWithArguments);
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
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("record 1 i/Jane Doe"));
    }
}
