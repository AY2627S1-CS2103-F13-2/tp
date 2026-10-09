package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

class ContactRecoveryParserTest {

    private final ContactRecoveryParser parser = new ContactRecoveryParser();

    @Test
    void parse_mixedRecords_preservesOrderAndOriginalPositions() throws Exception {
        String json = """
                {
                  "persons": [
                    {"name": "Charlie"},
                    {"name": "Broken", "phone": "invalid", "email": "invalid"},
                    {"name": "Alice"},
                    {"name": "Charlie"},
                    null,
                    {"name": "Bob"}
                  ]
                }
                """;

        ContactRecoveryResult result = parse(json);

        assertEquals(List.of("Charlie", "Alice", "Bob"),
                result.recoveredContacts().stream()
                        .map(person -> person.getName().fullName)
                        .toList());

        assertEquals(List.of(2, 4, 5),
                result.skippedContacts().stream()
                        .map(SkippedContact::recordNumber)
                        .toList());

        assertTrue(result.skippedContacts().get(0).reason().contains("phone"));
        assertTrue(result.skippedContacts().get(0).reason().contains("email"));
        assertTrue(result.skippedContacts().get(1).reason().contains("record 1"));
    }

    @Test
    void parse_invalidEarlierOccurrence_keepsLaterValidContact() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [
                  {"name": "Alice", "phone": "invalid"},
                  {"name": "Alice"}
                ]}
                """);

        assertEquals(1, result.getRecoveredCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.skippedContacts().get(0).recordNumber());
    }

    @Test
    void parse_wrongFieldTypesAndNullTags_skipsAffectedRecords() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [
                  {"name": "Alice", "phone": {"value": "12345"}},
                  {"name": "Bob", "tags": [null]},
                  {"name": "Charlie"}
                ]}
                """);

        assertEquals(1, result.getRecoveredCount());
        assertEquals(2, result.getSkippedCount());
        assertEquals("Charlie", result.recoveredContacts().get(0).getName().fullName);
    }

    @Test
    void parse_emptyContactList_isValid() throws Exception {
        ContactRecoveryResult result = parse("{\"persons\": []}");

        assertEquals(0, result.getRecoveredCount());
        assertEquals(0, result.getSkippedCount());
    }

    @Test
    void parse_invalidDocument_throwsException() {
        for (String json : List.of(
                "",
                " \r\n\t ",
                "{",
                "null",
                "[]",
                "42",
                "true",
                "\"persons\"",
                "{}",
                "{\"persons\": null}",
                "{\"persons\": {}}",
                "{\"persons\": \"[]\"}",
                "{\"persons\": []} {}",
                "{\"persons\": []} trailing garbage",
                "{\"persons\": [], \"persons\": []}",
                "{\"persons\": [{\"name\": \"Alice\", \"name\": \"Bob\"}]}")) {
            assertThrows(IOException.class, () -> parse(json), json);
        }
    }

    @Test
    void parse_nonObjectRecords_skipsEachAndContinues() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [null, true, 42, "Alice", [], {"name": "Bob"}]}
                """);

        assertEquals(1, result.getRecoveredCount());
        assertEquals("Bob", result.recoveredContacts().get(0).getName().fullName);
        assertEquals(List.of(1, 2, 3, 4, 5), result.skippedContacts().stream()
                .map(SkippedContact::recordNumber).toList());
        assertTrue(result.skippedContacts().stream()
                .allMatch(record -> record.reason().contains("must be a JSON object")));
    }

    @Test
    void parse_missingNullAndBlankNames_skipsRecords() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [{}, {"name": null}, {"name": ""}, {"name": "   "}]}
                """);

        assertEquals(0, result.getRecoveredCount());
        assertEquals(4, result.getSkippedCount());
        assertTrue(result.skippedContacts().get(0).reason().contains("name: field is missing"));
        assertTrue(result.skippedContacts().get(1).reason().contains("name: field is missing"));
        assertTrue(result.skippedContacts().get(2).reason().contains("name:"));
        assertTrue(result.skippedContacts().get(3).reason().contains("name:"));
    }

    @Test
    void parse_nonStringTextFields_skipsInsteadOfCoercingValues() throws Exception {
        for (String field : List.of("name", "company", "role", "phone", "email", "linkedin")) {
            for (String value : List.of("12345", "true", "[]", "{}")) {
                String fields = field.equals("name") ? "" : "\"name\": \"Alice\", ";
                ContactRecoveryResult result = parse("{\"persons\": [{" + fields
                        + "\"" + field + "\": " + value + "}]}");

                assertEquals(0, result.getRecoveredCount(), field + ": " + value);
                assertEquals(List.of(new SkippedContact(1, field + ": must be a string.")),
                        result.skippedContacts());
            }
        }
    }

    @Test
    void parse_nullOptionalFields_treatsThemAsAbsent() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [{"name": "Alice", "company": null, "role": null,
                  "phone": null, "email": null, "linkedin": null, "tags": null}]}
                """);

        Person person = result.recoveredContacts().get(0);
        assertEquals(0, result.getSkippedCount());
        assertTrue(person.getCompany().isEmpty());
        assertTrue(person.getRole().isEmpty());
        assertTrue(person.getPhone().isEmpty());
        assertTrue(person.getEmail().isEmpty());
        assertTrue(person.getLinkedin().isEmpty());
        assertTrue(person.getTags().isEmpty());
    }

    @Test
    void parse_blankOptionalFields_reportsEveryInvalidField() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [{"name": "Alice", "company": "", "role": "",
                  "phone": "", "email": "", "linkedin": ""}]}
                """);

        assertEquals(0, result.getRecoveredCount());
        assertEquals(1, result.getSkippedCount());
        for (String field : List.of("company", "role", "phone", "email", "linkedin")) {
            assertTrue(result.skippedContacts().get(0).reason().contains(field + ":"), field);
        }
    }

    @Test
    void parse_nonArrayTags_skipsRecord() throws Exception {
        for (String tags : List.of("\"friends\"", "{}", "123", "true")) {
            ContactRecoveryResult result = parse("{\"persons\": [{\"name\": \"Alice\", \"tags\": " + tags + "}]}");

            assertEquals(0, result.getRecoveredCount());
            assertEquals(List.of(new SkippedContact(1, "tags: must be an array.")), result.skippedContacts());
        }
    }

    @Test
    void parse_invalidTags_reportsOriginalTagPositions() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [{"name": "Alice", "tags": ["friends", "", null, 3, {}, [], "bad!"]}]}
                """);

        assertEquals(0, result.getRecoveredCount());
        assertEquals(1, result.getSkippedCount());
        String reason = result.skippedContacts().get(0).reason();
        assertFalse(reason.contains("tags[1]"));
        for (int index = 2; index <= 7; index++) {
            assertTrue(reason.contains("tags[" + index + "]:"));
        }
    }

    @Test
    void parse_validFieldsAndLegacyData_preservesAllSupportedDetails() throws Exception {
        ContactRecoveryResult result = parse("""
                {"legacy": true, "persons": [{"name": "  Jos\u00e9   Tan  ", "company": "Acme",
                  "role": "Engineer", "phone": "91234567", "email": "jose@example.com",
                  "linkedin": "https://www.linkedin.com/in/jose-tan",
                  "tags": ["friends", "work", "friends"], "address": "Legacy address"}]}
                """);

        Person expected = new PersonBuilder().withName("Jos\u00e9 Tan").withCompany("Acme").withRole("Engineer")
                .withPhone("91234567").withEmail("jose@example.com")
                .withLinkedin("https://www.linkedin.com/in/jose-tan").withTags("friends", "work").build();
        assertEquals(List.of(expected), result.recoveredContacts());
        assertEquals(Set.of(new Tag("friends"), new Tag("work")), result.recoveredContacts().get(0).getTags());
        assertEquals(0, result.getSkippedCount());
    }

    @Test
    void parse_normalizedDuplicate_keepsFirstValidDetailsAndOriginalRecordNumber() throws Exception {
        ContactRecoveryResult result = parse("""
                {"persons": [
                  {"name": "Alice Tan", "phone": "invalid"},
                  {"name": "  Alice   Tan ", "phone": "91234567"},
                  {"name": "Bob"},
                  {"name": "Alice Tan", "phone": "98765432"}
                ]}
                """);

        assertEquals(List.of("Alice Tan", "Bob"), result.recoveredContacts().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals("91234567", result.recoveredContacts().get(0).getPhone().orElseThrow().value);
        assertEquals(List.of(1, 4), result.skippedContacts().stream().map(SkippedContact::recordNumber).toList());
        assertEquals("Duplicate contact; conflicts with retained record 2.", result.skippedContacts().get(1).reason());
    }

    private ContactRecoveryResult parse(String json) throws IOException {
        return parser.parse(json.getBytes(StandardCharsets.UTF_8));
    }
}
