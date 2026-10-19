package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.AliasCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new AliasCommand object.
 */
public class AliasCommandParser implements Parser<AliasCommand> {

    public static final String REPLACE_FLAG = "/replace";
    public static final String REMOVE_FLAG = "/remove";

    public static final String MESSAGE_INVALID_SHORTCUT = "Shortcut names should be one word of letters and digits "
            + "that starts with a letter, e.g. fn. \"%1$s\" is not.";
    public static final String MESSAGE_BUILT_IN = "%1$s is already one of Astra's commands, so it cannot be "
            + "used as a shortcut.";
    public static final String MESSAGE_UNKNOWN_TARGET = "A shortcut must stand for one of Astra's commands (%1$s), "
            + "but %2$s is not one of them.";

    private static final String SHORTCUT_REGEX = "[A-Za-z][A-Za-z0-9]*";

    /**
     * Parses the given {@code String} of arguments in the context of the AliasCommand
     * and returns an AliasCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AliasCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return AliasCommand.list();
        }

        String[] firstAndRest = splitFirstWord(trimmedArgs);
        if (firstAndRest[0].equals(REMOVE_FLAG)) {
            String[] shortcutAndRest = splitFirstWord(firstAndRest[1]);
            if (shortcutAndRest[0].isEmpty() || !shortcutAndRest[1].isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AliasCommand.MESSAGE_USAGE));
            }
            return AliasCommand.remove(parseShortcut(shortcutAndRest[0]));
        }

        boolean isReplace = firstAndRest[0].equals(REPLACE_FLAG);
        String[] shortcutAndExpansion = isReplace ? splitFirstWord(firstAndRest[1]) : firstAndRest;
        if (shortcutAndExpansion[0].isEmpty() || shortcutAndExpansion[1].isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AliasCommand.MESSAGE_USAGE));
        }

        String shortcut = parseShortcut(shortcutAndExpansion[0]);
        String expansion = parseExpansion(shortcutAndExpansion[1]);
        return isReplace ? AliasCommand.replace(shortcut, expansion) : AliasCommand.add(shortcut, expansion);
    }

    /**
     * Returns the shortcut name in lower case, so that shortcuts can be entered in any letter case.
     */
    private static String parseShortcut(String shortcut) throws ParseException {
        if (!shortcut.matches(SHORTCUT_REGEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_SHORTCUT, shortcut));
        }
        String lowerCaseShortcut = shortcut.toLowerCase(Locale.ROOT);
        if (ContactFileParser.isBuiltInCommandWord(lowerCaseShortcut)) {
            throw new ParseException(String.format(MESSAGE_BUILT_IN, lowerCaseShortcut));
        }
        return lowerCaseShortcut;
    }

    /**
     * Checks that the expansion starts with a built-in command. Shortcuts cannot stand for other shortcuts
     * or for {@code alias} itself, so expanding a shortcut always takes exactly one step.
     */
    private static String parseExpansion(String expansion) throws ParseException {
        String commandWord = splitFirstWord(expansion)[0];
        if (!ContactFileParser.isBuiltInCommandWord(commandWord) || commandWord.equals(AliasCommand.COMMAND_WORD)) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_TARGET,
                    String.join(", ", ContactFileParser.targetCommandWords()), commandWord));
        }
        return expansion;
    }

    /**
     * Splits trimmed {@code text} into its first word and the trimmed remainder (empty if there is none).
     */
    private static String[] splitFirstWord(String text) {
        String[] parts = text.trim().split("\\s+", 2);
        return new String[] {parts[0], parts.length > 1 ? parts[1].trim() : ""};
    }
}
