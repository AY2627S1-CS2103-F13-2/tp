package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

@Timeout(20)
class ConfirmationPanelUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void window_sharedPanel_handlesDeleteClearAndDuplicateFollowUps() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = createWindow(model, new JsonContactFileStorage(testFolder.resolve("contacts.json")));
            try {
                Node panel = stage.getScene().lookup("#confirmationPanel");
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea preview = (TextArea) stage.getScene().lookup("#confirmationPreview");
                Button confirm = (Button) stage.getScene().lookup("#confirmButton");
                Button cancel = (Button) stage.getScene().lookup("#cancelButton");
                assertFalse(panel.isVisible());
                assertFalse(panel.isManaged());
                input.requestFocus();
                execute(input, "edit 1 c/New Company");
                assertSame(input, stage.getScene().getFocusOwner());
                assertTrue(confirm.isFocusTraversable());
                assertTrue(cancel.isFocusTraversable());
                assertTrue(panel.isVisible());
                assertTrue(preview.getText().contains("Resulting contact:"));
                assertEquals(ALICE, model.getContactFile().getPersonList().getFirst());
                confirm.fire();
                assertSame(input, stage.getScene().getFocusOwner());
                assertEquals("New Company", model.getContactFile().getPersonList().getFirst()
                        .getCompany().orElseThrow().value);
                assertFalse(panel.isVisible());
                execute(input, "delete 1");
                assertTrue(panel.isVisible());
                assertTrue(panel.isManaged());
                assertTrue(preview.getText().contains(ALICE.getName().fullName));
                assertFalse(preview.isEditable());
                assertTrue(preview.isWrapText());
                execute(input, "list");
                assertTrue(panel.isVisible());
                assertEquals(2, model.getContactFile().getPersonList().size());
                cancel.fire();
                assertFalse(panel.isVisible());
                assertEquals(2, model.getContactFile().getPersonList().size());
                execute(input, "followup 1 a/Call");
                execute(input, "followup 1 a/Call");
                assertTrue(preview.getText().contains("Add another?"));
                confirm.fire();
                assertEquals(2, model.getFilteredPersonList().getFirst().getFollowUps().size());
                assertFalse(panel.isVisible());
                execute(input, "deletefollowup 1 i/2");
                assertTrue(preview.getText().contains("#2 [PENDING] Call"));
                execute(input, "y");
                assertFalse(panel.isVisible());
                assertEquals(1, model.getFilteredPersonList().getFirst().getFollowUps().size());
                execute(input, "clear");
                assertTrue(preview.getText().contains("Clear all 2"));
                confirm.fire();
                confirm.fire();
                assertTrue(model.getContactFile().getPersonList().isEmpty());
                assertFalse(panel.isManaged());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    void window_selectionOnly_showsPanelAfterResolvedContactIsChosen() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(new PersonBuilder(BENSON).withName("Alice Other").build());
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = createWindow(model, new JsonContactFileStorage(testFolder.resolve("contacts.json")));
            try {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                Node panel = stage.getScene().lookup("#confirmationPanel");
                execute(input, "delete Alice");
                assertFalse(panel.isVisible());
                execute(input, "2");
                assertTrue(panel.isVisible());
                assertTrue(((TextArea) stage.getScene().lookup("#confirmationPreview")).getText()
                        .contains("Alice Other"));
                execute(input, "n");
                assertFalse(panel.isVisible());
                execute(input, "followup 1 a/Call");
                execute(input, "followups");
                assertFalse(panel.isVisible());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    void window_failedSave_hidesPanelAndIgnoresStaleEventsForLaterRequests() throws Exception {
        ModelManager model = new ModelManager();
        Person original = ALICE.appendFollowUp(new FollowUp(1, "Call", FollowUp.Status.PENDING));
        model.addPerson(original);
        FxTestUtil.runOnFxThread(() -> {
            JsonContactFileStorage failing = new JsonContactFileStorage(testFolder.resolve("contacts.json")) {
                private int attempts;

                @Override
                public void saveContactFile(ReadOnlyContactFile contacts) throws IOException {
                    if (++attempts > 2) {
                        throw new IllegalStateException();
                    }
                    if (attempts > 1) {
                        throw new IllegalStateException("Simulated unexpected storage failure");
                    }
                    throw new IOException("Simulated UI save failure");
                }
            };
            Stage stage = createWindow(model, failing);
            try {
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                Node panel = stage.getScene().lookup("#confirmationPanel");
                execute(input, "deletefollowup 1 i/1");
                Button confirm = (Button) stage.getScene().lookup("#confirmButton");
                EventHandler<ActionEvent> staleHandler = confirm.getOnAction();
                confirm.fire();
                assertFalse(panel.isVisible());
                assertEquals(List.of(original), model.getContactFile().getPersonList());
                assertTrue(((TextArea) stage.getScene().lookup("#resultDisplay")).getText()
                        .contains("Could not save data"));
                execute(input, "clear");
                assertTrue(panel.isVisible());
                staleHandler.handle(new ActionEvent());
                assertEquals(List.of(original), model.getContactFile().getPersonList());
                assertTrue(panel.isVisible());
                execute(input, "cancel");
                staleHandler.handle(new ActionEvent());
                assertFalse(panel.isVisible());
                execute(input, "deletefollowup 1 i/1");
                execute(input, "y");
                assertFalse(panel.isVisible());
                assertEquals(List.of(original), model.getContactFile().getPersonList());
                assertTrue(((TextArea) stage.getScene().lookup("#resultDisplay")).getText()
                        .contains("Simulated unexpected storage failure"));
                execute(input, "deletefollowup 1 i/1");
                confirm.fire();
                assertFalse(panel.isVisible());
                assertEquals(List.of(original), model.getContactFile().getPersonList());
                assertTrue(((TextArea) stage.getScene().lookup("#resultDisplay")).getText()
                        .contains("Unexpected command failure."));
            } finally {
                stage.hide();
            }
        });
    }

    private Stage createWindow(ModelManager model, JsonContactFileStorage storage) {
        Path contacts = testFolder.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        Stage stage = new Stage();
        MainWindow window = new MainWindow(stage, logic, contacts);
        window.fillInnerParts("Loaded");
        window.show();
        return stage;
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
