package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;

import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_COMPANY = "Acme Labs";
    public static final String DEFAULT_ROLE = "Software Engineer";
    public static final String DEFAULT_LINKEDIN = "linkedin.com/in/amybee";

    private Name name;
    private Company company;
    private Role role;
    private Phone phone;
    private Email email;
    private Linkedin linkedin;
    private Set<Tag> tags;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        company = new Company(DEFAULT_COMPANY);
        role = new Role(DEFAULT_ROLE);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        linkedin = new Linkedin(DEFAULT_LINKEDIN);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        company = personToCopy.getCompany().orElse(null);
        role = personToCopy.getRole().orElse(null);
        phone = personToCopy.getPhone().orElse(null);
        email = personToCopy.getEmail().orElse(null);
        linkedin = personToCopy.getLinkedin().orElse(null);
        tags = new HashSet<>(personToCopy.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Company} of the {@code Person} that we are building.
     * A null {@code company} builds a person without a company.
     */
    public PersonBuilder withCompany(String company) {
        this.company = (company == null) ? null : new Company(company);
        return this;
    }

    /**
     * Sets the {@code Role} of the {@code Person} that we are building.
     * A null {@code role} builds a person without a role.
     */
    public PersonBuilder withRole(String role) {
        this.role = (role == null) ? null : new Role(role);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     * A null {@code phone} builds a person without a phone.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = (phone == null) ? null : new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     * A null {@code email} builds a person without an email.
     */
    public PersonBuilder withEmail(String email) {
        this.email = (email == null) ? null : new Email(email);
        return this;
    }

    /**
     * Sets the {@code Linkedin} of the {@code Person} that we are building.
     * A null {@code linkedin} builds a person without a LinkedIn profile.
     */
    public PersonBuilder withLinkedin(String linkedin) {
        this.linkedin = (linkedin == null) ? null : new Linkedin(linkedin);
        return this;
    }

    public Person build() {
        return new Person(name, company, role, phone, email, linkedin, tags);
    }

}
