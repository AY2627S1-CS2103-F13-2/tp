package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

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
    public void toModelType_nameOnlyPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(NAME_ONLY);
        assertEquals(NAME_ONLY, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(INVALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidCompany_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, INVALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Company.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, INVALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Role.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, INVALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                INVALID_EMAIL, VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidLinkedin_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, INVALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Linkedin.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullOptionalFields_returnsPersonWithoutThem() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, null, null, null, null, VALID_TAGS);
        assertEquals(new Name(VALID_NAME), person.toModelType().getName());
        assertEquals(java.util.Optional.empty(), person.toModelType().getCompany());
        assertEquals(java.util.Optional.empty(), person.toModelType().getRole());
        assertEquals(java.util.Optional.empty(), person.toModelType().getPhone());
        assertEquals(java.util.Optional.empty(), person.toModelType().getEmail());
        assertEquals(java.util.Optional.empty(), person.toModelType().getLinkedin());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_COMPANY, VALID_ROLE, VALID_PHONE,
                VALID_EMAIL, VALID_LINKEDIN, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
