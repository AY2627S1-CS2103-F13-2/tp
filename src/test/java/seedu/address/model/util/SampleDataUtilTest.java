package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ContactFile;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.FollowUpFilter;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonContactFileStorage;

public class SampleDataUtilTest {
    @TempDir
    public Path testFolder;

    @Test
    public void getSamplePersons_demonstratesNetworkingFeaturesWithValidIntroductions() {
        List<Person> people = Arrays.asList(SampleDataUtil.getSamplePersons(LocalDate.of(2026, 10, 28)));
        Set<String> names = people.stream().map(person -> person.getName().fullName)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(people.stream().anyMatch(Person::isStarred));
        assertTrue(people.stream().anyMatch(person -> !person.isStarred()));
        assertTrue(people.stream().anyMatch(person -> person.getIntroducer().isPresent()));
        for (Person person : people) {
            assertFalse(person.getFollowUps().isEmpty());
            assertFalse(person.getInteractions().isEmpty());
            assertFalse(person.getEvents().isEmpty());
            assertTrue(person.getTags().size() >= 2);
            person.getIntroducer().ifPresent(introducer -> {
                assertTrue(names.contains(introducer.fullName));
                assertFalse(introducer.equals(person.getName()));
            });
            assertEquals(person.getFollowUps().size(), person.getFollowUps().stream()
                    .map(action -> action.getId()).distinct().count());
        }
    }

    @Test
    public void getSamplePersons_everyFollowUpFilterHasResultsAtDifferentReferenceDates() {
        for (LocalDate today : List.of(LocalDate.of(2026, 10, 28), LocalDate.of(2040, 2, 29))) {
            List<FollowUpEntry> entries = Arrays.stream(SampleDataUtil.getSamplePersons(today))
                    .flatMap(person -> person.getFollowUps().stream().map(action -> new FollowUpEntry(person, action)))
                    .toList();
            for (FollowUpFilter filter : FollowUpFilter.values()) {
                assertFalse(filter.apply(entries, today).isEmpty(), filter + " should have sample results on " + today);
            }
            assertTrue(FollowUpFilter.OVERDUE.apply(entries, today).stream()
                    .allMatch(entry -> entry.followUp().getDueDate().orElseThrow().isBefore(today)));
        }
    }

    @Test
    public void getSamplePersons_jsonRoundTripPreservesAllFeaturesAndDates() throws Exception {
        ContactFile samples = new ContactFile();
        Arrays.stream(SampleDataUtil.getSamplePersons(LocalDate.of(2026, 10, 28))).forEach(samples::addPerson);
        JsonContactFileStorage storage = new JsonContactFileStorage(testFolder.resolve("samples.json"));
        storage.saveContactFile(samples);
        assertEquals(samples, storage.readContactFile().orElseThrow());
    }

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
        // the sample data is loaded into a ContactFile, which rejects two persons with the same name
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
    public void getSampleContactFile_containsEverySamplePerson() {
        ReadOnlyContactFile sampleContactFile = SampleDataUtil.getSampleContactFile();

        assertEquals(Arrays.asList(SampleDataUtil.getSamplePersons()), sampleContactFile.getPersonList());
    }

    @Test
    public void getTagSet_returnsTagsForEveryName() {
        assertEquals(Set.of(), SampleDataUtil.getTagSet());
        assertEquals(Set.of(new Tag("careerfair")), SampleDataUtil.getTagSet("careerfair"));
        assertEquals(Set.of(new Tag("careerfair"), new Tag("referral")),
                SampleDataUtil.getTagSet("careerfair", "referral"));
    }
}
