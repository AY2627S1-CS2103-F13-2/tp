package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AliasCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class ContactFileParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT =
            Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(ContactFileParser.class);

    /**
     * Command words handled by {@link #parseCommand(String)}. Shortcuts may not use these names.
     */
    private static final List<String> BUILT_IN_COMMAND_WORDS = List.of(AddCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD, DeleteCommand.COMMAND_WORD, ViewCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
            FindCommand.COMMAND_WORD, ListCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
            AliasCommand.COMMAND_WORD);

    /**
     * Returns true if {@code word} is one of Astra's own command words.
     */
    public static boolean isBuiltInCommandWord(String word) {
        return BUILT_IN_COMMAND_WORDS.contains(word);
    }

    /**
     * Returns the command words a shortcut may stand for.
     */
    public static List<String> targetCommandWords() {
        return BUILT_IN_COMMAND_WORDS.stream().filter(word -> !word.equals(AliasCommand.COMMAND_WORD)).toList();
    }

    /**
     * Replaces a leading shortcut in {@code userInput} with the command it stands for, keeping the rest of the
     * input as typed. Input that starts with a built-in command word or with no known shortcut is returned as is.
     */
    public static String expandShortcut(String userInput, Map<String, String> aliases) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches() || isBuiltInCommandWord(matcher.group("commandWord"))) {
            return userInput;
        }

        String expansion = aliases.get(matcher.group("commandWord").toLowerCase(Locale.ROOT));
        if (expansion == null) {
            return userInput;
        }
        String expanded = expansion + matcher.group("arguments");
        logger.fine("Expanded shortcut " + matcher.group("commandWord") + " to: " + expanded);
        return expanded;
    }

    /**
     * Expands a leading shortcut in {@code userInput} using {@code aliases}, then parses the result.
     *
     * @see #expandShortcut(String, Map)
     * @see #parseCommand(String)
     */
    public Command parseCommand(String userInput, Map<String, String> aliases) throws ParseException {
        return parseCommand(expandShortcut(userInput, aliases));
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            case AliasCommand.COMMAND_WORD -> new AliasCommandParser().parse(arguments);
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }
}
