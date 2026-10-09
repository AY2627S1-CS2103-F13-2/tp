package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class SkippedContactTest {

    @Test
    void constructor_nonPositivePosition_rejectsRecord() {
        assertThrows(IllegalArgumentException.class, () -> new SkippedContact(0, "Missing name."));
        assertThrows(IllegalArgumentException.class, () -> new SkippedContact(-1, "Missing name."));
    }

    @Test
    void constructor_missingReason_rejectsRecord() {
        assertThrows(NullPointerException.class, () -> new SkippedContact(1, null));
        for (String reason : List.of("", " ", "\r\n\t")) {
            assertThrows(IllegalArgumentException.class, () -> new SkippedContact(1, reason));
        }
    }

    @Test
    void constructor_validRecord_preservesDiagnostic() {
        SkippedContact record = new SkippedContact(3, "name: missing; phone: invalid");

        assertEquals(3, record.recordNumber());
        assertEquals("name: missing; phone: invalid", record.reason());
    }
}
