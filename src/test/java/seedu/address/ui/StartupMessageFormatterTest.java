package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.RecoveryArchive;
import seedu.address.storage.StartupLoadResult;

class StartupMessageFormatterTest {

    @Test
    void formatLoadedContacts_singularAndPlural_showsNormalizedAbsolutePath() {
        Path source = Path.of("data", "nested", "..", "addressbook.json");
        Path absolute = source.toAbsolutePath().normalize();

        assertEquals("Loaded 0 contacts from " + absolute + ".",
                StartupMessageFormatter.formatLoadedContacts(0, source));
        assertEquals("Loaded 1 contact from " + absolute + ".",
                StartupMessageFormatter.formatLoadedContacts(1, source));
        assertEquals("Loaded 2 contacts from " + absolute + ".",
                StartupMessageFormatter.formatLoadedContacts(2, source));
    }

    @Test
    void formatStartup_storedContacts_showsCountAndSource() {
        StartupLoadResult result = new StartupLoadResult(List.of(ALICE, BOB),
                Path.of("data", "addressbook.json"), StartupLoadResult.Source.STORED, Optional.empty(), 0);

        assertEquals("Loaded 2 contacts from " + result.sourceFile() + ".",
                StartupMessageFormatter.formatStartup(result));
    }

    @Test
    void formatStartup_recoveredContacts_showsCountsAndAllRecoveryLocations() {
        Path source = Path.of("data", "addressbook.json");
        RecoveryArchive archive = new RecoveryArchive(
                Path.of("data", "recovery", "incident", "addressbook.original.json"),
                Path.of("data", "recovery", "incident", "report.txt"));
        StartupLoadResult result = new StartupLoadResult(List.of(ALICE, BOB), source,
                StartupLoadResult.Source.RECOVERED, Optional.of(archive), 3);

        String expected = String.format("Recovered 2 contact(s); skipped 3 invalid or duplicate record(s).%n"
                + "Active file: %s%nOriginal backup: %s%nRecovery report: %s",
                result.sourceFile(), archive.backupFile(), archive.reportFile());
        assertEquals(expected, StartupMessageFormatter.formatStartup(result));
    }

    @Test
    void formatSampleContacts_explainsSamplesAndClearCommand() {
        String message = StartupMessageFormatter.formatSampleContacts();

        assertTrue(message.contains("sample contacts"));
        assertTrue(message.contains("Type " + ClearCommand.COMMAND_WORD));
        assertTrue(message.contains("empty contact list"));
    }

    @Test
    void formatStartup_sampleSource_showsSampleGuidance() {
        StartupLoadResult result = new StartupLoadResult(
                SampleDataUtil.getSampleAddressBook().getPersonList(),
                Path.of("data", "addressbook.json"),
                StartupLoadResult.Source.SAMPLE,
                Optional.empty(),
                0);

        String message = StartupMessageFormatter.formatStartup(result);

        assertTrue(message.contains("sample contacts"));
        assertTrue(message.contains("Type " + ClearCommand.COMMAND_WORD));
    }

    @Test
    void formatStartup_emptyStoredFile_doesNotShowSampleGuidance() {
        StartupLoadResult result = new StartupLoadResult(
                List.of(),
                Path.of("data", "addressbook.json"),
                StartupLoadResult.Source.STORED,
                Optional.empty(),
                0);

        String message = StartupMessageFormatter.formatStartup(result);

        assertTrue(message.contains("Loaded 0 contacts"));
        assertFalse(message.contains("sample contacts"));
    }
}
