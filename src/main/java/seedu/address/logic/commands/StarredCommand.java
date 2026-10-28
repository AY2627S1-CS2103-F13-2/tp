package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Lists all starred contacts in the contact file.
 */
public class StarredCommand extends Command {

    public static final String COMMAND_WORD = "starred";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all starred contacts.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Listed %1$d starred %2$s.";
    public static final String MESSAGE_NO_STARRED_CONTACTS = "There are no starred contacts.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(Person::isStarred);

        int starredCount = model.getFilteredPersonList().size();
        if (starredCount == 0) {
            return new CommandResult(MESSAGE_NO_STARRED_CONTACTS);
        }

        String contactLabel = starredCount == 1 ? "contact" : "contacts";
        return new CommandResult(String.format(MESSAGE_SUCCESS, starredCount, contactLabel));
    }
}
