package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
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
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

@Timeout(20)
class NumericContactSelectionUiTest {
    @TempDir
    public Path folder;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void window_numericSelection_showsChoicesBeforeDetailsOrConfirmation() throws Exception {
        ModelManager model = new ModelManager();
        List<String> names = List.of("7 Eleven", "Beta", "Carol", "David", "Eve", "Frank", "Zara");
        for (int index = 0; index < names.size(); index++) {
            model.addPerson(new PersonBuilder().withName(names.get(index)).withPhone("9000000" + (index + 1))
                    .withEmail("person" + index + "@example.com").build());
        }
        Path contacts = folder.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonContactFileStorage(contacts),
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, contacts);
                window.fillInnerParts("Loaded");
                window.show();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
                TextArea preview = (TextArea) stage.getScene().lookup("#confirmationPreview");
                execute(input, "view 7");
                assertTrue(output.getText().contains("2 contacts found:"));
                assertTrue(output.getText().contains("7 Eleven"));
                assertTrue(output.getText().contains("Zara"));
                execute(input, "99");
                assertTrue(output.getText().contains("Enter a number from the displayed candidates."));
                execute(input, "2");
                assertEquals("Viewing Zara.", output.getText());
                assertEquals("Zara", ((Label) stage.getScene().lookup("#detailName")).getText());
                assertFalse(Files.exists(contacts));
                execute(input, "list");
                execute(input, "delete 7");
                assertFalse(stage.getScene().lookup("#confirmationPanel").isVisible());
                execute(input, "1");
                assertTrue(stage.getScene().lookup("#confirmationPanel").isVisible());
                assertTrue(preview.getText().contains("7 Eleven"));
                String prompt = preview.getText();
                execute(input, "invalid");
                assertEquals(prompt, preview.getText());
                execute(input, "cancel");
                assertFalse(stage.getScene().lookup("#confirmationPanel").isVisible());
                assertEquals(7, model.getFilteredPersonList().size());
                assertFalse(Files.exists(contacts));
            } finally {
                stage.hide();
            }
        });
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
