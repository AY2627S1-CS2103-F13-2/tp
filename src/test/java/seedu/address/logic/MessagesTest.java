package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.FieldOrder;
import seedu.address.model.person.Event;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    private static final Interaction COFFEE_CHAT = new Interaction(LocalDate.of(2026, 10, 1), "Coffee chat");

    @Test
    public void format_introducerAndInteractions_introducerFollowsLinkedin() {
        Person person = new PersonBuilder().withName("Alex Yeoh").withTags().withCompany(null).withRole(null)
                .withPhone(null).withEmail(null).withLinkedin("linkedin.com/in/alex").withIntroducer("Jane")
                .build().withInteraction(COFFEE_CHAT);

        assertEquals("Alex Yeoh; LinkedIn: linkedin.com/in/alex; Introduced by: Jane; "
                + "Interaction history:\n1. 2026-10-01: Coffee chat", Messages.format(person));
    }

    @Test
    public void format_introducerWithoutLinkedin_introducerKeepsItsPlace() {
        Person person = new PersonBuilder().withName("Alex Yeoh").withTags().withCompany(null).withRole(null)
                .withPhone(null).withEmail(null).withLinkedin(null).withIntroducer("Jane")
                .build().withInteraction(COFFEE_CHAT);

        assertEquals("Alex Yeoh; Introduced by: Jane; Interaction history:\n1. 2026-10-01: Coffee chat",
                Messages.format(person));
    }

    @Test
    public void format_customOrder_introducerStillFollowsLinkedin() {
        Person person = new PersonBuilder().withName("Alex Yeoh").withTags().withCompany(null).withRole(null)
                .withPhone(null).withEmail(null).withLinkedin("linkedin.com/in/alex").withIntroducer("Jane")
                .build().withInteraction(COFFEE_CHAT);
        FieldOrder order = new FieldOrder(List.of("interactions", "linkedin", "name", "tags", "company", "role",
                "phone", "email", "followups"));

        assertEquals("Interaction history:\n1. 2026-10-01: Coffee chat; LinkedIn: linkedin.com/in/alex; "
                + "Introduced by: Jane; Alex Yeoh", Messages.format(person, order));
    }

    @Test
    public void format_withEvents_includesEvents() {
        Event event = new Event("NUS Career Fair");
        Person person = new PersonBuilder()
                .withName("Alice Tan")
                .withTags()
                .withCompany(null)
                .withRole(null)
                .withPhone(null)
                .withEmail(null)
                .withLinkedin(null)
                .withIntroducer(null)
                .build()
                .withEvent(event);

        assertEquals("Alice Tan; Events: " + event, Messages.format(person));
    }
}
