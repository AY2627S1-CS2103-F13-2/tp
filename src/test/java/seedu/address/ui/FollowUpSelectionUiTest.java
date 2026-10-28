package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.FieldOrder;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

@Timeout(20)
class FollowUpSelectionUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void window_selectHiddenContact_displaysFollowUpAndFullContactWithoutSaving() throws Exception {
        Person alice = ALICE.appendFollowUp(
                new FollowUp(7, "Call", FollowUp.Status.PENDING, LocalDate.of(2000, 2, 29)));
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(BENSON);
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        Path contacts = testFolder.resolve("contacts.json");
        Path preferences = testFolder.resolve("preferences.json");
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(contacts), new JsonUserPrefsStorage(preferences)));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, contacts);
                window.fillInnerParts("Loaded");
                window.show();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                execute(input, "view Alice");
                assertEquals("Viewing Alice Pauline.", output.getText());
                execute(input, "followups");
                assertTrue(stage.getScene().lookup("#followUpListPanel").isVisible());
                assertEquals("Pending follow-ups (1):", output.getText());
                execute(input, "list");
                assertEquals("Listed all persons.", output.getText());
                assertEquals(2, model.getFilteredPersonList().size());
                execute(input, "find Benson");
                execute(input, "followups");
                execute(input, "99");
                assertTrue(output.getText().contains("Enter a number from the displayed follow-ups."));
                execute(input, "1");
                assertEquals("Viewing follow-up #7 for Alice Pauline.", output.getText());
                assertTrue(output.getText().contains(ALICE.getName().fullName));
                assertFalse(output.getText().contains(ALICE.getPhone().orElseThrow().value));
                Label phone = (Label) stage.getScene().lookup("#phoneValue");
                assertEquals(ALICE.getPhone().orElseThrow().value, phone.getText());
                assertTrue(((Label) stage.getScene().lookup("#selectedFollowUpText")).getText().contains("Call"));
                assertEquals(List.of(BENSON), model.getFilteredPersonList());
                FieldOrder custom = new FieldOrder(
                        List.of("followups", "phone", "name", "tags", "company", "role", "email", "linkedin"));
                model.setFieldOrder(custom);
                assertEquals("Viewing follow-up #7 for Alice Pauline.", output.getText());
                assertTrue(((Label) stage.getScene().lookup("#selectedFollowUpText")).getText().contains("Call"));
                execute(input, "not-a-command");
                assertFalse(output.getText().contains(Messages.format(alice, custom)));
                execute(input, "view Benson");
                model.setFieldOrder(FieldOrder.DEFAULT);
                assertEquals("Viewing Benson Meier.", output.getText());
                assertFalse(output.getText().contains(alice.getName().fullName));
            } finally {
                stage.hide();
            }
        });
        assertFalse(Files.exists(contacts));
        assertFalse(Files.exists(preferences));
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
