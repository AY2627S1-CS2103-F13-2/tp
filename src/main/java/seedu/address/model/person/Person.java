package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the contact file.
 * Guarantees: the name and the tag set are present and not null, field values are validated, immutable.
 * Every other field is optional and may be absent.
 */
public class Person {

    // Identity fields
    private final Name name;

    // Data fields
    private final Company company;
    private final Role role;
    private final Phone phone;
    private final Email email;
    private final Linkedin linkedin;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Event> events = new HashSet<>();

    /**
     * Constructs a {@code Person} without any events.
     *
     * @param name name of the person
     * @param company company of the person
     * @param role role of the person
     * @param phone phone number of the person
     * @param email email of the person
     * @param linkedin LinkedIn profile of the person
     * @param tags tags associated with the person
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags) {
        this(name, company, role, phone, email, linkedin, tags, new HashSet<>());
    }

    /**
     * Constructs a {@code Person} with the given events.
     *
     * @param name name of the person
     * @param company company of the person
     * @param role role of the person
     * @param phone phone number of the person
     * @param email email of the person
     * @param linkedin LinkedIn profile of the person
     * @param tags tags associated with the person
     * @param events events associated with the person
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, Set<Event> events) {
        requireNonNull(name);
        requireNonNull(tags);
        requireNonNull(events);

        this.name = name;
        this.company = company;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.linkedin = linkedin;
        this.tags.addAll(tags);
        this.events.addAll(events);
    }

    public Name getName() {
        return name;
    }

    public Optional<Company> getCompany() {
        return Optional.ofNullable(company);
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    public Optional<Phone> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<Linkedin> getLinkedin() {
        return Optional.ofNullable(linkedin);
    }

    /**
     * Returns an immutable tag set.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns an immutable event set.
     */
    public Set<Event> getEvents() {
        return Collections.unmodifiableSet(events);
    }

    /**
     * Returns true if this person is associated with the given event.
     */
    public boolean hasEvent(Event event) {
        requireNonNull(event);
        return events.contains(event);
    }

    /**
     * Returns a copy of this person with the given event added.
     */
    public Person withEvent(Event event) {
        requireNonNull(event);

        Set<Event> updatedEvents = new HashSet<>(events);
        updatedEvents.add(event);

        return new Person(name, company, role, phone, email, linkedin, tags, updatedEvents);
    }

    /**
     * Returns true if both persons have the same name and both have an email or both do not.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName())
                && otherPerson.getEmail().isPresent() == getEmail().isPresent();
    }

    /**
     * Returns true if both persons have the same phone.
     */
    public boolean hasSamePhone(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both persons have the same email.
     */
    public boolean hasSameEmail(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && getEmail().isPresent()
                && otherPerson.getEmail().isPresent()
                && otherPerson.getEmail().orElseThrow().value
                        .equalsIgnoreCase(getEmail().orElseThrow().value);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && Objects.equals(company, otherPerson.company)
                && Objects.equals(role, otherPerson.role)
                && Objects.equals(phone, otherPerson.phone)
                && Objects.equals(email, otherPerson.email)
                && Objects.equals(linkedin, otherPerson.linkedin)
                && tags.equals(otherPerson.tags)
                && events.equals(otherPerson.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, company, role, phone, email, linkedin, tags, events);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("company", company)
                .add("role", role)
                .add("phone", phone)
                .add("email", email)
                .add("linkedin", linkedin)
                .add("tags", tags)
                .add("events", events)
                .toString();
    }
}
