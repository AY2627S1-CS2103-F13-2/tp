package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ContactFile;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises real routing, filtering, displayed-index viewing, and zero-write persistence guarantees.
 */
class TagFilterWorkflowTest {
    private static final Person ZOE = new PersonBuilder().withName("Zoe").withTags("work", "friend")
            .withPhone("91234567").withEmail("zoe@example.com").build();
    private static final Person ADAM = new PersonBuilder().withName("Adam").withTags("work")
            .withPhone(null).withEmail(null).build();
    private static final Person MARY = new PersonBuilder().withName("Mary").withTags("friend")
            .withPhone(null).withEmail(null).build();
    private static final Person UNTAGGED = new PersonBuilder().withName("Untagged").withTags()
            .withPhone(null).withEmail(null).build();
    private static final List<Person> STORED = List.of(ZOE, ADAM, MARY, UNTAGGED);

    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private NoWriteStorage storage;
    private Path file;
    private byte[] originalBytes;

    @BeforeEach
    void setUp() throws Exception {
        ContactFile contacts = new ContactFile();
        STORED.forEach(contacts::addPerson);
        model = new ModelManager(contacts, new UserPrefs());
        file = testFolder.resolve("contacts.json");
        new JsonContactFileStorage(file).saveContactFile(contacts);
        originalBytes = Files.readAllBytes(file);
        storage = new NoWriteStorage(file);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    @AfterEach
    void verifyNoWrites() throws Exception {
        assertEquals(0, storage.saveCount);
        assertArrayEquals(originalBytes, Files.readAllBytes(file));
    }

    @Test
    void execute_defaultAnd_requiresAllTagsWithoutChangingContactsOrBinding() throws Exception {
        ObservableList<Person> binding = model.getFilteredPersonList();
        String feedback = logic.execute("filter t/work t/friend").getFeedbackToUser();
        assertEquals(List.of(ZOE), model.getFilteredPersonList());
        assertEquals(STORED, model.getContactFile().getPersonList());
        assertSame(binding, model.getFilteredPersonList());
        assertEquals(String.format(FilterCommand.MESSAGE_MATCHES, 1, "AND"), feedback);
        logic.execute("filter mode/and t/friend t/work");
        assertEquals(List.of(ZOE), model.getFilteredPersonList());
    }

    @Test
    void execute_or_preservesStoredOrderAndNeverDuplicatesContacts() throws Exception {
        String feedback = logic.execute("filter mode/OR t/friend t/work t/work").getFeedbackToUser();
        assertEquals(List.of(ZOE, ADAM, MARY), model.getFilteredPersonList());
        assertEquals(String.format(FilterCommand.MESSAGE_MATCHES, 3, "OR"), feedback);
        assertEquals(STORED, model.getContactFile().getPersonList());
        logic.execute("filter t/work t/work");
        assertEquals(List.of(ZOE, ADAM), model.getFilteredPersonList());
    }

    @Test
    void execute_tagCaseAndWholeTagMatch_preservesExistingSemantics() throws Exception {
        assertEquals(FilterCommand.MESSAGE_NO_MATCHES,
                logic.execute("filter mode/or t/Work t/workplace").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("filter mode/oR t/work");
        assertEquals(List.of(ZOE, ADAM), model.getFilteredPersonList());
    }

    @Test
    void execute_filteredView_usesDisplayedIndexAndShowsAllDetails() throws Exception {
        logic.execute("filter t/friend");
        assertEquals(Messages.format(MARY), logic.execute("view 2").getFeedbackToUser());
        assertEquals(Messages.format(ZOE), logic.execute("view 1").getFeedbackToUser());
        assertThrows(CommandException.class, () -> logic.execute("view 3"));
        assertEquals(List.of(ZOE, MARY), model.getFilteredPersonList());
    }

    @Test
    void execute_noMatches_showsEmptyListAndListRestoresEveryone() throws Exception {
        assertEquals(FilterCommand.MESSAGE_NO_MATCHES,
                logic.execute("filter t/absent").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertThrows(CommandException.class, () -> logic.execute("view 1"));
        logic.execute("list");
        assertEquals(STORED, model.getFilteredPersonList());
    }

    @Test
    void execute_newFilter_replacesPriorFindAndEmptyFilter() throws Exception {
        logic.execute("find Adam");
        assertEquals(List.of(ADAM), model.getFilteredPersonList());
        logic.execute("filter t/friend");
        assertEquals(List.of(ZOE, MARY), model.getFilteredPersonList());
        logic.execute("filter mode/and t/missing");
        logic.execute("filter mode/or t/work");
        assertEquals(List.of(ZOE, ADAM), model.getFilteredPersonList());
        logic.execute("find Mary");
        assertEquals(List.of(MARY), model.getFilteredPersonList());
        logic.execute("list");
        assertEquals(STORED, model.getFilteredPersonList());
    }

    @Test
    void execute_invalidInput_preservesPriorDataAndDynamicPredicate() throws Exception {
        logic.execute("filter t/work");
        ObservableList<Person> binding = model.getFilteredPersonList();
        for (String command : List.of("filter", "filter mode/or", "filter t/work t/", "filter mode/xor t/work",
                "filter mode/ t/work", "filter mode/and t/work mode/or", "filter t/work t/bad!",
                "filter t/work x/value", "filter work")) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertEquals(List.of(ZOE, ADAM), model.getFilteredPersonList());
            assertEquals(STORED, model.getContactFile().getPersonList());
            assertSame(binding, model.getFilteredPersonList());
        }
        Person nowTagged = new PersonBuilder(UNTAGGED).withTags("work").build();
        model.setPerson(UNTAGGED, nowTagged);
        assertEquals(List.of(ZOE, ADAM, nowTagged), model.getFilteredPersonList());
    }

    @Test
    void execute_emptyFile_reportsNoMatchesWithoutWriting() throws Exception {
        model.setContactFile(new ContactFile());
        for (String mode : List.of("and", "or")) {
            assertEquals(FilterCommand.MESSAGE_NO_MATCHES,
                    logic.execute("filter mode/" + mode + " t/work").getFeedbackToUser());
            assertTrue(model.getFilteredPersonList().isEmpty());
        }
    }

    /**
     * Fails immediately if any query attempts to save, even when the resulting bytes would be identical.
     */
    private static final class NoWriteStorage extends JsonContactFileStorage {
        private int saveCount;

        private NoWriteStorage(Path path) {
            super(path);
        }

        @Override
        public void saveContactFile(ReadOnlyContactFile contacts) {
            saveCount++;
            throw new AssertionError("Read-only filtering/viewing must never attempt a contact-file save.");
        }
    }
}
