package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Interaction updates retain the current contacts and indexes until an explicit search refreshes them.
 */
public class InteractionViewStabilityTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(directory.resolve("contacts.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("add n/Alice Tan");
        logic.execute("add n/Bob Lee");
        logic.execute("add n/Carol Lim");
        logic.execute("log 1 nt/portfolio");
        logic.execute("log 2 nt/portfolio");
        logic.execute("log 3 nt/meeting");
        logic.execute("find /notes portfolio");
    }

    @Test
    public void execute_editLastMatchingNote_keepsContactIndexesAndNextLogTarget() throws Exception {
        logic.execute("editlog 1 i/1 nt/meeting");

        assertDisplayedNames("Alice Tan", "Bob Lee");
        logic.execute("log 1 nt/next conversation");
        assertEquals(2, model.getContactFile().getPersonList().get(0).getInteractions().size());
        assertEquals(1, model.getContactFile().getPersonList().get(1).getInteractions().size());
        assertDisplayedNames("Alice Tan", "Bob Lee");
    }

    @Test
    public void execute_unlogLastMatchingNote_keepsContactAfterConfirmation() throws Exception {
        logic.execute("unlog 1 i/1");
        assertDisplayedNames("Alice Tan", "Bob Lee");
        logic.execute("y");

        assertDisplayedNames("Alice Tan", "Bob Lee");
        assertTrue(model.getFilteredPersonList().getFirst().getInteractions().isEmpty());
    }

    @Test
    public void execute_editBothContacts_retainsBothUntilExplicitSearch() throws Exception {
        logic.execute("editlog 1 i/1 nt/meeting");
        logic.execute("editlog 2 i/1 nt/meeting");

        assertDisplayedNames("Alice Tan", "Bob Lee");
        logic.execute("find /notes meeting");
        assertDisplayedNames("Alice Tan", "Bob Lee", "Carol Lim");
        logic.execute("list");
        assertDisplayedNames("Alice Tan", "Bob Lee", "Carol Lim");
    }

    @Test
    public void execute_starAfterEditing_keepsRetainedContactVisible() throws Exception {
        logic.execute("editlog 1 i/1 nt/meeting");
        logic.execute("star 1");

        assertDisplayedNames("Alice Tan", "Bob Lee");
        assertTrue(model.getFilteredPersonList().getFirst().isStarred());
        logic.execute("find /notes portfolio");
        assertDisplayedNames("Bob Lee");
    }

    @Test
    public void execute_cancelUnlog_doesNotChangeNotesOrSearch() throws Exception {
        logic.execute("unlog 1 i/1");
        logic.execute("cancel");

        assertDisplayedNames("Alice Tan", "Bob Lee");
        assertEquals("portfolio", model.getFilteredPersonList().getFirst().getInteractions().getFirst().getNote());
    }

    private void assertDisplayedNames(String... names) {
        assertEquals(List.of(names), model.getFilteredPersonList().stream()
                .map(Person::getName).map(name -> name.fullName).toList());
    }
}
