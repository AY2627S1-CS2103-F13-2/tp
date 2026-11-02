package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Interaction;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

/**
 * Exercises selection and live details in the terminal workspace without saving contact data.
 */
@Timeout(20)
class TerminalWorkspaceUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    void closeWindows() throws Exception {
        FxTestUtil.runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    void selectionAndFiltering_noMatchPreservesDisplayedContact() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = openWindow(model);
            assertEquals(ALICE.getName().fullName, label(stage, "detailName").getText());
            ListView<?> list = (ListView<?>) stage.getScene().lookup("#personListView");
            list.getSelectionModel().select(1);
            assertEquals(BENSON.getName().fullName, label(stage, "detailName").getText());
            execute(stage, "find Alice");
            assertEquals(ALICE.getName().fullName, label(stage, "detailName").getText());
            execute(stage, "find NoSuchContact");
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
            assertEquals(ALICE.getName().fullName, label(stage, "detailName").getText());
            execute(stage, "list");
            assertEquals(ALICE.getName().fullName, label(stage, "detailName").getText());
        });
        assertFalse(Files.exists(testFolder.resolve("contacts.json")));
    }

    @Test
    void namedView_showsNotesAndTracksTheResolvedContact() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE.withInteraction(new Interaction(LocalDate.of(2026, 10, 28),
                "Discussed internship opportunities and agreed to follow up next week.")));
        model.addPerson(BENSON);
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = openWindow(model);
            execute(stage, "find Benson");
            execute(stage, "view Alice");
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(ALICE.getName(), model.getFilteredPersonList().getFirst().getName());
            assertEquals(ALICE.getName().fullName, label(stage, "detailName").getText());
            Label notes = label(stage, "interactionsValue");
            assertTrue(notes.getText().contains("internship opportunities"));
            assertTrue(notes.isWrapText());
            assertEquals("view Alice", label(stage, "lastCommand").getText());
            assertNotNull(stage.getScene().lookup("#commandTextField"));
        });
        assertFalse(Files.exists(testFolder.resolve("contacts.json")));
    }

    @Test
    void editedAndRemovedContact_refreshesDetailsWithoutStaleValues() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = openWindow(model);
            model.setPerson(ALICE, ALICE.withStarred(true));
            assertEquals(ALICE.getName().fullName + " ★", label(stage, "detailName").getText());
            model.deletePerson(ALICE.withStarred(true));
            assertEquals(BENSON.getName().fullName, label(stage, "detailName").getText());
            assertFalse(label(stage, "detailName").getText().contains(ALICE.getName().fullName));
        });
    }

    @Test
    void narrowWindow_wrapsLongNotesInsideTheDetailCard() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE.withInteraction(new Interaction(LocalDate.of(2026, 10, 28),
                "Follow up about the internship opportunity, send a portfolio, and arrange another conversation.")));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = openWindow(model);
            Region root = (Region) stage.getScene().getRoot();
            root.resize(760, 480);
            root.applyCss();
            root.layout();
            Region card = (Region) stage.getScene().lookup("#personDetailsPlaceholder");
            Label notes = label(stage, "interactionsValue");
            assertTrue(card.getWidth() >= 340);
            assertTrue(notes.getHeight() > 30);
            assertTrue(notes.localToScene(notes.getBoundsInLocal()).getMaxX()
                    <= card.localToScene(card.getBoundsInLocal()).getMaxX());
        });
    }

    @Test
    void windowConstruction_appliesSavedSizeAndPosition() throws Exception {
        ModelManager model = new ModelManager();
        model.setGuiSettings(new GuiSettings(900, 700, 100, 120));

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = openWindow(model);
            assertEquals(900, stage.getWidth());
            assertEquals(700, stage.getHeight());
            assertEquals(100, stage.getX());
            assertEquals(120, stage.getY());
        });
    }

    private Stage openWindow(ModelManager model) {
        Path contacts = testFolder.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonContactFileStorage(contacts),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        Stage stage = new Stage();
        MainWindow window = new MainWindow(stage, logic, contacts);
        window.fillInnerParts("Loaded contacts.");
        window.show();
        return stage;
    }

    private static Label label(Stage stage, String id) {
        return (Label) stage.getScene().lookup("#" + id);
    }

    private static void execute(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
