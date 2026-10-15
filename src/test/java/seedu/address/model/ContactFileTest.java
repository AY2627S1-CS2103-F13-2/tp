package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_COMPANY_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class ContactFileTest {

    private final ContactFile contactFile = new ContactFile();

    @Test
    public void constructor() {
        assertEquals(List.of(), contactFile.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> contactFile.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyContactFile_replacesData() {
        ContactFile newData = getTypicalContactFile();
        contactFile.resetData(newData);
        assertEquals(newData, contactFile);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withCompany(VALID_COMPANY_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        ContactFileStub newData = new ContactFileStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> contactFile.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> contactFile.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInContactFile_returnsFalse() {
        assertFalse(contactFile.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInContactFile_returnsTrue() {
        contactFile.addPerson(ALICE);
        assertTrue(contactFile.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInContactFile_returnsTrue() {
        contactFile.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withCompany(VALID_COMPANY_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(contactFile.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> contactFile.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = ContactFile.class.getCanonicalName() + "{persons=" + contactFile.getPersonList() + "}";
        assertEquals(expected, contactFile.toString());
    }

    /**
     * A stub ReadOnlyContactFile whose persons list can violate interface constraints.
     */
    private static class ContactFileStub implements ReadOnlyContactFile {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        ContactFileStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
