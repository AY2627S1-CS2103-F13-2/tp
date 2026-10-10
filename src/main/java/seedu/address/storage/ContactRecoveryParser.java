package seedu.address.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Recovers valid contacts from a structurally valid contact document.
 */
final class ContactRecoveryParser {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

    /**
     * Validates individual records while preserving their original order.
     *
     * @throws IOException if the document cannot be parsed reliably
     */
    public ContactRecoveryResult parse(byte[] originalBytes) throws IOException {
        JsonNode root = readDocument(originalBytes);

        if (root == null || !root.isObject()
                || !root.has("persons") || !root.get("persons").isArray()) {
            throw new IOException(
                    "The contact file must contain an object with a persons array.");
        }

        List<Person> recovered = new ArrayList<>();
        List<Integer> originalNumbers = new ArrayList<>();
        List<SkippedContact> skipped = new ArrayList<>();
        JsonNode records = root.get("persons");

        for (int index = 0; index < records.size(); index++) {
            recoverRecord(records.get(index), index + 1,
                    recovered, originalNumbers, skipped);
        }

        return new ContactRecoveryResult(recovered, skipped);
    }

    private void recoverRecord(JsonNode record, int recordNumber,
                               List<Person> recovered, List<Integer> originalNumbers,
                               List<SkippedContact> skipped) {
        List<String> errors = validateRecord(record);

        if (!errors.isEmpty()) {
            skipped.add(new SkippedContact(recordNumber, String.join("; ", errors)));
            return;
        }

        try {
            Person person = JsonUtil.fromJsonString(
                    record.toString(), JsonAdaptedPerson.class).toModelType();

            int conflictingRecord = findConflictingRecord(
                    person, recovered, originalNumbers);

            if (conflictingRecord != 0) {
                skipped.add(new SkippedContact(recordNumber,
                        "Duplicate contact; conflicts with retained record "
                                + conflictingRecord + "."));
                return;
            }

            recovered.add(person);
            originalNumbers.add(recordNumber);
        } catch (IOException | IllegalValueException e) {
            skipped.add(new SkippedContact(recordNumber,
                    "Could not convert record: " + e.getMessage()));
        }
    }

    private int findConflictingRecord(Person person, List<Person> recovered,
                                      List<Integer> originalNumbers) {
        for (int index = 0; index < recovered.size(); index++) {
            Person retained = recovered.get(index);
            if (person.isSamePerson(retained) || hasSamePhone(person, retained) || hasSameEmail(person, retained)) {
                return originalNumbers.get(index);
            }
        }

        return 0;
    }

    private boolean hasSamePhone(Person first, Person second) {
        return first.getPhone().isPresent() && second.getPhone().isPresent()
                && first.getPhone().orElseThrow().value.replaceAll("\\P{Nd}", "")
                        .equals(second.getPhone().orElseThrow().value.replaceAll("\\P{Nd}", ""));
    }

    private boolean hasSameEmail(Person first, Person second) {
        return first.getEmail().isPresent() && second.getEmail().isPresent()
                && first.getEmail().orElseThrow().value.equalsIgnoreCase(second.getEmail().orElseThrow().value);
    }

    private List<String> validateRecord(JsonNode record) {
        List<String> errors = new ArrayList<>();

        if (record == null || !record.isObject()) {
            errors.add("Contact record must be a JSON object.");
            return errors;
        }

        validateText(record, "name", true,
                Name::isValidName, Name.MESSAGE_CONSTRAINTS, errors);
        validateText(record, "company", false,
                Company::isValidCompany, Company.MESSAGE_CONSTRAINTS, errors);
        validateText(record, "role", false,
                Role::isValidRole, Role.MESSAGE_CONSTRAINTS, errors);
        validateText(record, "phone", false,
                Phone::isValidPhone, Phone.MESSAGE_CONSTRAINTS, errors);
        validateText(record, "email", false,
                Email::isValidEmail, Email.MESSAGE_CONSTRAINTS, errors);
        validateText(record, "linkedin", false,
                Linkedin::isValidLinkedin, Linkedin.MESSAGE_CONSTRAINTS, errors);

        validateTags(record.get("tags"), errors);
        return errors;
    }

    private void validateText(JsonNode record, String field, boolean required,
                              Predicate<String> validator, String constraints,
                              List<String> errors) {
        JsonNode value = record.get(field);

        if (value == null || value.isNull()) {
            if (required) {
                errors.add(field + ": field is missing.");
            }
            return;
        }

        if (!value.isTextual()) {
            errors.add(field + ": must be a string.");
        } else if (!validator.test(value.textValue())) {
            errors.add(field + ": " + constraints);
        }
    }

    private void validateTags(JsonNode tags, List<String> errors) {
        if (tags == null || tags.isNull()) {
            return;
        }

        if (!tags.isArray()) {
            errors.add("tags: must be an array.");
            return;
        }

        for (int index = 0; index < tags.size(); index++) {
            JsonNode tag = tags.get(index);
            String label = "tags[" + (index + 1) + "]: ";

            if (!tag.isTextual()) {
                errors.add(label + "must be a string.");
            } else if (!Tag.isValidTagName(tag.textValue())) {
                errors.add(label + Tag.MESSAGE_CONSTRAINTS);
            }
        }
    }

    /**
     * Reads exactly one JSON document, rejecting trailing content.
     */
    private JsonNode readDocument(byte[] originalBytes) throws IOException {
        try (JsonParser jsonParser = MAPPER.getFactory().createParser(originalBytes)) {
            JsonNode root = MAPPER.readTree(jsonParser);

            if (jsonParser.nextToken() != null) {
                throw new IOException(
                        "The contact file contains extra content after the JSON document.");
            }

            return root;
        }
    }
}
