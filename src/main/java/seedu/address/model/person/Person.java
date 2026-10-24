package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the contact file.
 * Guarantees: the name, tag set, events and ordered action list are present and immutable.
 * Contact details other than the name are optional; all field values are validated.
 */
public class Person {

    private final Name name;
    private final Company company;
    private final Role role;
    private final Phone phone;
    private final Email email;
    private final Linkedin linkedin;
    private final Name introducer;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Event> events = new HashSet<>();
    private final List<FollowUp> followUps = new ArrayList<>();
    private final boolean starred;

    /**
     * Constructs a person without events, follow-ups or starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Set<Tag> tags) {
        this(name, company, role, phone, email, linkedin,
                null, tags, Set.of(), List.of(), false);
    }

    /**
     * Constructs a person with an optional introducer name.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Name introducer, Set<Tag> tags) {
        this(name, company, role, phone, email, linkedin,
                introducer, tags, Set.of(), List.of(), false);
    }

    /**
     * Constructs a person with follow-ups but without events or starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, List<FollowUp> followUps) {
        this(name, company, role, phone, email, linkedin,
                null, tags, Set.of(), followUps, false);
    }

    /**
     * Constructs a person with events but without follow-ups or starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Set<Tag> tags, Set<Event> events) {
        this(name, company, role, phone, email, linkedin,
                null, tags, events, List.of(), false);
    }

    /**
     * Constructs a person with follow-ups but without events or starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, List<FollowUp> followUps, boolean starred) {
        this(name, company, role, phone, email, linkedin,
                null, tags, Set.of(), followUps, starred);
    }

    /**
     * Constructs a person with starred status but without events or follow-ups.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, boolean starred) {
        this(name, company, role, phone, email, linkedin,
                null, tags, Set.of(), List.of(), starred);
    }

    /**
     * Constructs a person with introducer and follow-ups but without events.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Name introducer, Set<Tag> tags, List<FollowUp> followUps,
            boolean starred) {
        this(name, company, role, phone, email, linkedin,
                introducer, tags, Set.of(), followUps, starred);
    }

    /**
     * Constructs a person with events, follow-ups and starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Set<Tag> tags, Set<Event> events,
            List<FollowUp> followUps, boolean starred) {
        this(name, company, role, phone, email, linkedin,
                null, tags, events, followUps, starred);
    }

    /**
     * Constructs a person with introducer, events, follow-ups and starred status.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email,
            Linkedin linkedin, Name introducer, Set<Tag> tags, Set<Event> events,
            List<FollowUp> followUps, boolean starred) {
        requireNonNull(name);
        requireNonNull(tags);
        requireNonNull(events);
        requireNonNull(followUps);

        this.name = name;
        this.company = company;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.linkedin = linkedin;
        this.introducer = introducer;
        this.starred = starred;

        this.tags.addAll(tags);
        this.events.addAll(events);
        this.followUps.addAll(followUps);

        Set<Integer> actionIds = new HashSet<>();
        for (FollowUp action : this.followUps) {
            requireNonNull(action);
            if (!actionIds.add(action.getId())) {
                throw new IllegalArgumentException(
                        "Follow-up IDs must be unique within a contact.");
            }
        }
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

    public Optional<Name> getIntroducer() {
        return Optional.ofNullable(introducer);
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
     * Returns an immutable follow-up list.
     */
    public List<FollowUp> getFollowUps() {
        return Collections.unmodifiableList(followUps);
    }

    /**
     * Returns true if this person is starred.
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * Returns a copy with the requested starred status.
     */
    public Person withStarred(boolean starred) {
        return new Person(name, company, role, phone, email, linkedin,
                introducer, tags, events, followUps, starred);
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

        return new Person(name, company, role, phone, email, linkedin,
                introducer, tags, updatedEvents, followUps, starred);
    }

    /**
     * Returns a copy with the given follow-up appended.
     */
    public Person appendFollowUp(FollowUp action) {
        requireNonNull(action);

        List<FollowUp> updatedActions = new ArrayList<>(followUps);
        if (updatedActions.contains(action)) {
            throw new IllegalArgumentException("Follow-up already exists.");
        }

        updatedActions.add(action);
        return new Person(name, company, role, phone, email, linkedin,
                introducer, tags, events, updatedActions, starred);
    }

    /**
     * Returns true if both persons have the same name and email presence.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        if (otherPerson == null || !otherPerson.getName().equals(getName())) {
            return false;
        }

        Optional<Email> otherEmail = otherPerson.getEmail();
        Optional<Email> email = getEmail();
        return otherEmail.isEmpty() && email.isEmpty()
                || otherEmail.isPresent() && email.isPresent()
                        && email.orElseThrow().matches(otherEmail.orElseThrow().value);
    }

    /**
     * Returns true if both persons have a phone and their normalized numbers match.
     */
    public boolean hasSamePhone(Person otherPerson) {
        return otherPerson != null
                && getPhone().isPresent()
                && otherPerson.getPhone().isPresent()
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both persons have the same email.
     */
    public boolean hasSameEmail(Person otherPerson) {
        return otherPerson != null
                && getEmail().isPresent()
                && otherPerson.getEmail().isPresent()
                && getEmail().orElseThrow()
                        .matches(otherPerson.getEmail().orElseThrow().value);
    }

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
                && Objects.equals(introducer, otherPerson.introducer)
                && tags.equals(otherPerson.tags)
                && events.equals(otherPerson.events)
                && followUps.equals(otherPerson.followUps)
                && starred == otherPerson.starred;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, company, role, phone, email, linkedin,
                introducer, tags, events, followUps, starred);
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
                .add("introducer", introducer)
                .add("tags", tags)
                .add("events", events)
                .add("followUps", followUps)
                .add("starred", starred)
                .toString();
    }
}
