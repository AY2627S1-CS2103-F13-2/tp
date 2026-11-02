package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.PendingRequest;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.CountingStorage;

class LogicManagerInvariantTest {
    @TempDir
    private Path temporaryFolder;

    private final Model model = new ModelManager();
    private CountingStorage storage;
    private LogicManager logic;

    @BeforeEach
    void setUp() {
        model.addPerson(ALICE);
        storage = new CountingStorage(temporaryFolder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    void execute_pendingReplyMutatesContacts_throwsAssertionError() throws Exception {
        assertInvalidPendingReply(() -> model.addPerson(BOB),
                "Pending requests must not mutate contact data");
    }

    @Test
    void execute_pendingReplyMutatesPreferences_throwsAssertionError() throws Exception {
        assertInvalidPendingReply(() -> model.setCommandAliases(Map.of("ls", "list")),
                "Pending requests must not mutate preferences");
    }

    private void assertInvalidPendingReply(Runnable mutation, String expectedMessage) throws Exception {
        assertTrue(logic.execute("clear").getPendingRequest().isPresent());

        // Inject a faulty reply handler: public commands cannot produce this invalid intermediate state.
        PendingRequest faultyRequest = new PendingRequest() {
            @Override
            public String getPrompt() {
                return "Awaiting confirmation";
            }

            @Override
            public CommandResult respond(String response, Model model) {
                mutation.run();
                return new CommandResult(getPrompt(), this);
            }
        };
        Field requestField = LogicManager.class.getDeclaredField("pendingRequest");
        requestField.setAccessible(true);
        requestField.set(logic, faultyRequest);

        AssertionError error = assertThrows(AssertionError.class, () -> logic.execute("y"));

        assertEquals(expectedMessage, error.getMessage());
        assertEquals(0, storage.getSaveCount());
        assertFalse(Files.exists(temporaryFolder.resolve("contacts.json")));
        assertFalse(Files.exists(temporaryFolder.resolve("preferences.json")));
    }
}
