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

    public List<FollowUp> getFollowUps() {
        return followUps;
    }

    /**
     * Returns a copy with the action appended, retaining all existing fields and action order.
     */
    public Person appendFollowUp(FollowUp action) {
        List<FollowUp> updatedActions = new ArrayList<>(followUps);
        updatedActions.add(requireNonNull(action));
        return new Person(name, company, role, phone, email, linkedin, tags, updatedActions);
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
                && followUps.equals(otherPerson.followUps);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, company, role, phone, email, linkedin, tags, followUps);
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
                .toString();
    }

}
