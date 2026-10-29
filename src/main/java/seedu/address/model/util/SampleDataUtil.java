package seedu.address.model.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.ContactFile;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code ContactFile} with sample data.
 */
public class SampleDataUtil {
    /**
     * Returns a networking sample with dates relative to the day it is first loaded.
     */
    public static Person[] getSamplePersons() {
        return getSamplePersons(LocalDate.now());
    }

    /**
     * Builds the examples against one reference day so all follow-up views have relevant results.
     */
    static Person[] getSamplePersons(LocalDate today) {
        Event careerFair = new Event("Tech Careers Fair", today.minusDays(10).toString());
        Event meetup = new Event("Developer Community Meetup", today.minusDays(4).toString());
        Event alumni = new Event("Alumni Networking Evening", today.minusDays(7).toString());
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Company("Shopee"), new Role("Software Engineer"),
                new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Linkedin("linkedin.com/in/alexyeoh"), new Name("Bernice Yu"),
                getTagSet("careerfair", "engineering", "mentor"), Set.of(careerFair, meetup),
                List.of(new FollowUp(1, "Send portfolio and thank-you email", FollowUp.Status.PENDING,
                            today.minusDays(2)),
                        new FollowUp(2, "Review the graduate engineering role", FollowUp.Status.PENDING, today),
                        new FollowUp(3, "Connect on LinkedIn", FollowUp.Status.COMPLETED, today.minusDays(6))),
                true, List.of(new Interaction(today.minusDays(10),
                            "Bernice introduced us at the careers fair. Discussed backend engineering internships."),
                        new Interaction(today.minusDays(4),
                            "Caught up at the developer meetup. Alex offered to review my portfolio."))),
            new Person(new Name("Bernice Yu"), new Company("GovTech"), new Role("Product Manager"),
                new Phone("99272758"), new Email("berniceyu@example.com"),
                new Linkedin("linkedin.com/in/berniceyu"), new Name("Roy Balakrishnan"),
                getTagSet("careerfair", "referral", "product"), Set.of(careerFair),
                List.of(new FollowUp(1, "Arrange a coffee chat about public-sector product work",
                            FollowUp.Status.PENDING, today.plusDays(3)),
                        new FollowUp(2, "Share a product case study when it is ready", FollowUp.Status.PENDING),
                        new FollowUp(3, "Thank Roy for the introduction", FollowUp.Status.COMPLETED)),
                true, List.of(new Interaction(today.minusDays(10),
                            "Roy introduced Bernice, who explained how her team researches citizen needs."),
                        new Interaction(today.minusDays(5),
                            "Bernice introduced Alex and suggested preparing a short product case study."))),
            // Only a name, a company and an email were exchanged before the conversation moved on.
            new Person(new Name("Charlotte Oliveiro"), new Company("Grab"), null,
                null, new Email("charlotte@example.com"), null, new Name("Alex Yeoh"),
                getTagSet("techmeetup", "engineering"), Set.of(meetup),
                List.of(new FollowUp(1, "Ask about the next developer community session",
                        FollowUp.Status.PENDING, today.plusDays(7))),
                false, List.of(new Interaction(today.minusDays(4),
                        "Alex introduced Charlotte after the meetup. Exchanged email addresses before she left."))),
            new Person(new Name("David Li"), new Company("DBS"), new Role("Quantitative Analyst"),
                new Phone("91031282"), new Email("lidavid@example.com"),
                new Linkedin("linkedin.com/in/davidli"), null,
                getTagSet("alumni", "finance", "mentor"), Set.of(alumni),
                List.of(new FollowUp(1, "Send the quantitative finance reading list", FollowUp.Status.COMPLETED,
                            today.minusDays(3)),
                        new FollowUp(2, "Ask for feedback on the research project", FollowUp.Status.PENDING)),
                false, List.of(new Interaction(today.minusDays(7),
                            "Met at the alumni evening. David recommended starting with a small research project."),
                        new Interaction(today.minusDays(3),
                            "Shared the reading list and discussed an introduction to Irfan."))),
            // Met after a talk, with only a LinkedIn profile to follow up on.
            new Person(new Name("Irfan Ibrahim"), new Company("Sea"), new Role("Data Scientist"),
                null, null, new Linkedin("linkedin.com/in/irfanibrahim"), new Name("David Li"),
                getTagSet("conference", "data", "referral"), Set.of(meetup),
                List.of(new FollowUp(1, "Share the recommendation-system project outline",
                        FollowUp.Status.PENDING, today.plusDays(14))),
                true, List.of(new Interaction(today.minusDays(4),
                        "David introduced Irfan after the data science talk. He suggested a recommendation project."))),
            new Person(new Name("Roy Balakrishnan"), new Company("Stripe"), new Role("Recruiter"),
                new Phone("92624417"), new Email("royb@example.com"),
                new Linkedin("linkedin.com/in/roybala"), null,
                getTagSet("careerfair", "recruiter", "referral"), Set.of(careerFair),
                List.of(new FollowUp(1, "Send updated resume and availability", FollowUp.Status.PENDING, today),
                        new FollowUp(2, "Review the application guide", FollowUp.Status.COMPLETED,
                            today.minusDays(8))),
                true, List.of(new Interaction(today.minusDays(10),
                            "Discussed graduate hiring timelines. Roy introduced Bernice at the careers fair."),
                        new Interaction(today.minusDays(2),
                            "Roy asked for an updated resume and availability for an introductory call.")))
        };
    }

    public static ReadOnlyContactFile getSampleContactFile() {
        ContactFile sampleAb = new ContactFile();
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
