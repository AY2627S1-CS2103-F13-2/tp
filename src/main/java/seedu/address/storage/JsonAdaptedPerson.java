package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_DUPLICATE_INTERACTION = "Person has the same interaction twice (%1$s).";

    private final String name;
    private final String company;
    private final String role;
    private final String phone;
    private final String email;
    private final String linkedin;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final boolean starred;
    private final List<JsonAdaptedFollowUp> followUps = new ArrayList<>();
    private final List<JsonAdaptedInteraction> interactions = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            List<JsonAdaptedTag> tags) {
        this(name, company, role, phone, email, linkedin, tags, null);
    }

    /**
     * Creates an adapted contact, defaulting absent action history to an empty list for older files.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            List<JsonAdaptedTag> tags, List<JsonAdaptedFollowUp> followUps) {
        this(name, company, role, phone, email, linkedin, tags, followUps, false);
    }

    /**
     * Creates an adapted contact with starred status and no action history.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            List<JsonAdaptedTag> tags, boolean starred) {
        this(name, company, role, phone, email, linkedin, tags, null, starred);
    }

    /**
     * Creates an adapted contact with starred status, action history and interactions.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            List<JsonAdaptedTag> tags, List<JsonAdaptedFollowUp> followUps, boolean starred) {
        this(name, company, role, phone, email, linkedin, tags, followUps, starred, null);
    }

    /**
     * Reads every field; absent fields retain legacy defaults. A contact file written before
     * interactions were recorded has no {@code interactions} field, which is read as an empty history.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("company") String company,
            @JsonProperty("role") String role, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("linkedin") String linkedin,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("followUps") List<JsonAdaptedFollowUp> followUps,
            @JsonProperty("starred") boolean starred,
            @JsonProperty("interactions") List<JsonAdaptedInteraction> interactions) {
        this.name = name;
        this.company = company;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.linkedin = linkedin;
        this.starred = starred;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (followUps != null) {
            this.followUps.addAll(followUps);
        }
        if (interactions != null) {
            this.interactions.addAll(interactions);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        company = source.getCompany().map(value -> value.value).orElse(null);
        role = source.getRole().map(value -> value.value).orElse(null);
        phone = source.getPhone().map(value -> value.value).orElse(null);
        email = source.getEmail().map(value -> value.value).orElse(null);
        linkedin = source.getLinkedin().map(value -> value.value).orElse(null);
        starred = source.isStarred();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        source.getFollowUps().stream().map(JsonAdaptedFollowUp::new).forEach(followUps::add);
        interactions.addAll(source.getInteractions().stream()
                .map(JsonAdaptedInteraction::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     * Every field other than the name is optional, so a missing value is read back as an absent field
     * rather than rejected.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        final Company modelCompany;
        if (company == null) {
            modelCompany = null;
        } else if (!Company.isValidCompany(company)) {
            throw new IllegalValueException(Company.MESSAGE_CONSTRAINTS);
        } else {
            modelCompany = new Company(company);
        }

        final Role modelRole;
        if (role == null) {
            modelRole = null;
        } else if (!Role.isValidRole(role)) {
            throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
        } else {
            modelRole = new Role(role);
        }

        final Phone modelPhone;
        if (phone == null) {
            modelPhone = null;
        } else if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        } else {
            modelPhone = new Phone(phone);
        }

        final Email modelEmail;
        if (email == null) {
            modelEmail = null;
        } else if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        } else {
            modelEmail = new Email(email);
        }

        final Linkedin modelLinkedin;
        if (linkedin == null) {
            modelLinkedin = null;
        } else if (!Linkedin.isValidLinkedin(linkedin)) {
            throw new IllegalValueException(Linkedin.MESSAGE_CONSTRAINTS);
        } else {
            modelLinkedin = new Linkedin(linkedin);
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);
        List<FollowUp> modelFollowUps = new ArrayList<>();
        Set<Integer> actionIds = new HashSet<>();
        for (JsonAdaptedFollowUp action : followUps) {
            if (action == null) {
                throw new IllegalValueException("Follow-up entries must not be null.");
            }
            FollowUp modelAction = action.toModelType();
            if (!actionIds.add(modelAction.getId())) {
                throw new IllegalValueException("Follow-up IDs must be unique within a contact.");
            }
            modelFollowUps.add(modelAction);
        }

        List<Interaction> modelInteractions = new ArrayList<>();
        for (JsonAdaptedInteraction interaction : interactions) {
            if (interaction == null) {
                throw new IllegalValueException("Interaction entries must not be null.");
            }
            Interaction modelInteraction = interaction.toModelType();
            if (modelInteractions.stream().anyMatch(modelInteraction::isSameInteraction)) {
                throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_INTERACTION, modelInteraction));
            }
            modelInteractions.add(modelInteraction);
        }

        return new Person(modelName, modelCompany, modelRole, modelPhone, modelEmail, modelLinkedin,
                modelTags, modelFollowUps, starred, modelInteractions);
    }

}
