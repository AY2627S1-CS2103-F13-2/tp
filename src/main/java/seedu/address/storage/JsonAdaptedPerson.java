package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;

import seedu.address.commons.exceptions.IllegalValueException;
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
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT =
            "Person's %s field is missing!";
    public static final String MESSAGE_DUPLICATE_INTERACTION = "Person has the same interaction twice (%1$s).";

    private final String name;
    private final String company;
    private final String role;
    private final String phone;
    private final String email;
    private final String linkedin;
    private final String introducer;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedEvent> events = new ArrayList<>();
    private final List<JsonAdaptedFollowUp> followUps = new ArrayList<>();
    private final boolean starred;
    private final JsonNode lastFollowUpId;
    private final List<JsonAdaptedInteraction> interactions = new ArrayList<>();

    /**
     * Constructs a JsonAdaptedPerson without events, follow-ups or starred status.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone,
            String email, String linkedin,
            List<JsonAdaptedTag> tags) {
        this(name, company, role, phone, email, linkedin,
                null, tags, null, null, false);
    }

    /**
     * Creates an adapted contact with an optional introducer and default action metadata.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            String introducer, List<JsonAdaptedTag> tags) {
        this(name, company, role, phone, email, linkedin,
                introducer, tags, null, null, false);
    }

    /**
     * Constructs a JsonAdaptedPerson with follow-ups.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone,
            String email, String linkedin,
            List<JsonAdaptedTag> tags, List<JsonAdaptedFollowUp> followUps) {
        this(name, company, role, phone, email, linkedin,
                null, tags, null, followUps, false);
    }

    /**
     * Constructs a JsonAdaptedPerson with starred status.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone,
            String email, String linkedin,
            List<JsonAdaptedTag> tags, boolean starred) {
        this(name, company, role, phone, email, linkedin,
                null, tags, null, null, starred);
    }

    /**
     * Constructs a JsonAdaptedPerson with every field except interactions.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            String introducer, List<JsonAdaptedTag> tags, List<JsonAdaptedEvent> events,
            List<JsonAdaptedFollowUp> followUps, boolean starred) {
        this(name, company, role, phone, email, linkedin, introducer, tags, events, followUps, starred, null);
    }

    /**
     * Creates an adapted legacy contact whose allocated ID is inferred from its history.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone, String email, String linkedin,
            String introducer, List<JsonAdaptedTag> tags, List<JsonAdaptedEvent> events,
            List<JsonAdaptedFollowUp> followUps, boolean starred, List<JsonAdaptedInteraction> interactions) {
        this(name, company, role, phone, email, linkedin, introducer, tags, events, followUps, starred,
                interactions, null);
    }

    /**
     * Jackson constructor containing all contact data. A contact file written before interactions were
     * recorded has no {@code interactions} field, which is read as an empty history.
     */
    @JsonCreator
    public JsonAdaptedPerson(
            @JsonProperty("name") String name,
            @JsonProperty("company") String company,
            @JsonProperty("role") String role,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("linkedin") String linkedin,
            @JsonProperty("introducer") String introducer,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("events") List<JsonAdaptedEvent> events,
            @JsonProperty("followUps") List<JsonAdaptedFollowUp> followUps,
            @JsonProperty("starred") boolean starred,
            @JsonProperty("interactions") List<JsonAdaptedInteraction> interactions,
            @JsonProperty("lastFollowUpId") JsonNode lastFollowUpId) {
        this.name = name;
        this.company = company;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.linkedin = linkedin;
        this.introducer = introducer;
        this.starred = starred;
        this.lastFollowUpId = lastFollowUpId;

        if (tags != null) {
            this.tags.addAll(tags);
        }

        if (events != null) {
            this.events.addAll(events);
        }

        if (followUps != null) {
            this.followUps.addAll(followUps);
        }

        if (interactions != null) {
            this.interactions.addAll(interactions);
        }
    }

    /**
     * Constructs a JsonAdaptedPerson with events and follow-ups.
     */
    public JsonAdaptedPerson(String name, String company, String role, String phone,
            String email, String linkedin, List<JsonAdaptedTag> tags,
            List<JsonAdaptedEvent> events, List<JsonAdaptedFollowUp> followUps,
            boolean starred) {
        this(name, company, role, phone, email, linkedin,
                null, tags, events, followUps, starred);
    }

    /**
     * Converts a given Person into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        company = source.getCompany().map(value -> value.value).orElse(null);
        role = source.getRole().map(value -> value.value).orElse(null);
        phone = source.getPhone().map(value -> value.value).orElse(null);
        email = source.getEmail().map(value -> value.value).orElse(null);
        linkedin = source.getLinkedin().map(value -> value.value).orElse(null);
        introducer = source.getIntroducer().map(value -> value.fullName).orElse(null);
        starred = source.isStarred();
        lastFollowUpId = IntNode.valueOf(source.getLastFollowUpId());

        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));

        events.addAll(source.getEvents().stream()
                .map(JsonAdaptedEvent::new)
                .collect(Collectors.toList()));

        source.getFollowUps().stream()
                .map(JsonAdaptedFollowUp::new)
                .forEach(followUps::add);

        interactions.addAll(source.getInteractions().stream()
                .map(JsonAdaptedInteraction::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's Person object.
     *
     * @throws IllegalValueException if data constraints are violated
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        final List<Event> personEvents = new ArrayList<>();
        for (JsonAdaptedEvent event : events) {
            personEvents.add(event.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT,
                            Name.class.getSimpleName()));
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

        final Name modelIntroducer;
        if (introducer == null) {
            modelIntroducer = null;
        } else if (!Name.isValidName(introducer)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        } else {
            modelIntroducer = new Name(introducer);
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);
        final Set<Event> modelEventSet = new HashSet<>(personEvents);
        final List<FollowUp> modelFollowUps = new ArrayList<>();
        final Set<Integer> actionIds = new HashSet<>();

        for (JsonAdaptedFollowUp action : followUps) {
            if (action == null) {
                throw new IllegalValueException(
                        "Follow-up entries must not be null.");
            }

            FollowUp modelAction = action.toModelType();

            if (!actionIds.add(modelAction.getId())) {
                throw new IllegalValueException(
                        "Follow-up IDs must be unique within a contact.");
            }

            modelFollowUps.add(modelAction);
        }

        int greatestId = modelFollowUps.stream().mapToInt(FollowUp::getId).max().orElse(0);
        int modelLastFollowUpId = greatestId;
        if (lastFollowUpId != null && !lastFollowUpId.isNull()) {
            if (!lastFollowUpId.isIntegralNumber() || !lastFollowUpId.canConvertToInt()
                    || lastFollowUpId.intValue() < greatestId) {
                throw new IllegalValueException("lastFollowUpId must be an integer at least " + greatestId + ".");
            }
            modelLastFollowUpId = lastFollowUpId.intValue();
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
        return new Person(
                modelName,
                modelCompany,
                modelRole,
                modelPhone,
                modelEmail,
                modelLinkedin,
                modelIntroducer,
                modelTags,
                modelEventSet,
                modelFollowUps,
                starred,
                modelInteractions,
                modelLastFollowUpId);
    }
}
