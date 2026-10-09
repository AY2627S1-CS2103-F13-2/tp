package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;

class StartupLoadResultTest {

    private static final Path SOURCE = Path.of("data", "nested", "..", "addressbook.json");
    private static final RecoveryArchive ARCHIVE = new RecoveryArchive(
            Path.of("recovery", "original.json"), Path.of("recovery", "report.txt"));

    @Test
    void constructor_copiesContactsAndNormalizesSource() {
        List<Person> contacts = new ArrayList<>(List.of(ALICE));
        StartupLoadResult result = new StartupLoadResult(contacts, SOURCE,
                StartupLoadResult.Source.STORED, Optional.empty(), 0);
        contacts.clear();

        assertEquals(List.of(ALICE), result.contacts());
        assertThrows(UnsupportedOperationException.class, () -> result.contacts().clear());
        assertEquals(SOURCE.toAbsolutePath().normalize(), result.sourceFile());
    }

    @Test
    void constructor_incompleteRecovery_rejectsResult() {
        assertThrows(IllegalArgumentException.class, () -> new StartupLoadResult(List.of(ALICE), SOURCE,
                StartupLoadResult.Source.RECOVERED, Optional.empty(), 1));
        assertThrows(IllegalArgumentException.class, () -> new StartupLoadResult(List.of(), SOURCE,
                StartupLoadResult.Source.RECOVERED, Optional.of(ARCHIVE), 1));
        for (int count : List.of(-1, 0)) {
            assertThrows(IllegalArgumentException.class, () -> new StartupLoadResult(List.of(ALICE), SOURCE,
                    StartupLoadResult.Source.RECOVERED, Optional.of(ARCHIVE), count));
        }
    }

    @Test
    void constructor_nonRecoveredSourceWithRecoveryDetails_rejectsResult() {
        for (StartupLoadResult.Source source : List.of(
                StartupLoadResult.Source.STORED, StartupLoadResult.Source.SAMPLE)) {
            assertThrows(IllegalArgumentException.class, () -> new StartupLoadResult(List.of(ALICE), SOURCE,
                    source, Optional.of(ARCHIVE), 0));
            for (int count : List.of(-1, 1)) {
                assertThrows(IllegalArgumentException.class, () -> new StartupLoadResult(List.of(ALICE), SOURCE,
                        source, Optional.empty(), count));
            }
        }
    }

    @Test
    void constructor_nullComponents_rejectsResult() {
        assertThrows(NullPointerException.class, () -> new StartupLoadResult(null, SOURCE,
                StartupLoadResult.Source.STORED, Optional.empty(), 0));
        assertThrows(NullPointerException.class, () -> new StartupLoadResult(List.of(), null,
                StartupLoadResult.Source.STORED, Optional.empty(), 0));
        assertThrows(NullPointerException.class, () -> new StartupLoadResult(List.of(), SOURCE,
                null, Optional.empty(), 0));
        assertThrows(NullPointerException.class, () -> new StartupLoadResult(List.of(), SOURCE,
                StartupLoadResult.Source.STORED, null, 0));
    }
}
