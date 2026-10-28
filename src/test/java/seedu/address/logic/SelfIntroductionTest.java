package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.IntroduceCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises all user-facing paths that could record a contact as their own introducer.
 */
public class SelfIntroductionTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private LogicManager logic;
    private Path contactPath;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        contactPath = directory.resolve("contacts.json");
        logic = new LogicManager(model, new StorageManager(new JsonContactFileStorage(contactPath),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void execute_addSelfIntroducer_rejectedWithoutSaving() {
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("add n/Alice Tan i/aLiCe   tAn"));

        assertEquals(IntroduceCommand.MESSAGE_SELF_INTRODUCTION, error.getMessage());
        assertEquals(0, model.getContactFile().getPersonList().size());
        assertFalse(Files.exists(contactPath));
    }

    @Test
    public void execute_introduceSelfByIndexOrName_rejectedWithoutPromptOrSaving() throws Exception {
        logic.execute("add n/Alice Tan i/Jane Smith");
        ContactFile before = new ContactFile(model.getContactFile());
        String savedBefore = Files.readString(contactPath);

        for (String selector : new String[] {"1", "Alice Tan"}) {
            var result = logic.execute("introduce " + selector + " i/aLiCe   tAn");
            assertEquals(IntroduceCommand.MESSAGE_SELF_INTRODUCTION, result.getFeedbackToUser());
            assertFalse(result.getPendingRequest().isPresent());
            assertEquals(before, model.getContactFile());
            assertEquals(savedBefore, Files.readString(contactPath));
        }
    }

    @Test
    public void execute_selfIntroducerAfterSelectingContact_rejected() throws Exception {
        logic.execute("add n/Alice Tan");
        logic.execute("add n/Alice Lim");
        ContactFile before = new ContactFile(model.getContactFile());

        logic.execute("introduce Alice i/Alice Tan");
        var result = logic.execute("1");

        assertEquals(IntroduceCommand.MESSAGE_SELF_INTRODUCTION, result.getFeedbackToUser());
        assertFalse(result.getPendingRequest().isPresent());
        assertEquals(before, model.getContactFile());
    }

    @Test
    public void execute_renameToIntroducer_rejectedWithoutSaving() throws Exception {
        logic.execute("add n/Alice Tan i/Jane Smith");
        String savedBefore = Files.readString(contactPath);

        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 n/jAnE   sMiTh"));

        assertEquals(IntroduceCommand.MESSAGE_SELF_INTRODUCTION, error.getMessage());
        assertEquals("Alice Tan", model.getContactFile().getPersonList().getFirst().getName().fullName);
        assertEquals(savedBefore, Files.readString(contactPath));
    }

    @Test
    public void execute_differentIntroducerStillAccepted() throws Exception {
        logic.execute("add n/Alice Tan i/Jane Smith");
        logic.execute("introduce 1 i/Bob Lee");
        logic.execute("y");

        assertEquals("Bob Lee", model.getContactFile().getPersonList().getFirst()
                .getIntroducer().orElseThrow().fullName);
    }
}
