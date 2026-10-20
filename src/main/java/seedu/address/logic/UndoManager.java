package seedu.address.logic;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Stores successful edit and deletion history for one application session.
 */
public class UndoManager {
    public static final String MESSAGE_NOTHING_TO_UNDO = "There is nothing to undo.";
    public static final String MESSAGE_CONFIRM_UNDO = "Undo the following %1$s?\n%2$s\n"
            + "To confirm, run: undo --confirm";
    public static final String MESSAGE_UNDO_SUCCESS = "Restored contact:\n%1$s";
    public static final String MESSAGE_UNDO_CONFLICT =
            "The contact cannot be restored because it would violate contact uniqueness rules. No changes were made.";
    public static final String MESSAGE_HISTORY_EMPTY = "There is no edit or deletion history.";

    private final List<HistoryEntry> history = new ArrayList<>();
    private HistoryEntry undoableEntry;
    private HistoryEntry pendingEntry;

    /**
     * Records a successful edit or deletion, or invalidates the undo action when another command changed data.
     */
    public void recordChange(Command command, List<Person> before, List<Person> after, String feedback) {
        if (before.equals(after)) {
            return;
        }
        if (command instanceof EditCommand || command instanceof DeleteCommand) {
            HistoryEntry entry = new HistoryEntry(command instanceof EditCommand ? "edit" : "deletion",
                    feedback, before, after);
            history.add(entry);
            undoableEntry = entry;
        } else {
            undoableEntry = null;
        }
        pendingEntry = null;
    }

    /**
     * Starts or confirms an undo operation.
     */
    public CommandResult executeUndo(Model model, boolean confirmed) throws CommandException {
        if (undoableEntry == null) {
            pendingEntry = null;
            return new CommandResult(MESSAGE_NOTHING_TO_UNDO, false, false, false);
        }
        if (!confirmed) {
            pendingEntry = undoableEntry;
            return new CommandResult(String.format(MESSAGE_CONFIRM_UNDO,
                    undoableEntry.action, undoableEntry.feedback), false, false, false);
        }
        if (pendingEntry != undoableEntry) {
            return new CommandResult(MESSAGE_NOTHING_TO_UNDO, false, false, false);
        }

        Person restored = undoableEntry.getRestoredPerson();
        Person personToReplace = undoableEntry.getPersonToReplace();
        List<Person> currentPersons = new ArrayList<>(model.getContactFile().getPersonList());
        int replaceIndex = personToReplace == null ? -1 : currentPersons.indexOf(personToReplace);
        if (restored == null || (personToReplace != null && replaceIndex < 0)) {
            pendingEntry = null;
            return new CommandResult(MESSAGE_UNDO_CONFLICT, false, false, false);
        }

        if (personToReplace == null) {
            currentPersons.add(Math.min(undoableEntry.getOriginalIndex(), currentPersons.size()), restored);
        } else {
            currentPersons.set(replaceIndex, restored);
        }
        if (violatesUniqueness(currentPersons)) {
            pendingEntry = null;
            return new CommandResult(MESSAGE_UNDO_CONFLICT, false, false, false);
        }
        ContactFile restoredContactFile = new ContactFile();
        restoredContactFile.setPersons(currentPersons);
        model.setContactFile(restoredContactFile);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        HistoryEntry undoneEntry = undoableEntry;
        pendingEntry = null;
        undoableEntry = null;
        undoneEntry.isUndone = true;
        return new CommandResult(String.format(MESSAGE_UNDO_SUCCESS, Messages.format(restored)));
    }

    /**
     * Returns the edit and deletion history for this session.
     */
    public String getHistory() {
        if (history.isEmpty()) {
            return MESSAGE_HISTORY_EMPTY;
        }
        List<String> lines = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            HistoryEntry entry = history.get(i);
            lines.add(String.format("%d. %s%s: %s", history.size() - i, entry.action,
                    entry.isUndone ? " (undone)" : "", entry.feedback.replace("\n", " ")));
        }
        return String.join("\n", lines);
    }

    private static boolean violatesUniqueness(List<Person> persons) {
        for (int i = 0; i < persons.size(); i++) {
            Person first = persons.get(i);
            for (int j = i + 1; j < persons.size(); j++) {
                Person second = persons.get(j);
                boolean samePhone = first.getPhone().isPresent() && second.getPhone().isPresent()
                        && first.hasSamePhone(second);
                if (first.isSamePerson(second) || samePhone || first.hasSameEmail(second)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class HistoryEntry {
        private final String action;
        private final String feedback;
        private final List<Person> before;
        private final List<Person> after;
        private boolean isUndone;

        private HistoryEntry(String action, String feedback, List<Person> before, List<Person> after) {
            this.action = action;
            this.feedback = feedback;
            this.before = List.copyOf(before);
            this.after = List.copyOf(after);
        }

        private Person getRestoredPerson() {
            return before.stream().filter(person -> !after.contains(person)).findFirst().orElse(null);
        }

        private Person getPersonToReplace() {
            return after.stream().filter(person -> !before.contains(person)).findFirst().orElse(null);
        }

        private int getOriginalIndex() {
            return before.indexOf(getRestoredPerson());
        }
    }
}
