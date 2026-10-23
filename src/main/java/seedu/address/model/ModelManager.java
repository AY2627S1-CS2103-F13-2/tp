package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the contact file data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final ContactFile contactFile;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final ReadOnlyObjectWrapper<FieldOrder> fieldOrder;

    /**
     * Initializes a ModelManager with the given contactFile and userPrefs.
     */
    public ModelManager(ReadOnlyContactFile contactFile, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(contactFile, userPrefs);

        logger.fine("Initializing with contact file: " + contactFile + " and user prefs " + userPrefs);

        this.contactFile = new ContactFile(contactFile);
        this.userPrefs = new UserPrefs(userPrefs);
        fieldOrder = new ReadOnlyObjectWrapper<>(this.userPrefs.getFieldOrder());
        filteredPersons = new FilteredList<>(this.contactFile.getPersonList());
    }

    /**
     * Creates an empty model with default preferences.
     */
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

    @Override
    public FieldOrder getFieldOrder() {
        return fieldOrder.get();
    }

    @Override
    public void setFieldOrder(FieldOrder order) {
        userPrefs.setFieldOrder(requireNonNull(order));
        fieldOrder.set(order);
    }

    @Override
    public ReadOnlyObjectProperty<FieldOrder> getFieldOrderProperty() {
        return fieldOrder.getReadOnlyProperty();
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
    public List<FollowUpEntry> getPendingFollowUps() {
        return contactFile.getPersonList().stream()
                .flatMap(contact -> contact.getFollowUps().stream()
                        .filter(action -> action.getStatus() == FollowUp.Status.PENDING)
                        .map(action -> new FollowUpEntry(contact, action)))
                .sorted(Comparator.comparing(entry -> entry.followUp().getDueDate().orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
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
    public Predicate<Person> getPersonFilter() {
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        return predicate == null ? null : predicate::test;
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
