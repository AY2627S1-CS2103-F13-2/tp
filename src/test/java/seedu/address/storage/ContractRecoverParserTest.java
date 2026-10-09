package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

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
                "{",
                "null",
                "{}",
                "{\"persons\": null}",
                "{\"persons\": {}}",
                "{\"persons\": []} {}",
                "{\"persons\": [], \"persons\": []}")) {
            assertThrows(IOException.class, () -> parse(json));
        }
    }

    private ContactRecoveryResult parse(String json) throws IOException {
        return parser.parse(json.getBytes(StandardCharsets.UTF_8));
    }
}
