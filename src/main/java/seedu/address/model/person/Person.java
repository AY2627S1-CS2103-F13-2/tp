package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the contact file.
 * Guarantees: the name, tag set, and ordered action list are present and immutable.
 * Contact details other than the name are optional; all field values are validated.
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
    private final List<FollowUp> followUps;
    private final boolean starred;
    private final List<Interaction> interactions;

    /**
     * Constructs a {@code Person}. Only {@code name} and {@code tags} are required; a networking
     * contact is often captured with just a name and whatever details were exchanged, so
     * {@code company}, {@code role}, {@code phone}, {@code email} and {@code linkedin} may be null.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags) {
        this(name, company, role, phone, email, linkedin, tags, List.of());
    }

    /**
     * Creates a contact with an ordered immutable action list whose IDs are unique.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, List<FollowUp> followUps) {
        this(name, company, role, phone, email, linkedin, tags, followUps, false);
    }

    /**
     * Creates a contact with the given starred status and no action history.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, boolean starred) {
        this(name, company, role, phone, email, linkedin, tags, List.of(), starred);
    }

    /**
     * Creates a contact retaining both starred status and ordered action history, with no interactions.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, List<FollowUp> followUps, boolean starred) {
        this(name, company, role, phone, email, linkedin, tags, followUps, starred, List.of());
    }

    /**
     * Creates a contact with every field. All other constructors and copy methods go through this one,
     * so no field is lost when a contact is copied. {@code interactions} is kept in date order;
     * interactions on the same date keep their given order.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, List<FollowUp> followUps, boolean starred, List<Interaction> interactions) {
        requireNonNull(interactions);
        List<Interaction> sortedInteractions = new ArrayList<>(interactions);
        sortedInteractions.sort(Comparator.comparing(Interaction::getDate));
        this.interactions = List.copyOf(sortedInteractions);
        this.followUps = List.copyOf(followUps);
        Set<Integer> actionIds = new HashSet<>();
        for (FollowUp action : this.followUps) {
            if (!actionIds.add(action.getId())) {
                throw new IllegalArgumentException("Follow-up IDs must be unique within a contact.");
            }
        }
        requireNonNull(name);
        requireNonNull(tags);
        this.name = name;
        this.company = company;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.linkedin = linkedin;
        this.tags.addAll(tags);
        this.starred = starred;
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
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean isStarred() {
        return starred;
    }

    /**
     * Returns a copy with the requested starred status, retaining all contact data and actions.
     */
    public Person withStarred(boolean starred) {
        return new Person(name, company, role, phone, email, linkedin, tags, followUps, starred, interactions);
    }

    public List<FollowUp> getFollowUps() {
        return followUps;
    }

    /**
     * Returns a copy with the action appended, retaining all existing fields and action order.
     */
    public Person appendFollowUp(FollowUp action) {
        List<FollowUp> updatedActions = new ArrayList<>(followUps);
        updatedActions.add(requireNonNull(action));
        return new Person(name, company, role, phone, email, linkedin, tags, updatedActions, starred, interactions);
    }

    /**
     * Returns the interaction history in date order, oldest first, as an unmodifiable list.
     */
    public List<Interaction> getInteractions() {
        return interactions;
    }

    /**
     * Returns a copy of this person with {@code interactions} as the interaction history.
     */
    public Person withInteractions(List<Interaction> interactions) {
        return new Person(name, company, role, phone, email, linkedin, tags, followUps, starred, interactions);
    }

    /**
     * Returns a copy of this person with {@code interaction} added to the interaction history.
     */
    public Person withInteraction(Interaction interaction) {
        requireNonNull(interaction);
        List<Interaction> updatedInteractions = new ArrayList<>(interactions);
        updatedInteractions.add(interaction);
        return withInteractions(updatedInteractions);
    }

    /**
     * Returns true if this person already has an interaction with the same date and note as {@code interaction}.
     */
    public boolean hasInteraction(Interaction interaction) {
        return interactions.stream().anyMatch(existing -> existing.isSameInteraction(interaction));
    }

    /**
     * Returns true if both persons have the same name and both have an email or both do not.
     * This defines a weaker notion of equality between two persons.
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
                && getEmail().orElseThrow().matches(otherPerson.getEmail().orElseThrow().value);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
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
                && followUps.equals(otherPerson.followUps)
                && starred == otherPerson.starred
                && interactions.equals(otherPerson.interactions);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, company, role, phone, email, linkedin, tags, followUps, starred, interactions);
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
                .add("followUps", followUps)
                .add("starred", starred)
                .add("interactions", interactions)
                .toString();
    }

}
