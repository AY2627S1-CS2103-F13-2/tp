package seedu.address.logic;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DropCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.IntroduceCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Stores successful data-changing command history for one application session.
 */
public class UndoManager {
    public static final String MESSAGE_NOTHING_TO_UNDO = "There is nothing to undo.";
    public static final String MESSAGE_CONFIRM_UNDO = "Undo the following %1$s?\n%2$s\n"
            + "To confirm, run: undo --confirm";
    public static final String MESSAGE_UNDO_SUCCESS = "Restored contact:\n%1$s";
    public static final String MESSAGE_UNDO_CONFLICT =
            "The contact cannot be restored because it would violate contact uniqueness rules. No changes were made.";
    public static final String MESSAGE_HISTORY_EMPTY = "There is no data-changing command history.";

    private final List<HistoryEntry> history = new ArrayList<>();
    private HistoryEntry undoableEntry;
    private HistoryEntry pendingEntry;

    /**
     * Records a successful data-changing command and updates the available undo action.
     */
    public void recordChange(Command command, List<Person> before, List<Person> after, String feedback) {
        if (before.equals(after)) {
            return;
        }
        String action = getHistoryAction(command);
        HistoryEntry entry = action == null ? null : new HistoryEntry(action, feedback, before, after);
        if (entry != null) {
            history.add(entry);
        }
        if (command instanceof EditCommand || command instanceof DeleteCommand) {
            undoableEntry = entry;
        } else {
            undoableEntry = null;
        }
        pendingEntry = null;
    }

    private String getHistoryAction(Command command) {
        if (command instanceof AddCommand) {
            return "addition";
        } else if (command instanceof EditCommand) {
            return "edit";
        } else if (command instanceof DeleteCommand) {
            return "deletion";
        } else if (command instanceof DropCommand) {
            return "drop";
        } else if (command instanceof ClearCommand) {
            return "clear";
        } else if (command instanceof IntroduceCommand) {
            return "introduce";
        }
        return null;
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
            return previewUndo();
        }
        if (pendingEntry != undoableEntry) {
            return new CommandResult(MESSAGE_NOTHING_TO_UNDO, false, false, false);
        }

        return confirmUndo(model);
    }

    private CommandResult previewUndo() {
        pendingEntry = undoableEntry;
        return new CommandResult(String.format(MESSAGE_CONFIRM_UNDO,
                undoableEntry.action, undoableEntry.feedback), false, false, false);
    }

    private CommandResult confirmUndo(Model model) {
        Person restored = undoableEntry.getRestoredPerson();
        Person personToReplace = undoableEntry.getPersonToReplace();
        List<Person> currentPersons = new ArrayList<>(model.getContactFile().getPersonList());
        int replaceIndex = personToReplace == null ? -1 : currentPersons.indexOf(personToReplace);
        if (restored == null || (personToReplace != null && replaceIndex < 0)) {
            pendingEntry = null;
            return new CommandResult(MESSAGE_UNDO_CONFLICT, false, false, false);
        }

        restorePerson(currentPersons, restored, replaceIndex);
        if (violatesUniqueness(currentPersons)) {
            pendingEntry = null;
            return new CommandResult(MESSAGE_UNDO_CONFLICT, false, false, false);
        }

        updateModel(model, currentPersons);
        HistoryEntry undoneEntry = undoableEntry;
        pendingEntry = null;
        undoableEntry = null;
        undoneEntry.isUndone = true;
        return new CommandResult(String.format(MESSAGE_UNDO_SUCCESS, Messages.format(restored)));
    }

    private void restorePerson(List<Person> currentPersons, Person restored, int replaceIndex) {
        if (replaceIndex < 0) {
            currentPersons.add(Math.min(undoableEntry.getOriginalIndex(), currentPersons.size()), restored);
        } else {
            currentPersons.set(replaceIndex, restored);
        }
    }

    private void updateModel(Model model, List<Person> persons) {
        ContactFile restoredContactFile = new ContactFile();
        restoredContactFile.setPersons(persons);
        model.setContactFile(restoredContactFile);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
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
