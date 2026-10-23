package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Collections;
import java.util.LinkedHashMap;
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
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.commands.PendingFollowUpsCommand;
import seedu.address.logic.commands.StarCommand;
import seedu.address.logic.commands.StarredCommand;
import seedu.address.logic.commands.UnstarCommand;
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
     * Creates the command for one command word from the arguments typed after it.
     */
    @FunctionalInterface
    private interface CommandFactory {
        Command create(String arguments) throws ParseException;
    }

    /**
     * Every built-in command word and how to create its command. This is the only list of command words:
     * {@link #parseCommand(String)} and the shortcut checks both read it, so a new command added here can
     * be run, cannot be hidden by a shortcut, and can be the target of one.
     */
    private static final Map<String, CommandFactory> COMMANDS = createCommands();

    private static Map<String, CommandFactory> createCommands() {
        Map<String, CommandFactory> commands = new LinkedHashMap<>();
        commands.put(AddCommand.COMMAND_WORD, arguments -> new AddCommandParser().parse(arguments));
        commands.put(EditCommand.COMMAND_WORD, arguments -> new EditCommandParser().parse(arguments));
        commands.put(DeleteCommand.COMMAND_WORD, arguments -> new DeleteCommandParser().parse(arguments));
        commands.put(ViewCommand.COMMAND_WORD, arguments -> new ViewCommandParser().parse(arguments));
        commands.put(StarCommand.COMMAND_WORD, arguments -> new StarCommandParser().parse(arguments));
        commands.put(UnstarCommand.COMMAND_WORD, arguments -> new UnstarCommandParser().parse(arguments));
        commands.put(StarredCommand.COMMAND_WORD, arguments -> new StarredCommand());
        commands.put(ClearCommand.COMMAND_WORD, arguments -> new ClearCommandParser().parse(arguments));
        commands.put(FilterCommand.COMMAND_WORD, arguments -> new FilterCommandParser().parse(arguments));
        commands.put(FindCommand.COMMAND_WORD, arguments -> new FindCommandParser().parse(arguments));
        commands.put(FollowUpCommand.COMMAND_WORD, arguments -> new FollowUpCommandParser().parse(arguments));
        commands.put(PendingFollowUpsCommand.COMMAND_WORD, arguments -> {
            if (!arguments.isBlank()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        PendingFollowUpsCommand.MESSAGE_USAGE));
            }
            return new PendingFollowUpsCommand();
        });
        commands.put(OrderCommand.COMMAND_WORD, arguments -> new OrderCommandParser().parse(arguments));
        commands.put(ListCommand.COMMAND_WORD, arguments -> new ListCommand());
        commands.put(ExitCommand.COMMAND_WORD, arguments -> new ExitCommand());
        commands.put(HelpCommand.COMMAND_WORD, arguments -> new HelpCommand());
        commands.put(AliasCommand.COMMAND_WORD, arguments -> new AliasCommandParser().parse(arguments));
        return Collections.unmodifiableMap(commands);
    }

    /**
     * Returns true if {@code word} is one of Astra's own command words.
     */
    public static boolean isBuiltInCommandWord(String word) {
        return COMMANDS.containsKey(word);
    }

    /**
     * Returns the command words a shortcut may stand for.
     */
    public static List<String> targetCommandWords() {
        return COMMANDS.keySet().stream().filter(word -> !word.equals(AliasCommand.COMMAND_WORD)).toList();
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

        CommandFactory factory = COMMANDS.get(commandWord);
        if (factory == null) {
            logger.finer("This user input caused a ParseException: " + userInput);
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }
        return factory.create(arguments);
    }
}
