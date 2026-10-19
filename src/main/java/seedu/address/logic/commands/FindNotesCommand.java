package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.NoteContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Finds contacts whose interaction notes contain any of the given keywords, ignoring case, and shows the
 * matching notes grouped under each contact.
 */
public class FindNotesCommand extends FindCommand {

    public static final String MESSAGE_NO_MATCHING_NOTES = "No matching notes were found.";
    public static final String MESSAGE_MATCHES = "%1$d contact(s) with matching notes:\n%2$s";

    /** Notes longer than this are shortened to an excerpt around the first match. */
    static final int MAX_EXCERPT_LENGTH = 60;

    private static final String ELLIPSIS = "...";

    private final NoteContainsKeywordsPredicate predicate;

    /**
     * Creates a {@code FindNotesCommand} that finds contacts matching {@code predicate}.
     */
    public FindNotesCommand(NoteContainsKeywordsPredicate predicate) {
        super(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        // Like find, leave the displayed list as it was when nothing matches.
        if (model.getContactFile().getPersonList().stream().noneMatch(predicate)) {
            return new CommandResult(MESSAGE_NO_MATCHING_NOTES);
        }

        model.updateFilteredPersonList(predicate);
        List<Person> matches = model.getFilteredPersonList();

        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            Person person = matches.get(i);
            if (i > 0) {
                groups.append("\n");
            }
            groups.append(i + 1).append(". ").append(person.getName());
            for (Interaction interaction : predicate.matchingInteractions(person)) {
                groups.append("\n   ").append(interaction.date).append(": ").append(excerpt(interaction.note));
            }
        }
        return new CommandResult(String.format(MESSAGE_MATCHES, matches.size(), groups));
    }

    /**
     * Returns {@code note} unchanged if it is short, or else an excerpt of at most {@link #MAX_EXCERPT_LENGTH}
     * characters that starts shortly before the first matching keyword, marking cut text with "...".
     */
    String excerpt(String note) {
        if (note.length() <= MAX_EXCERPT_LENGTH) {
            return note;
        }
        int matchIndex = Math.max(predicate.firstMatchIndex(note), 0);
        int start = Math.max(0, Math.min(matchIndex - MAX_EXCERPT_LENGTH / 4, note.length() - MAX_EXCERPT_LENGTH));
        int end = start + MAX_EXCERPT_LENGTH;
        return (start > 0 ? ELLIPSIS : "") + note.substring(start, end) + (end < note.length() ? ELLIPSIS : "");
    }
}
