package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Company("Shopee"), new Role("Software Engineer"),
                new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Linkedin("linkedin.com/in/alexyeoh"), getTagSet("careerfair")),
            new Person(new Name("Bernice Yu"), new Company("GovTech"), new Role("Product Manager"),
                new Phone("99272758"), new Email("berniceyu@example.com"),
                new Linkedin("linkedin.com/in/berniceyu"), getTagSet("careerfair", "referral")),
            // Only a name, a company and an email were exchanged before the conversation moved on.
            new Person(new Name("Charlotte Oliveiro"), new Company("Grab"), null,
                null, new Email("charlotte@example.com"), null, getTagSet("techmeetup")),
            new Person(new Name("David Li"), new Company("DBS"), new Role("Quantitative Analyst"),
                new Phone("91031282"), new Email("lidavid@example.com"),
                new Linkedin("linkedin.com/in/davidli"), getTagSet("alumni")),
            // Met at a conference talk, with only a LinkedIn profile to follow up on.
            new Person(new Name("Irfan Ibrahim"), new Company("Sea"), new Role("Data Scientist"),
                null, null, new Linkedin("linkedin.com/in/irfanibrahim"), getTagSet("conference")),
            new Person(new Name("Roy Balakrishnan"), new Company("Stripe"), new Role("Recruiter"),
                new Phone("92624417"), new Email("royb@example.com"),
                new Linkedin("linkedin.com/in/roybala"), getTagSet("careerfair", "recruiter"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
