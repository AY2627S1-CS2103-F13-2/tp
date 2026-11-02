package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.ContactFile;
import seedu.address.model.FieldOrder;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

@Timeout(20)
class FieldOrderUiTest {
    private static final FieldOrder CUSTOM = new FieldOrder(
            List.of("followups", "phone", "email", "introducer", "name", "tags", "company", "role", "linkedin",
                    "interactions"));
    private static final List<String> CUSTOM_ROWS =
            List.of("followUps", "phone", "email", "introducer", "nameRow", "tags", "company", "role", "linkedin");
    private static final Person WITH_ACTION = ALICE.appendFollowUp(
            new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING));

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
    void card_customOrder_arrangesAllRowsAndKeepsIndexWithName() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(WITH_ACTION, 3, CUSTOM);
            assertEquals(CUSTOM_ROWS, rowIds(card.getRoot()));
            assertEquals("3. ", ((Label) card.getRoot().lookup("#id")).getText());
            assertEquals(ALICE.getName().fullName, ((Label) card.getRoot().lookup("#name")).getText());
            assertTrue(((Label) card.getRoot().lookup("#followUps")).isWrapText());
        });
    }

    @Test
    void card_missingOptionalFields_hidesRowsWithoutLayoutGaps() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(NAME_ONLY, 1, CUSTOM);
            VBox fields = (VBox) card.getRoot().lookup("#fields");
            assertEquals(List.of("nameRow"), fields.getChildren().stream()
                    .filter(Node::isManaged).map(Node::getId).toList());
            for (Node row : fields.getChildren()) {
                if (!row.getId().equals("nameRow")) {
                    assertFalse(row.isManaged());
                    assertFalse(row.isVisible());
                }
            }
        });
    }

    @Test
    void panel_observableOrder_refreshesExistingCardsWithoutChangingContacts() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            ObservableList<Person> people = FXCollections.observableArrayList(WITH_ACTION, NAME_ONLY);
            ReadOnlyObjectWrapper<FieldOrder> order = new ReadOnlyObjectWrapper<>(FieldOrder.DEFAULT);
            PersonListPanel panel = new PersonListPanel(people, order.getReadOnlyProperty());
            new Scene(panel.getRoot(), 500, 600);
            panel.getRoot().resize(500, 600);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertEquals("nameRow", rowIds(panel.getRoot()).getFirst());
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            list.getSelectionModel().select(1);
            order.set(CUSTOM);
            panel.getRoot().layout();
            assertEquals(CUSTOM_ROWS, rowIds(panel.getRoot()));
            assertEquals(1, list.getSelectionModel().getSelectedIndex());
            assertEquals(NAME_ONLY, list.getSelectionModel().getSelectedItem());
            assertEquals(List.of(WITH_ACTION, NAME_ONLY), people);
        });
    }

    @Test
    void window_orderCommand_refreshesCardsAndLastViewedDetailsAndRollsBackOnFailure() throws Exception {
        Path contactsPath = testFolder.resolve("contacts.json");
        Path prefsPath = testFolder.resolve("preferences.json");
        ContactFile contacts = new ContactFile();
        contacts.addPerson(WITH_ACTION);
        ModelManager model = new ModelManager(contacts, new UserPrefs());
        JsonContactFileStorage contactStorage = new JsonContactFileStorage(contactsPath);
        contactStorage.saveContactFile(contacts);
        byte[] contactBytes = Files.readAllBytes(contactsPath);
        new JsonUserPrefsStorage(prefsPath).saveUserPrefs(model.getUserPrefs());
        FailingPreferences prefs = new FailingPreferences(prefsPath);
        LogicManager logic = new LogicManager(model, new StorageManager(contactStorage, prefs));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, contactsPath);
            window.fillInnerParts("Loaded");
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
            execute(input, "view 1");
            assertEquals("Viewing Alice Pauline.", output.getText());
            execute(input, "order " + CUSTOM);
            stage.getScene().getRoot().layout();
            assertEquals(CUSTOM, model.getFieldOrder());
            assertFalse(output.getText().contains(Messages.format(WITH_ACTION, CUSTOM)));
            Label detailName = (Label) stage.getScene().lookup("#detailName");
            assertEquals(ALICE.getName().fullName, detailName.getText());
            assertEquals(CUSTOM_ROWS, rowIds(stage.getScene().getRoot()));
            assertEquals(1, prefs.saveCount);
            prefs.shouldFail = true;
            execute(input, "order " + FieldOrder.DEFAULT);
            stage.getScene().getRoot().layout();
            assertEquals(CUSTOM, model.getFieldOrder());
            assertEquals(CUSTOM_ROWS, rowIds(stage.getScene().getRoot()));
            assertTrue(output.getText().contains("Could not save data"));
            assertFalse(output.getText().contains(Messages.format(WITH_ACTION, CUSTOM)));
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(2, prefs.saveCount);
        });
        assertEquals(CUSTOM, prefs.readUserPrefs().orElseThrow().getFieldOrder());
        assertArrayEquals(contactBytes, Files.readAllBytes(contactsPath));
    }

    @Test
    void window_orderSaveFailure_restoresSelectedFollowUpView() throws Exception {
        Path contactsPath = testFolder.resolve("follow-up-contacts.json");
        Path prefsPath = testFolder.resolve("follow-up-preferences.json");
        ContactFile contacts = new ContactFile();
        contacts.addPerson(WITH_ACTION);
        ModelManager model = new ModelManager(contacts, new UserPrefs());
        JsonContactFileStorage contactStorage = new JsonContactFileStorage(contactsPath);
        contactStorage.saveContactFile(contacts);
        new JsonUserPrefsStorage(prefsPath).saveUserPrefs(model.getUserPrefs());
        FailingPreferences prefs = new FailingPreferences(prefsPath);
        LogicManager logic = new LogicManager(model, new StorageManager(contactStorage, prefs));

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, contactsPath);
            window.fillInnerParts("Loaded");
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");

            execute(input, "followups");
            execute(input, "1");
            prefs.shouldFail = true;
            execute(input, "order " + CUSTOM);

            assertEquals(FieldOrder.DEFAULT, model.getFieldOrder());
            assertTrue(output.getText().contains("Could not save data"));
            assertFalse(personListRoot(stage).isVisible());
            assertTrue(stage.getScene().lookup("#followUpListPanel").isVisible());
            ListView<?> followUpList = (ListView<?>) stage.getScene().lookup("#followUpListPanel").lookup(".list-view");
            assertEquals(new FollowUpEntry(WITH_ACTION, WITH_ACTION.getFollowUps().getFirst()),
                    followUpList.getSelectionModel().getSelectedItem());
            assertTrue(((Label) stage.getScene().lookup("#selectedFollowUpText")).getText().contains("Send portfolio"));
        });
    }

    @Test
    void window_externalOrderNotification_refreshesDisplayedDetailsImmediately() throws Exception {
        ContactFile contacts = new ContactFile();
        contacts.addPerson(WITH_ACTION);
        ModelManager model = new ModelManager(contacts, new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, testFolder.resolve("contacts.json"));
            window.fillInnerParts("Loaded");
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
            execute(input, "view 1");
            ObservableList<Person> binding = model.getFilteredPersonList();
            model.setFieldOrder(CUSTOM);
            assertEquals("Viewing Alice Pauline.", output.getText());
            assertEquals(CUSTOM_ROWS, rowIds(stage.getScene().getRoot()));
            assertSame(binding, model.getFilteredPersonList());
            assertEquals(List.of(WITH_ACTION), binding);
        });
    }

    @Test
    void window_namedView_refreshesDetailsWhenFieldOrderChanges() throws Exception {
        ContactFile contacts = new ContactFile();
        contacts.addPerson(WITH_ACTION);
        ModelManager model = new ModelManager(contacts, new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(
                new JsonContactFileStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, testFolder.resolve("contacts.json"));
            window.fillInnerParts("Loaded");
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea output = (TextArea) stage.getScene().lookup("#resultDisplay");
            execute(input, "view Alice Pauline");
            assertEquals("Viewing Alice Pauline.", output.getText());
            ObservableList<Person> binding = model.getFilteredPersonList();
            model.setFieldOrder(CUSTOM);
            assertEquals("Viewing Alice Pauline.", output.getText());
            assertEquals(CUSTOM_ROWS, rowIds(stage.getScene().getRoot()));
            assertSame(binding, model.getFilteredPersonList());
            assertEquals(List.of(WITH_ACTION), binding);
        });
        assertFalse(Files.exists(testFolder.resolve("contacts.json")));
        assertFalse(Files.exists(testFolder.resolve("preferences.json")));
    }

    private static List<String> rowIds(Node root) {
        VBox rows = (VBox) root.lookup("#fields");
        return rows.getChildren().stream().map(Node::getId).toList();
    }

    private static void execute(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static Node personListRoot(Stage stage) {
        return ((StackPane) stage.getScene().lookup("#personListPanelPlaceholder")).getChildren().get(0);
    }

    private static final class FailingPreferences extends JsonUserPrefsStorage {
        private boolean shouldFail;
        private int saveCount;

        private FailingPreferences(Path path) {
            super(path);
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs prefs) throws IOException {
            saveCount++;
            if (shouldFail) {
                throw new IOException("Simulated preference failure");
            }
            super.saveUserPrefs(prefs);
        }
    }
}
