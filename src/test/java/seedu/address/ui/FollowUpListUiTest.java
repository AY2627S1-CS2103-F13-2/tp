package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

/**
 * Checks that structured results, row selection, and typed selection remain synchronized.
 */
@Timeout(20)
class FollowUpListUiTest {
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
    void rowsAndNumbers_selectTheSameActionWithoutChangingContactFilterOrSaving() throws Exception {
        ModelManager model = new ModelManager();
        Person alice = ALICE.appendFollowUp(new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING))
                .appendFollowUp(new FollowUp(9, "Call recruiter", FollowUp.Status.PENDING, LocalDate.of(2000, 2, 29)));
        model.addPerson(alice);
        model.addPerson(BENSON);
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = open(model);
            execute(stage, "followups");
            ListView<FollowUpEntry> list = followUps(stage);
            assertTrue(stage.getScene().lookup("#followUpListPanel").isVisible());
            assertEquals(List.of(9, 7), list.getItems().stream().map(entry -> entry.followUp().getId()).toList());
            assertTrue(label(stage, "selectedFollowUpText").contains("Call recruiter"));
            assertEquals("Pending follow-ups (2):", output(stage));
            assertFalse(output(stage).contains("Call recruiter"));
            list.getSelectionModel().select(1);
            assertTrue(label(stage, "selectedFollowUpText").contains("Send portfolio"));
            execute(stage, "2");
            assertEquals(1, list.getSelectionModel().getSelectedIndex());
            assertTrue(label(stage, "selectedFollowUpText").contains("#7"));
            assertEquals("Viewing follow-up #7 for Alice Pauline.", output(stage));
            assertEquals(List.of(BENSON), model.getFilteredPersonList());
            execute(stage, "list");
            assertFalse(stage.getScene().lookup("#followUpListPanel").isVisible());
            assertNull(stage.getScene().lookup("#selectedFollowUpText"));
        });
        assertFalse(Files.exists(testFolder.resolve("contacts.json")));
    }

    @Test
    void filtersEmptyResultsInvalidNumbersAndCancellation_clearOldRowsAndContext() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE.appendFollowUp(new FollowUp(1, "Call", FollowUp.Status.PENDING))
                .appendFollowUp(new FollowUp(2, "Already sent", FollowUp.Status.COMPLETED)));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = open(model);
            execute(stage, "followups completed");
            assertEquals(1, followUps(stage).getItems().size());
            assertEquals(FollowUp.Status.COMPLETED, followUps(stage).getItems().getFirst().followUp().getStatus());
            execute(stage, "99");
            assertEquals(1, followUps(stage).getItems().size());
            assertEquals("Enter a number from the displayed follow-ups.", output(stage));
            execute(stage, "cancel");
            assertFalse(stage.getScene().lookup("#followUpListPanel").isVisible());
            assertNull(stage.getScene().lookup("#selectedFollowUpText"));
            execute(stage, "followups upcoming");
            assertTrue(stage.getScene().lookup("#followUpListPanel").isVisible());
            assertTrue(followUps(stage).getItems().isEmpty());
            assertNull(stage.getScene().lookup("#selectedFollowUpText"));
            assertNull(stage.getScene().lookup("#detailName"));
            assertEquals("No upcoming follow-ups remain.", output(stage));
            execute(stage, "find Alice");
            assertFalse(stage.getScene().lookup("#followUpListPanel").isVisible());
            assertEquals(ALICE.getName().fullName, label(stage, "detailName"));
        });
    }

    private Stage open(ModelManager model) {
        Path contacts = testFolder.resolve("contacts.json");
        Stage stage = new Stage();
        MainWindow window = new MainWindow(stage, new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(contacts),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")))), contacts);
        window.fillInnerParts("Loaded");
        window.show();
        return stage;
    }

    @SuppressWarnings("unchecked")
    private static ListView<FollowUpEntry> followUps(Stage stage) {
        return (ListView<FollowUpEntry>) stage.getScene().lookup("#followUpListView");
    }

    private static String label(Stage stage, String id) {
        return ((Label) stage.getScene().lookup("#" + id)).getText();
    }

    private static String output(Stage stage) {
        return ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private static void execute(Stage stage, String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
