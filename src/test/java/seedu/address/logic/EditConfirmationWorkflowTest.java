package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class EditConfirmationWorkflowTest {
    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private CountingStorage storage;
    private Path file;

    @BeforeEach
    void setUp() throws Exception {
        ContactFile contacts = new ContactFile();
        contacts.addPerson(ALICE);
        contacts.addPerson(BENSON);
        model = new ModelManager(contacts, new UserPrefs());
        file = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(file).saveContactFile(contacts);
        storage = new CountingStorage(file);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    @Test
    void execute_previewAndInvalidReplies_preserveDataUntilConfirmed() throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        CommandResult preview = logic.execute("edit 1 c/New Company p/76543210");
        assertTrue(preview.getFeedbackToUser().contains("Resulting contact:"));
        assertTrue(preview.getFeedbackToUser().contains("New Company"));
        assertTrue(preview.getFeedbackToUser().contains("76543210"));
        for (String reply : List.of("maybe", "list", "yes", "clear", "")) {
            assertTrue(logic.execute(reply).getPendingRequest().isPresent());
            assertEquals(List.of(ALICE, BENSON), model.getContactFile().getPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertEquals(0, storage.saveCount);
        }
        logic.execute("y");
        assertEquals(new PersonBuilder(ALICE).withCompany("New Company").withPhone("76543210").build(),
                model.getContactFile().getPersonList().getFirst());
        assertEquals(1, storage.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
    }

    @Test
    void execute_cancelNamedEdit_restoresOriginalDynamicFilterAndBinding() throws Exception {
        logic.execute("find Benson");
        var binding = model.getFilteredPersonList();
        byte[] bytes = Files.readAllBytes(file);
        for (String reply : List.of("n", "cancel")) {
            logic.execute("edit Alice c/New Company");
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
            logic.execute(reply);
            assertEquals(List.of(BENSON), binding);
            assertSame(binding, model.getFilteredPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
        assertEquals(0, storage.saveCount);
    }

    @Test
    void execute_changedDisplay_confirmsOriginallyResolvedContact() throws Exception {
        logic.execute("edit 1 c/New Company");
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        logic.execute("y");
        assertEquals("New Company", model.getContactFile().getPersonList().getFirst().getCompany().orElseThrow().value);
        assertEquals(BENSON, model.getContactFile().getPersonList().getLast());
        assertEquals(2, model.getFilteredPersonList().size());
    }

    @Test
    void execute_failedSave_restoresPriorDataAndFilterAndClearsRequest() throws Exception {
        logic.execute("find Benson");
        byte[] bytes = Files.readAllBytes(file);
        storage.shouldFail = true;
        logic.execute("edit Alice c/");
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertEquals(List.of(ALICE, BENSON), model.getContactFile().getPersonList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertArrayEquals(bytes, Files.readAllBytes(file));
        assertThrows(ParseException.class, () -> logic.execute("y"));
        assertEquals(1, storage.saveCount);
    }

    @Test
    void execute_invalidOrNoOpEdit_doesNotStartConfirmation() throws Exception {
        for (String command : List.of("edit 1 n/Alice Pauline", "edit 1 p/" + BENSON.getPhone().orElseThrow(),
                "edit 99 c/New Company")) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertThrows(ParseException.class, () -> logic.execute("y"));
        }
        assertEquals(0, storage.saveCount);
    }

    @Test
    void execute_changedTargetOrNewConflict_rejectsConfirmation() throws Exception {
        logic.execute("edit 1 c/New Company");
        model.setPerson(ALICE, ALICE.withStarred(true));
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertThrows(ParseException.class, () -> logic.execute("y"));
        logic.execute("edit 1 p/76543210");
        model.setPerson(BENSON, new PersonBuilder(BENSON).withPhone("76543210").build());
        assertThrows(CommandException.class, () -> logic.execute("y"));
        assertEquals(List.of(ALICE, BENSON), model.getContactFile().getPersonList());
        assertEquals(0, storage.saveCount);
    }

    @Test
    void execute_newIdentityConflict_rejectsConfirmedRemovalOfContactKeys() throws Exception {
        logic.execute("edit 1 n/New Name p/ e/");
        model.addPerson(new PersonBuilder(ALICE).withName("New Name").withPhone(null).withEmail(null).build());
        CommandException failure = assertThrows(CommandException.class, () -> logic.execute("y"));
        assertEquals(EditCommand.MESSAGE_DUPLICATE_PERSON, failure.getMessage());
        assertEquals(List.of(ALICE, BENSON), model.getContactFile().getPersonList());
        assertEquals(0, storage.saveCount);
        assertThrows(ParseException.class, () -> logic.execute("y"));
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
                throw new IOException("Simulated edit save failure");
            }
            super.saveContactFile(contacts);
        }
    }
}
