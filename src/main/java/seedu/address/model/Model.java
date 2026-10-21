package seedu.address.model;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /**
     * Matches every contact.
     */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns the global contact display order.
     */
    FieldOrder getFieldOrder();

    /**
     * Sets the global display order and notifies bound views.
     */
    void setFieldOrder(FieldOrder fieldOrder);

    /**
     * Returns the observable display order without exposing a writable property.
     */
    ReadOnlyObjectProperty<FieldOrder> getFieldOrderProperty();

    /**
     * Replaces contact file data with the data in {@code contactFile}.
     */
    void setContactFile(ReadOnlyContactFile contactFile);

    /**
     * Returns the contact file.
     */
    ReadOnlyContactFile getContactFile();

    /**
     * Returns an unmodifiable snapshot of pending follow-ups across all contacts, regardless of the contact filter.
     * Dates sort ascending, with undated actions last. Ties retain contact order, then stored action order.
     */
    List<FollowUpEntry> getPendingFollowUps();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the contact file.
     */
    boolean hasPerson(Person person);

    /**
     * Returns true if a person with the same phone as {@code person} exists in the contact file.
     */
    boolean hasPhone(Person person);

    /**
     * Return true if a person with the same email as {@code person} exists in the address book.
     */
    boolean hasEmail(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the contact file.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the contact file.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the contact file.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the contact file.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Returns an unmodifiable view of the filtered person list.
     */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the active display predicate, or null when the list has no filter.
     */
    Predicate<Person> getPersonFilter();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     *
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
