package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class FollowUpWorkflowTest {
    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private CountingStorage storage;
    private Path file;

    @BeforeEach
    void setUp() throws Exception {
        model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        file = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(file).saveContactFile(model.getContactFile());
        storage = new CountingStorage(file);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    @Test
    void execute_starEditAndUnstar_preservesHistoryAcrossReloadAndStartup() throws Exception {
        Person original = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING),
                new FollowUp(2, "Call", FollowUp.Status.COMPLETED)).build();
        model.setPerson(ALICE, original);
        logic.execute("find Alice");
        logic.execute("star 1");
        assertEquals(original.withStarred(true), model.getFilteredPersonList().getFirst());
        assertEquals(original.withStarred(true), storage.readContactFile().orElseThrow().getPersonList().getFirst());
        logic.execute("edit 1 p/76543210");
        Person edited = new PersonBuilder(original).withStarred(true).withPhone("76543210").build();
        assertEquals(edited, model.getFilteredPersonList().getFirst());
        assertEquals(edited, storage.loadForStartup().contacts().getFirst());
        logic.execute("unstar 1");
        assertEquals(edited.withStarred(false), storage.readContactFile().orElseThrow().getPersonList().getFirst());
        assertEquals(original.getFollowUps(), model.getFilteredPersonList().getFirst().getFollowUps());
        assertEquals(3, storage.saveCount);
    }

    @Test
    void execute_followUpOnStarredContact_retainsStarAndSkipsNoOpSaves() throws Exception {
        logic.execute("star 1");
        logic.execute("followup 1 a/Send portfolio");
        Person updated = model.getFilteredPersonList().getFirst();
        assertTrue(updated.isStarred());
        assertEquals(List.of(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING)), updated.getFollowUps());
        assertEquals(updated, storage.loadForStartup().contacts().getFirst());
        byte[] saved = Files.readAllBytes(file);
        logic.execute("star 1");
        logic.execute("view 1");
        assertEquals(2, storage.saveCount);
        assertArrayEquals(saved, Files.readAllBytes(file));
        logic.execute("unstar 1");
        logic.execute("unstar 1");
        assertEquals(3, storage.saveCount);
    }

    @Test
    void execute_failedStarAndUnstar_restoresHistoryFilterAndFile() throws Exception {
        Person original = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING)).build();
        model.setPerson(ALICE, original);
        logic.execute("find Alice");
        storage.saveContactFile(model.getContactFile());
        for (boolean starred : List.of(false, true)) {
            storage.shouldFail = false;
            if (starred) {
                logic.execute("star 1");
            }
            byte[] saved = Files.readAllBytes(file);
            ContactFile before = new ContactFile(model.getContactFile());
            storage.shouldFail = true;
            assertThrows(CommandException.class, () -> logic.execute(starred ? "unstar 1" : "star 1"));
            assertEquals(before, model.getContactFile());
            assertEquals(List.of(original.withStarred(starred)), model.getFilteredPersonList());
            assertArrayEquals(saved, Files.readAllBytes(file));
        }
    }

    @Test
    void execute_newAction_savesOnceAndDisplaysUpdatedOrderedList() throws Exception {
        List<Person> original = List.copyOf(model.getContactFile().getPersonList());
        CommandResult result = logic.execute("followup 1 a/  Send portfolio  ");
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals(List.of(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING)), updated.getFollowUps());
        assertTrue(result.getFeedbackToUser().contains("#1 [PENDING] Send portfolio"));
        assertFalse(result.getPendingRequest().isPresent());
        assertEquals(1, storage.saveCount);
        assertEquals(original.subList(1, original.size()), model.getContactFile().getPersonList().subList(1,
                original.size()));
        assertEquals(model.getContactFile().getPersonList(), storage.readContactFile().orElseThrow().getPersonList());
        assertTrue(logic.execute("view 1").getFeedbackToUser().contains("#1 [PENDING] Send portfolio"));
        assertEquals(1, storage.saveCount);
    }

    @Test
    void execute_duplicatePendingAction_waitsThenAddsOnlyOnYes() throws Exception {
        logic.execute("followup 1 a/Send portfolio");
        ContactFile before = new ContactFile(model.getContactFile());
        byte[] bytes = Files.readAllBytes(file);
        CommandResult prompt = logic.execute("followup 1 a/  Send portfolio  ");
        assertTrue(prompt.getPendingRequest().isPresent());
        assertTrue(prompt.getFeedbackToUser().contains("#1 [PENDING] Send portfolio"));
        assertTrue(prompt.getFeedbackToUser().contains("Add another?"));
        for (String reply : List.of("maybe", "list", "followup 2 a/Other", "", "yes")) {
            assertTrue(logic.execute(reply).getPendingRequest().isPresent());
            assertEquals(before, model.getContactFile());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertEquals(1, storage.saveCount);
        }
        logic.execute(" Y ");
        assertEquals(List.of(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING),
                new FollowUp(2, "Send portfolio", FollowUp.Status.PENDING)),
                model.getFilteredPersonList().getFirst().getFollowUps());
        assertEquals(2, storage.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    void execute_duplicateCancellation_preservesFileAndFilteredView() throws Exception {
        logic.execute("find Benson");
        logic.execute("followup 1 a/Call");
        ContactFile before = new ContactFile(model.getContactFile());
        byte[] bytes = Files.readAllBytes(file);
        List<Person> shown = List.copyOf(model.getFilteredPersonList());
        for (String reply : List.of("n", "cancel", " N ", "CANCEL")) {
            logic.execute("followup 1 a/Call");
            logic.execute(reply);
            assertEquals(before, model.getContactFile());
            assertEquals(shown, model.getFilteredPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertEquals(1, storage.saveCount);
            assertThrows(ParseException.class, () -> logic.execute("y"));
        }
    }

    @Test
    void execute_filteredIndex_updatesDisplayedContactAndRetainsFilter() throws Exception {
        logic.execute("find Benson");
        logic.execute("followup 1 a/Call");
        assertEquals(1, model.getFilteredPersonList().size());
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals(BENSON.getName(), updated.getName());
        assertEquals(List.of(new FollowUp(1, "Call", FollowUp.Status.PENDING)), updated.getFollowUps());
        assertEquals(ALICE, model.getContactFile().getPersonList().getFirst());
    }

    @Test
    void execute_confirmAfterViewChanges_usesRetainedContact() throws Exception {
        logic.execute("followup 1 a/Call");
        logic.execute("followup 1 a/Call");
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        logic.execute("y");
        assertEquals(2, model.getContactFile().getPersonList().getFirst().getFollowUps().size());
        assertTrue(model.getFilteredPersonList().getFirst().getFollowUps().isEmpty());
        assertEquals(2, storage.saveCount);
    }

    @Test
    void execute_completedDuplicateAndSparseIds_addsPendingWithGreatestIdPlusOne() throws Exception {
        Person history = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(9, "Send portfolio", FollowUp.Status.COMPLETED),
                new FollowUp(3, "Call", FollowUp.Status.PENDING)).build();
        model.setPerson(ALICE, history);
        CommandResult result = logic.execute("followup 1 a/Send portfolio");
        assertTrue(result.getPendingRequest().isEmpty());
        List<FollowUp> actions = model.getFilteredPersonList().getFirst().getFollowUps();
        assertEquals(List.of(9, 3, 10), actions.stream().map(FollowUp::getId).toList());
        assertEquals(FollowUp.Status.PENDING, actions.getLast().getStatus());
        assertEquals(actions, storage.readContactFile().orElseThrow().getPersonList().getFirst().getFollowUps());
    }

    @Test
    void execute_caseDifferentDescription_isNotAnExactDuplicate() throws Exception {
        logic.execute("followup 1 a/Call");
        assertTrue(logic.execute("followup 1 a/call").getPendingRequest().isEmpty());
        assertEquals(2, storage.saveCount);
    }

    @Test
    void execute_invalidRequest_doesNotChangeOrSave() throws Exception {
        ContactFile before = new ContactFile(model.getContactFile());
        byte[] bytes = Files.readAllBytes(file);
        assertThrows(CommandException.class, () -> logic.execute("followup 99 a/Call"));
        assertThrows(ParseException.class, () -> logic.execute("followup 1 a/ "));
        assertThrows(ParseException.class, () -> logic.execute("followup 1 a/Call confirm/yes"));
        assertEquals(before, model.getContactFile());
        assertEquals(0, storage.saveCount);
        assertArrayEquals(bytes, Files.readAllBytes(file));
        model.setContactFile(new ContactFile());
        assertThrows(CommandException.class, () -> logic.execute("followup 1 a/Call"));
    }

    @Test
    void execute_failedSave_restoresHistoryViewBindingAndFile() throws Exception {
        logic.execute("find Alice");
        logic.execute("followup 1 a/Call");
        for (boolean duplicate : List.of(false, true)) {
            ContactFile before = new ContactFile(model.getContactFile());
            byte[] bytes = Files.readAllBytes(file);
            ObservableList<Person> binding = model.getFilteredPersonList();
            storage.shouldFail = true;
            if (duplicate) {
                logic.execute("followup 1 a/Call");
                assertThrows(CommandException.class, () -> logic.execute("y"));
            } else {
                assertThrows(CommandException.class, () -> logic.execute("followup 1 a/Send portfolio"));
            }
            assertEquals(before, model.getContactFile());
            assertEquals(List.of(before.getPersonList().getFirst()), model.getFilteredPersonList());
            assertSame(binding, model.getFilteredPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertThrows(ParseException.class, () -> logic.execute("y"));
            storage.shouldFail = false;
        }
        assertEquals(3, storage.saveCount);
        logic.execute("followup 1 a/Send portfolio");
        assertEquals(2, model.getFilteredPersonList().getFirst().getFollowUps().getLast().getId());
    }

    @Test
    void execute_editAndRestart_preservesActionsAndNextId() throws Exception {
        logic.execute("followup 1 a/Send portfolio");
        logic.execute("followup 1 a/Call");
        logic.execute("edit 1 n/Alice Changed p/87654321 c/ t/work");
        Person edited = model.getFilteredPersonList().getFirst();
        assertEquals(2, edited.getFollowUps().size());
        assertTrue(edited.getCompany().isEmpty());
        assertEquals("Alice Changed", edited.getName().fullName);
        assertEquals(edited, storage.loadForStartup().contacts().getFirst());
        ModelManager reloaded = new ModelManager(storage.readContactFile().orElseThrow(), new UserPrefs());
        LogicManager restarted = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        restarted.execute("followup 1 a/Send notes");
        assertEquals(3, reloaded.getFilteredPersonList().getFirst().getFollowUps().getLast().getId());
    }

    @Test
    void execute_changedPendingContact_rejectsConfirmationAndClearsRequest() throws Exception {
        logic.execute("followup 1 a/Call");
        logic.execute("followup 1 a/Call");
        Person target = model.getFilteredPersonList().getFirst();
        model.setPerson(target, new PersonBuilder(target).withName("Changed").build());
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertThrows(ParseException.class, () -> logic.execute("y"));
        assertEquals(1, storage.saveCount);
    }

    @Test
    void execute_exhaustedIds_preservesHistoryWithoutOverflow() throws Exception {
        Person full = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(Integer.MAX_VALUE, "Call", FollowUp.Status.COMPLETED)).build();
        model.setPerson(ALICE, full);
        assertThrows(CommandException.class, () -> logic.execute("followup 1 a/Call"));
        assertEquals(full, model.getFilteredPersonList().getFirst());
        assertEquals(0, storage.saveCount);
    }

    @Test
    void command_valueSemantics_preservesTrimmedRequest() {
        FollowUpCommand command = new FollowUpCommand(Index.fromOneBased(1), "Call");
        assertEquals(command, command);
        assertEquals(command, new FollowUpCommand(Index.fromOneBased(1), " Call "));
        assertNotEquals(command, new FollowUpCommand(Index.fromOneBased(2), "Call"));
        assertNotEquals(command, new FollowUpCommand(Index.fromOneBased(1), "Other"));
        assertNotEquals(command, null);
        assertNotEquals(command, "Call");
        assertTrue(command.toString().contains("description=Call"));
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertThrows(NullPointerException.class, () -> new FollowUpCommand(null, "Call"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpCommand(Index.fromOneBased(1), " "));
    }

    private static final class CountingStorage extends JsonContactFileStorage {
        private int saveCount;
        private boolean shouldFail;

        private CountingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveContactFile(ReadOnlyContactFile contacts) throws IOException {
            saveCount++;
            if (shouldFail) {
                throw new IOException("Simulated follow-up save failure");
            }
            super.saveContactFile(contacts);
        }
    }
}
