package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

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
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_COMPANY = " ";
    private static final String INVALID_ROLE = " ";
    private static final String INVALID_PHONE = "9011p041";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LINKEDIN = "linkedin.com/in/two words";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_COMPANY = BENSON.getCompany().orElseThrow().toString();
    private static final String VALID_ROLE = BENSON.getRole().orElseThrow().toString();
    private static final String VALID_PHONE = BENSON.getPhone().orElseThrow().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().orElseThrow().toString();
    private static final String VALID_LINKEDIN = BENSON.getLinkedin().orElseThrow().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_legacyConstructor_defaultsMetadata() throws Exception {
        Person person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE, VALID_EMAIL,
                VALID_LINKEDIN, VALID_TAGS).toModelType();

        assertEquals(new Name(VALID_NAME), person.getName());
        assertEquals(Optional.empty(), person.getIntroducer());
        assertEquals(List.of(), person.getFollowUps());
        assertFalse(person.isStarred());
    }

    @Test
    public void toModelType_constructorWithFollowUps_preservesFollowUps() throws Exception {
        FollowUp followUp = new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING);
        JsonAdaptedPerson adaptedPerson = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS, List.of(new JsonAdaptedFollowUp(followUp)));

        assertEquals(List.of(followUp), adaptedPerson.toModelType().getFollowUps());
    }

    @Test
    public void toModelType_constructorWithStarredStatus_preservesStarredStatus() throws Exception {
        JsonAdaptedPerson adaptedPerson = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS, true);

        assertTrue(adaptedPerson.toModelType().isStarred());
        assertEquals(List.of(), adaptedPerson.toModelType().getFollowUps());
    }

    @Test
    public void toModelType_starredPerson_preservesStarredStatus() throws Exception {
        Person starredPerson = new PersonBuilder(BENSON).withStarred(true).build();
        assertEquals(starredPerson, new JsonAdaptedPerson(starredPerson).toModelType());
    }

    @Test
    public void toModelType_nameOnlyPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(NAME_ONLY);
        assertEquals(NAME_ONLY, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(INVALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidCompany_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, INVALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Company.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, INVALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Role.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, INVALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                INVALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidLinkedin_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, INVALID_LINKEDIN, null, VALID_TAGS);
        String expectedMessage = Linkedin.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullOptionalFields_returnsPersonWithoutThem() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null, null, VALID_TAGS);
        assertEquals(new Name(VALID_NAME), person.toModelType().getName());
        assertEquals(java.util.Optional.empty(), person.toModelType().getCompany());
        assertEquals(java.util.Optional.empty(), person.toModelType().getRole());
        assertEquals(java.util.Optional.empty(), person.toModelType().getPhone());
        assertEquals(java.util.Optional.empty(), person.toModelType().getEmail());
        assertEquals(java.util.Optional.empty(), person.toModelType().getLinkedin());
        assertEquals(java.util.Optional.empty(), person.toModelType().getIntroducer());
    }

    @Test
    public void toModelType_introducerDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                new seedu.address.testutil.PersonBuilder(NAME_ONLY).withIntroducer("Jane Doe").build());
        assertEquals("Jane Doe", person.toModelType().getIntroducer().orElseThrow().toString());
    }

    @Test
    public void toModelType_invalidIntroducer_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null,
                "Jane& Doe", VALID_TAGS);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_personWithInteractions_keepsInteractions() throws Exception {
        Person person = BENSON.withInteraction(new Interaction(LocalDate.of(2026, 10, 15), "Coffee chat"))
                .withInteraction(new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat"));
        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void toModelType_invalidInteraction_throwsIllegalValueException() {
        JsonAdaptedPerson person = withInteractions(new JsonAdaptedInteraction("not a date", "Coffee chat"));
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_DATE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_futureInteraction_throwsIllegalValueException() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        JsonAdaptedPerson person = withInteractions(new JsonAdaptedInteraction(tomorrow.toString(), "Coffee chat"));
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedInteraction.MESSAGE_FUTURE_DATE, tomorrow), person::toModelType);
    }

    @Test
    public void toModelType_duplicateInteractions_throwsIllegalValueException() {
        JsonAdaptedPerson person = withInteractions(new JsonAdaptedInteraction("2026-10-15", "Coffee chat"),
                new JsonAdaptedInteraction("2026-10-15", "coffee CHAT"));
        assertThrows(IllegalValueException.class, String.format(JsonAdaptedPerson.MESSAGE_DUPLICATE_INTERACTION,
                new Interaction(LocalDate.of(2026, 10, 15), "coffee CHAT")), person::toModelType);
    }

    @Test
    public void toModelType_nullInteraction_throwsIllegalValueException() {
        List<JsonAdaptedInteraction> interactions = new ArrayList<>();
        interactions.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS, null, null, false, interactions);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    private static JsonAdaptedPerson withInteractions(JsonAdaptedInteraction... interactions) {
        return new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, null, VALID_TAGS, null, null, false, List.of(interactions));
    }

    @Test
    public void toModelType_withEvents_returnsEvents() throws Exception {
        List<JsonAdaptedEvent> events = List.of(
                new JsonAdaptedEvent("NUS Career Fair", "2026-10-20"));

        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME,
                VALID_COMPANY,
                VALID_ROLE,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_LINKEDIN,
                VALID_TAGS,
                events,
                null,
                false);

        assertEquals(1, person.toModelType().getEvents().size());
        assertEquals(
                "NUS Career Fair",
                person.toModelType().getEvents().iterator().next().getName());
    }

    @Test
    public void toModelType_nullEvents_returnsPersonWithoutEvents() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME,
                VALID_COMPANY,
                VALID_ROLE,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_LINKEDIN,
                VALID_TAGS,
                null,
                null,
                false);

        assertEquals(0, person.toModelType().getEvents().size());
    }
}
