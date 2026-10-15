package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the contact-file level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class ContactFile implements ReadOnlyContactFile {

    private final UniquePersonList persons = new UniquePersonList();

    public ContactFile() {}

    /**
     * Creates a ContactFile using the Persons in the {@code toBeCopied}
     */
    public ContactFile(ReadOnlyContactFile toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code ContactFile} with {@code newData}.
     */
    public void resetData(ReadOnlyContactFile newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the contact file.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Returns true if a person with the same email as {@code person} exists in the contact file.
     */
    public boolean hasEmail(Person person) {
        requireNonNull(person);
        return persons.containsEmail(person);
    }

    /**
     * Returns true if a person with the same phone as {@code person} exists in the contact file.
     */
    public boolean hasPhone(Person person) {
        requireNonNull(person);
        return persons.containsPhone(person);
    }

    /**
     * Adds a person to the contact file.
     * The person must not already exist in the contact file.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the contact file.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the contact file.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code ContactFile}.
     * {@code key} must exist in the contact file.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ContactFile otherContactFile)) {
            return false;
        }

        return persons.equals(otherContactFile.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
