package seedu.address.testutil;

import seedu.address.model.ContactFile;
import seedu.address.model.person.Person;

/**
 * A utility class to help with building ContactFile objects.
 * Example usage: <br>
 *     {@code ContactFile ab = new ContactFileBuilder().withPerson("John", "Doe").build();}
 */
public class ContactFileBuilder {

    private ContactFile contactFile;

    public ContactFileBuilder() {
        contactFile = new ContactFile();
    }

    public ContactFileBuilder(ContactFile contactFile) {
        this.contactFile = contactFile;
    }

    /**
     * Adds a new {@code Person} to the {@code ContactFile} that we are building.
     */
    public ContactFileBuilder withPerson(Person person) {
        contactFile.addPerson(person);
        return this;
    }

    public ContactFile build() {
        return contactFile;
    }
}
