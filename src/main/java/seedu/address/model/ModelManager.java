package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the contact file data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final ContactFile contactFile;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;

    /**
     * Initializes a ModelManager with the given contactFile and userPrefs.
     */
    public ModelManager(ReadOnlyContactFile contactFile, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(contactFile, userPrefs);

        logger.fine("Initializing with contact file: " + contactFile + " and user prefs " + userPrefs);

        this.contactFile = new ContactFile(contactFile);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.contactFile.getPersonList());
    }

    public ModelManager() {
        this(new ContactFile(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== ContactFile ================================================================================

    @Override
    public void setContactFile(ReadOnlyContactFile contactFile) {
        this.contactFile.resetData(contactFile);
    }

    @Override
    public ReadOnlyContactFile getContactFile() {
        return contactFile;
    }

    @Override
    public boolean hasPerson(Person person) {
        return contactFile.hasPerson(person);
    }

    @Override
    public boolean hasEmail(Person person) {
        return contactFile.hasEmail(person);
    }

    @Override
    public boolean hasPhone(Person person) {
        requireNonNull(person);
        return contactFile.hasPhone(person);
    }

    @Override
    public void deletePerson(Person target) {
        contactFile.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        contactFile.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        contactFile.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code contactFile}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return contactFile.equals(otherModelManager.contactFile)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

}
