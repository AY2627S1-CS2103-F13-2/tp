package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StartupLoadResult;
import seedu.address.testutil.FxTestUtil;
import seedu.address.ui.Ui;

class MainAppTest {

    @TempDir
    private Path testFolder;

    private Path contactsFile;
    private Path prefsFile;
    private RecordingMainApp app;

    @BeforeEach
    void setUp() {
        contactsFile = testFolder.resolve("addressbook.json");
        prefsFile = testFolder.resolve("preferences.json");
        app = new RecordingMainApp(prefsFile, contactsFile);
    }

    @Test
    void init_missingContacts_initializesSamplesWithoutSavingThem() throws Exception {
        app.init();

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertFalse(Files.exists(contactsFile));
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsFile).readUserPrefs().orElseThrow());
    }

    @Test
    void init_storedContacts_preservesStoredOrderAndPreferences() throws Exception {
        Files.writeString(contactsFile, "{\"persons\": [{\"name\": \"Charlie\"}, {\"name\": \"Alice\"}]}");
        byte[] original = Files.readAllBytes(contactsFile);
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(900, 700, 30, 40));
        new JsonUserPrefsStorage(prefsFile).saveUserPrefs(prefs);

        app.init();

        assertEquals(List.of("Charlie", "Alice"), app.model.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals(prefs, app.model.getUserPrefs());
        assertArrayEquals(original, Files.readAllBytes(contactsFile));
    }

    @Test
    void init_emptyStoredContacts_doesNotSubstituteSamples() throws Exception {
        Files.writeString(contactsFile, "{\"persons\": []}");

        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
    }

    @Test
    void init_recoverableContacts_initializesModelFromRepairedFile() throws Exception {
        Files.writeString(contactsFile, "{\"persons\": [{\"name\": \"Alice\"}, {\"name\": \"\"}]}");

        app.init();

        assertEquals(1, app.model.getAddressBook().getPersonList().size());
        assertEquals("Alice", app.model.getAddressBook().getPersonList().get(0).getName().fullName);
        assertEquals(app.model.getAddressBook(), app.storage.readAddressBook().orElseThrow());
        assertNotNull(app.ui);
    }

    @Test
    void init_invalidPreferences_fallsBackToDefaults() throws Exception {
        for (String json : List.of("{", "null", "{\"guiSettings\": null}")) {
            Files.writeString(prefsFile, json);
            RecordingMainApp candidate = new RecordingMainApp(prefsFile, contactsFile);

            candidate.init();

            assertEquals(new UserPrefs(), candidate.model.getUserPrefs(), json);
            assertEquals(new UserPrefs(), new JsonUserPrefsStorage(prefsFile).readUserPrefs().orElseThrow(), json);
        }
    }

    @Test
    void init_preferencesCannotBeSaved_stillLoadsContacts() throws Exception {
        Files.createDirectory(prefsFile);

        assertDoesNotThrow(app::init);

        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertNotNull(app.ui);
        assertTrue(Files.isDirectory(prefsFile));
    }

    @Test
    void start_successfulStartup_startsUi() throws Exception {
        app.init();
        AtomicBoolean started = new AtomicBoolean();
        app.ui = stage -> started.set(true);

        app.start(null);

        assertTrue(started.get());
        assertNull(app.displayedFailure);
    }

    @Test
    void start_unparseableContacts_showsFailureWithoutCommandInterface() throws Exception {
        for (String json : List.of("{", "null", "", "{\"persons\": []} {}")) {
            Files.writeString(contactsFile, json);
            byte[] original = Files.readAllBytes(contactsFile);
            RecordingMainApp candidate = new RecordingMainApp(prefsFile, contactsFile);

            candidate.init();
            assertNull(candidate.model);
            assertNull(candidate.logic);
            assertNull(candidate.ui);

            candidate.start(null);
            assertNotNull(candidate.displayedFailure);
            assertDoesNotThrow(candidate::stop);
            assertArrayEquals(original, Files.readAllBytes(contactsFile));
            assertFalse(Files.exists(testFolder.resolve("recovery")));
        }
    }

    @Test
    void start_noValidContacts_showsArchiveLocationsAndPreservesFile() throws Exception {
        Files.writeString(contactsFile, "{\"persons\": [{\"name\": \"\"}]}");
        byte[] original = Files.readAllBytes(contactsFile);

        app.init();
        app.start(null);

        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        assertTrue(app.displayedFailure.getMessage().contains("Original backup:"));
        assertTrue(app.displayedFailure.getMessage().contains("Recovery report:"));
        assertArrayEquals(original, Files.readAllBytes(contactsFile));
    }

    @Test
    void stop_initializedApp_savesCurrentPreferencesWithoutSavingSamples() throws Exception {
        app.init();
        GuiSettings settings = new GuiSettings(1100, 800, 50, 60);
        app.model.setGuiSettings(settings);

        app.stop();

        assertEquals(settings, new JsonUserPrefsStorage(prefsFile).readUserPrefs().orElseThrow().getGuiSettings());
        assertFalse(Files.exists(contactsFile));
    }

    @Test
    void stop_preferenceWriteFails_doesNotThrow() throws Exception {
        app.init();
        Files.delete(prefsFile);
        Files.createDirectory(prefsFile);

        assertDoesNotThrow(app::stop);

        assertTrue(Files.isDirectory(prefsFile));
        assertFalse(Files.exists(contactsFile));
    }

    @Test
    void stop_beforeInitialization_doesNotAccessStorage() {
        assertDoesNotThrow(new MainApp()::stop);
    }

    @Test
    void startAnew_malformedFile_opensEmptyAddressBookAndKeepsPreferencesAndOriginal() throws Exception {
        Files.writeString(contactsFile, "{bad JSON\r\n");
        byte[] original = Files.readAllBytes(contactsFile);
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(900, 700, 30, 40));
        new JsonUserPrefsStorage(prefsFile).saveUserPrefs(prefs);
        ResettingMainApp candidate = new ResettingMainApp(prefsFile, contactsFile);
        candidate.init();
        assertNull(candidate.model);
        assertArrayEquals(original, Files.readAllBytes(contactsFile));

        candidate.startAnew(null);

        assertTrue(candidate.uiStarted);
        assertEquals(StartupLoadResult.Source.RESET, candidate.uiResult.source());
        assertTrue(candidate.model.getAddressBook().getPersonList().isEmpty());
        assertEquals(prefs, candidate.model.getUserPrefs());
        assertArrayEquals(original,
                Files.readAllBytes(candidate.uiResult.recoveryArchive().orElseThrow().backupFile()));
        assertTrue(candidate.storage.loadForStartup().contacts().isEmpty());
        assertEquals(StartupLoadResult.Source.STORED, candidate.storage.loadForStartup().source());
    }

    @Test
    void start_failureWithoutConfirmation_keepsOriginalAndCreatesReportsCopy() throws Exception {
        Files.writeString(contactsFile, "{");
        byte[] original = Files.readAllBytes(contactsFile);
        app.init();

        app.start(null);

        assertNotNull(app.displayedFailure);
        assertNull(app.model);
        assertArrayEquals(original, Files.readAllBytes(contactsFile));
        assertArrayEquals(original, Files.readAllBytes(findReportsBackup()));
    }

    @Test
    void start_cannotPreserveOriginal_explainsWhyResetIsUnavailable() throws Exception {
        Files.writeString(contactsFile, "{");
        Files.writeString(testFolder.resolve("reports"), "Existing file.");
        byte[] original = Files.readAllBytes(contactsFile);
        app.init();

        app.start(null);

        assertTrue(app.displayedFailure.getMessage().contains("Starting anew is unavailable"));
        assertTrue(app.displayedFailure.getMessage().contains("Astra has not cleared your address book"));
        assertNull(app.model);
        assertArrayEquals(original, Files.readAllBytes(contactsFile));
    }

    @Test
    void startAnew_fileChangesAfterOffer_showsErrorWithoutOpeningUi() throws Exception {
        Files.writeString(contactsFile, "{");
        ResettingMainApp candidate = new ResettingMainApp(prefsFile, contactsFile);
        candidate.init();
        Files.writeString(contactsFile, "{\"persons\": [{\"name\": \"Bob\"}]}");
        byte[] updated = Files.readAllBytes(contactsFile);

        candidate.startAnew(null);

        assertFalse(candidate.uiStarted);
        assertNull(candidate.model);
        assertTrue(candidate.displayedFailure.getMessage().contains("changed during recovery"));
        assertArrayEquals(updated, Files.readAllBytes(contactsFile));
    }

    @Test
    void start_userChoosesStartAnew_opensRealEmptyWindowWithReportsReassurance() throws Exception {
        Files.writeString(contactsFile, "{invalid JSON\r\n");
        byte[] original = Files.readAllBytes(contactsFile);
        MainApp candidate = new MainApp(prefsFile, contactsFile);
        candidate.init();
        Path backup = findReportsBackup();
        FxTestUtil.initialize();

        FxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            CompletableFuture<Void> inspected = new CompletableFuture<>();
            Platform.runLater(() -> {
                Window dialogWindow = Window.getWindows().stream()
                        .filter(window -> window.getScene().getRoot() instanceof DialogPane).findFirst().orElseThrow();
                DialogPane pane = (DialogPane) dialogWindow.getScene().getRoot();
                try {
                    assertTrue(pane.getContentText().contains("preserved unchanged under reports"));
                    assertTrue(pane.getContentText().contains(backup.toString()));
                    assertFalse(stage.isShowing());
                    inspected.complete(null);
                } catch (Throwable error) {
                    inspected.completeExceptionally(error);
                } finally {
                    ButtonType reset = pane.getButtonTypes().stream()
                            .filter(button -> button.getText().equals("Start anew")).findFirst().orElseThrow();
                    Button resetControl = (Button) pane.lookupButton(reset);
                    resetControl.fire();
                }
            });
            try {
                candidate.start(stage);

                assertTrue(inspected.isDone());
                inspected.join();
                assertTrue(stage.isShowing());
                assertTrue(candidate.model.getAddressBook().getPersonList().isEmpty());
                ListView<?> contacts = (ListView<?>) stage.getScene().lookup("#personListView");
                assertTrue(contacts.getItems().isEmpty());
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertTrue(feedback.getText().contains("Started anew with an empty contact list"));
                assertTrue(feedback.getText().contains(backup.toString()));
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });

        assertArrayEquals(original, Files.readAllBytes(backup));
        assertEquals(StartupLoadResult.Source.STORED, candidate.storage.loadForStartup().source());
        assertTrue(candidate.storage.loadForStartup().contacts().isEmpty());
    }

    private Path findReportsBackup() throws IOException {
        try (Stream<Path> paths = Files.walk(testFolder.resolve("reports"))) {
            return paths.filter(path -> path.getFileName().toString().equals("addressbook.original.json"))
                    .findFirst().orElseThrow();
        }
    }

    /**
     * Records fresh-start UI creation without opening a window in storage integration tests.
     */
    private static class ResettingMainApp extends RecordingMainApp {
        private boolean uiStarted;
        private StartupLoadResult uiResult;

        ResettingMainApp(Path prefsFile, Path contactsFile) {
            super(prefsFile, contactsFile);
        }

        @Override
        protected Ui createUi(Logic initializedLogic, StartupLoadResult result) {
            uiResult = result;
            return stage -> uiStarted = true;
        }
    }

    /**
     * Records failure presentation without starting the JavaFX toolkit or a modal dialog.
     */
    private static class RecordingMainApp extends MainApp {
        protected IOException displayedFailure;

        RecordingMainApp(Path prefsFile, Path contactsFile) {
            super(prefsFile, contactsFile);
        }

        @Override
        protected void showStartupError(Stage stage, IOException failure) {
            displayedFailure = failure;
        }
    }
}
