package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

@Timeout(20)
class CancelSelectionUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void window_cancelledRequest_keepsPreviouslySelectedContact() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        model.addPerson(DANIEL);
        Path contacts = testFolder.resolve("contacts.json");
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(contacts), new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, contacts);
                window.fillInnerParts("Loaded");
                window.show();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");

                execute(input, "view 2");
                execute(input, "delete Alice");
                execute(input, "cancel");
                assertEquals(BENSON, window.getPersonListPanel().selectedPersonProperty().get());
                assertEquals(BENSON.getPhone().orElseThrow().value, shownPhone(stage));

                // a name matching several contacts narrows the list while a candidate is chosen
                execute(input, "view 3");
                execute(input, "delete Meier");
                execute(input, "1");
                execute(input, "n");
                assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredPersonList());
                assertEquals(DANIEL, window.getPersonListPanel().selectedPersonProperty().get());
                assertEquals(DANIEL.getPhone().orElseThrow().value, shownPhone(stage));
            } finally {
                stage.hide();
            }
        });
        assertFalse(Files.exists(contacts));
    }

    private static String shownPhone(Stage stage) {
        // the details panel rebuilds its labels whenever it shows a contact, so look the label up each time
        return ((Label) stage.getScene().lookup("#phoneValue")).getText();
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
