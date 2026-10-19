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
 * This optionality applies to contact details; starred status is a boolean flag that defaults to false.
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
    private final boolean starred;

    /**
     * Constructs a {@code Person}. Only {@code name} and {@code tags} are required; a networking
     * contact is often captured with just a name and whatever details were exchanged, so
     * {@code company}, {@code role}, {@code phone}, {@code email} and {@code linkedin} may be null.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags) {
        this(name, company, role, phone, email, linkedin, tags, false);
    }

    /**
     * Constructs a {@code Person} with the given details and starred status.
     * Only {@code name} and {@code tags} are required; optional contact details may be null.
     */
    public Person(Name name, Company company, Role role, Phone phone, Email email, Linkedin linkedin,
            Set<Tag> tags, boolean starred) {
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
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
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
                && starred == otherPerson.starred;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, company, role, phone, email, linkedin, tags, starred);
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
                .add("starred", starred)
                .toString();
    }

}
