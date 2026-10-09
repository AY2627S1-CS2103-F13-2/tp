package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.StartupLoadResult;

class StartupMessageFormatterTest {

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
