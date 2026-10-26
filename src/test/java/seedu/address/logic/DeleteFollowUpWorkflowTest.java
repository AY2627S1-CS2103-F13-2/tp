package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class DeleteFollowUpWorkflowTest {
    private static final FollowUp FIRST = new FollowUp(2, "Call", FollowUp.Status.PENDING,
            LocalDate.of(2026, 10, 30));
    private static final FollowUp LAST = new FollowUp(9, "Send notes", FollowUp.Status.COMPLETED);
    private static final Person ORIGINAL = new PersonBuilder(ALICE).withFollowUps(FIRST, LAST)
            .withIntroducer("Jane Smith").withStarred(true).build()
            .withEvent(new Event("Networking"))
            .withInteraction(new Interaction(LocalDate.of(2026, 10, 20), "Discussed portfolio"));

    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private CountingStorage storage;
    private Path file;

    @BeforeEach
    void setUp() throws Exception {
        ContactFile contacts = new ContactFile();
        contacts.addPerson(ORIGINAL);
        contacts.addPerson(BENSON);
        model = new ModelManager(contacts, new UserPrefs());
        file = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(file).saveContactFile(contacts);
        storage = new CountingStorage(file);
        logic = createLogic(model);
    }

    @Test
    void execute_previewAndInvalidReplies_neverMutateOrSave() throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        CommandResult preview = logic.execute("deletefollowup 1 i/2");
        assertTrue(preview.getFeedbackToUser().contains(ORIGINAL.getName().fullName));
        assertTrue(preview.getFeedbackToUser().contains(Messages.formatFollowUp(FIRST)));
        assertFalse(preview.getFeedbackToUser().contains(Messages.formatFollowUp(LAST)));
        for (String reply : List.of("maybe", "yes", "list", "clear", "deletefollowup 2 i/2", "")) {
            assertTrue(logic.execute(reply).getPendingRequest().isPresent());
            assertEquals(List.of(ORIGINAL, BENSON), model.getContactFile().getPersonList());
            assertEquals(0, storage.saveCount);
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
    }

    @Test
    void execute_cancel_restoresFilteredViewAndClearsRequest() throws Exception {
        logic.execute("find Alice");
        ObservableList<Person> binding = model.getFilteredPersonList();
        byte[] bytes = Files.readAllBytes(file);
        for (String reply : List.of("n", "cancel", " N ", "CANCEL")) {
            logic.execute("deletefollowup 1 i/9");
            logic.execute(reply);
            assertEquals(List.of(ORIGINAL), model.getFilteredPersonList());
            assertSame(binding, model.getFilteredPersonList());
            assertThrows(ParseException.class, () -> logic.execute("y"));
            assertEquals(0, storage.saveCount);
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
    }

    @Test
    void execute_confirm_preservesOtherMetadataOrderAndFilteredBinding() throws Exception {
        logic.execute("find Alice");
        ObservableList<Person> binding = model.getFilteredPersonList();
        logic.execute("deletefollowup 1 i/2");
        CommandResult result = logic.execute(" Y ");
        Person expected = ORIGINAL.removeFollowUp(2);
        assertTrue(result.getFeedbackToUser().contains("Deleted follow-up #2"));
        assertEquals(List.of(LAST), expected.getFollowUps());
        assertEquals(List.of(expected, BENSON), model.getContactFile().getPersonList());
        assertEquals(List.of(expected), binding);
        assertSame(binding, model.getFilteredPersonList());
        assertEquals(model.getContactFile(), storage.readContactFile().orElseThrow());
        assertEquals(1, storage.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    void execute_filteredIndex_selectsDisplayedContactAndRetainsResolvedTarget() throws Exception {
        Person benson = BENSON.appendFollowUp(new FollowUp(2, "Benson action", FollowUp.Status.PENDING));
        model.setPerson(BENSON, benson);
        logic.execute("find Benson");
        logic.execute("deletefollowup 1 i/2");
        model.updateFilteredPersonList(person -> person.equals(ORIGINAL));
        logic.execute("y");
        assertEquals(ORIGINAL, model.getContactFile().getPersonList().getFirst());
        assertEquals(benson.removeFollowUp(2), model.getContactFile().getPersonList().getLast());
    }

    @Test
    void execute_deleteHighestAndAllActions_neverReusesIdsAfterRestart() throws Exception {
        for (int id : List.of(9, 2)) {
            logic.execute("deletefollowup 1 i/" + id);
            logic.execute("y");
        }
        restart();
        Person retired = model.getFilteredPersonList().getFirst();
        assertTrue(retired.getFollowUps().isEmpty());
        assertEquals(9, retired.getLastFollowUpId());
        logic.execute("followup 1 a/New action");
        assertEquals(10, model.getFilteredPersonList().getFirst().getFollowUps().getFirst().getId());
        assertEquals(model.getContactFile().getPersonList(), storage.loadForStartup().contacts());
    }

    @Test
    void execute_contactCopiesAndRestart_preserveRetiredIds() throws Exception {
        logic.execute("deletefollowup 1 i/9");
        logic.execute("y");
        logic.execute("edit 1 c/New Company");
        logic.execute("unstar 1");
        logic.execute("introduce Alice i/New Introducer");
        logic.execute("y");
        logic.execute("drop 1 c/New Company");
        logic.execute("y");
        logic.execute("editfollowup 1 i/2 a/Rescheduled call");
        restart();
        Person before = model.getFilteredPersonList().getFirst();
        assertEquals(9, before.getLastFollowUpId());
        assertEquals(ORIGINAL.getEvents(), before.getEvents());
        assertEquals(ORIGINAL.getInteractions(), before.getInteractions());
        logic.execute("followup 1 a/After copies");
        assertEquals(10, model.getFilteredPersonList().getFirst().getFollowUps().getLast().getId());
    }

    @Test
    void execute_failedSave_restoresFileHistoryCounterAndDynamicFilter() throws Exception {
        logic.execute("find Alice");
        ObservableList<Person> binding = model.getFilteredPersonList();
        byte[] bytes = Files.readAllBytes(file);
        storage.shouldFail = true;
        logic.execute("deletefollowup 1 i/9");
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertEquals(List.of(ORIGINAL), binding);
        assertSame(binding, model.getFilteredPersonList());
        assertEquals(9, binding.getFirst().getLastFollowUpId());
        assertArrayEquals(bytes, Files.readAllBytes(file));
        assertThrows(ParseException.class, () -> logic.execute("y"));
        storage.shouldFail = false;
        logic.execute("deletefollowup 1 i/9");
        logic.execute("y");
        assertEquals(2, storage.saveCount);
    }

    @Test
    void execute_staleContact_rejectsConfirmationAndReplay() throws Exception {
        logic.execute("deletefollowup 1 i/9");
        model.setPerson(ORIGINAL, ORIGINAL.withStarred(false));
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertThrows(ParseException.class, () -> logic.execute("y"));
        assertEquals(0, storage.saveCount);
        assertEquals(ORIGINAL, model.getContactFile().getPersonList().getFirst());
    }

    @Test
    void execute_invalidTarget_neverSavesOrStartsPendingRequest() throws Exception {
        for (String command : List.of("deletefollowup 99 i/2", "deletefollowup 2 i/2", "deletefollowup 1 i/1")) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertThrows(ParseException.class, () -> logic.execute("y"));
        }
        assertEquals(List.of(ORIGINAL, BENSON), model.getContactFile().getPersonList());
        assertEquals(0, storage.saveCount);
    }

    @Test
    void execute_deleteDuringReview_transitionsToConfirmationWithoutChangingData() throws Exception {
        logic.execute("followups");
        assertTrue(logic.execute("deletefollowup 1 i/2").getPendingRequest().isPresent());
        assertEquals(0, storage.saveCount);
        logic.execute("y");
        assertEquals(ORIGINAL.removeFollowUp(2), model.getContactFile().getPersonList().getFirst());
        assertEquals(1, storage.saveCount);
    }

    @Test
    void execute_exhaustedCounterAfterDeletion_stillRejectsAllocation() throws Exception {
        Person full = ALICE.appendFollowUp(new FollowUp(Integer.MAX_VALUE, "Last", FollowUp.Status.PENDING));
        model.setPerson(ORIGINAL, full);
        logic.execute("deletefollowup 1 i/2147483647");
        logic.execute("y");
        restart();
        assertThrows(CommandException.class, () -> logic.execute("followup 1 a/Overflow"));
        assertEquals(Integer.MAX_VALUE, model.getFilteredPersonList().getFirst().getLastFollowUpId());
        assertTrue(model.getFilteredPersonList().getFirst().getFollowUps().isEmpty());
    }

    private LogicManager createLogic(ModelManager currentModel) {
        return new LogicManager(currentModel, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    private void restart() throws Exception {
        model = new ModelManager(storage.readContactFile().orElseThrow(), new UserPrefs());
        logic = createLogic(model);
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
                throw new IOException("Simulated deletion save failure");
            }
            super.saveContactFile(contacts);
        }
    }
}
