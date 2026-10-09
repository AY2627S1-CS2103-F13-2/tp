package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_allHaveAName() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();

        assertTrue(samplePersons.length > 0);
        for (Person person : samplePersons) {
            assertFalse(person.getName().fullName.isBlank());
        }
    }

    @Test
    public void getSamplePersons_namesAreUnique() {
        // the sample data is loaded into an AddressBook, which rejects two persons with the same name
        List<String> names = Arrays.stream(SampleDataUtil.getSamplePersons())
                .map(person -> person.getName().fullName)
                .toList();

        assertEquals(names.size(), names.stream().distinct().count());
    }

    @Test
    public void getSamplePersons_someOmitOptionalFields() {
        // the sample data should show what a partially captured contact looks like, since
        // every field other than the name is optional
        Person[] samplePersons = SampleDataUtil.getSamplePersons();

        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getPhone().isEmpty()));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getRole().isEmpty()));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getLinkedin().isEmpty()));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getEmail().isEmpty()));

        // and at least one should still be filled in completely
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getCompany().isPresent()
                && person.getRole().isPresent() && person.getPhone().isPresent()
                && person.getEmail().isPresent() && person.getLinkedin().isPresent()));
    }

    @Test
    public void getSampleAddressBook_containsEverySamplePerson() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();

        assertEquals(Arrays.asList(SampleDataUtil.getSamplePersons()), sampleAddressBook.getPersonList());
    }

    @Test
    public void getTagSet_returnsTagsForEveryName() {
        assertEquals(Set.of(), SampleDataUtil.getTagSet());
        assertEquals(Set.of(new Tag("careerfair")), SampleDataUtil.getTagSet("careerfair"));
        assertEquals(Set.of(new Tag("careerfair"), new Tag("referral")),
                SampleDataUtil.getTagSet("careerfair", "referral"));
    }
}
