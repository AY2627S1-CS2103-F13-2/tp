package seedu.address.model;

import java.util.function.Predicate;

import seedu.address.model.person.Person;

/**
 * Captures contact data, preferences, and the active filter before a command or pending request.
 */
public final class ModelSnapshot {

    private final ContactFile contactFile;
    private final UserPrefs userPrefs;
    private final Predicate<Person> filter;

    /**
     * Copies the data and retains the predicate used by {@code model}.
     */
    public ModelSnapshot(Model model) {
        contactFile = new ContactFile(model.getContactFile());
        userPrefs = new UserPrefs(model.getUserPrefs());
        filter = model.getPersonFilter();
    }

    /**
     * Returns whether the model's contact data differs from the captured data.
     */
    public boolean hasDataChanged(Model model) {
        return !contactFile.equals(model.getContactFile());
    }

    /**
     * Returns whether user preferences differ from the captured preferences.
     */
    public boolean hasPreferencesChanged(Model model) {
        return !userPrefs.equals(model.getUserPrefs());
    }

    /**
     * Restores contacts, preferences, and the filter while preserving observable-list bindings.
     */
    public void restore(Model model) {
        if (hasDataChanged(model)) {
            model.setContactFile(contactFile);
        }
        if (hasPreferencesChanged(model)) {
            model.setGuiSettings(userPrefs.getGuiSettings());
            model.setFieldOrder(userPrefs.getFieldOrder());
        }
        model.updateFilteredPersonList(filter == null ? Model.PREDICATE_SHOW_ALL_PERSONS : filter);
    }
}
