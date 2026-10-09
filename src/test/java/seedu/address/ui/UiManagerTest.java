package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.RecoveryArchive;
import seedu.address.storage.StartupLoadResult;
import seedu.address.testutil.FxTestUtil;

/**
 * Exercises real windows and modal dialogs using the headless JavaFX toolkit.
 */
@Timeout(20)
class UiManagerTest {

    private static final Path SOURCE = Path.of("data", "addressbook.json").toAbsolutePath().normalize();

    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    void closeWindows() throws Exception {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @Test
    void start_storedContacts_showsContactsPathAndLoadedMessage() throws Exception {
        StartupLoadResult result = new StartupLoadResult(List.of(ALICE, BOB), SOURCE,
                StartupLoadResult.Source.STORED, Optional.empty(), 0);

        assertSuccessfulStartup(result, "Loaded 2 contacts from " + SOURCE + ".");
    }

    @Test
    void start_oneStoredContact_usesSingularMessage() throws Exception {
        StartupLoadResult result = new StartupLoadResult(List.of(ALICE), SOURCE,
                StartupLoadResult.Source.STORED, Optional.empty(), 0);

        assertSuccessfulStartup(result, "Loaded 1 contact from " + SOURCE + ".");
    }

    @Test
    void start_emptyStoredFile_showsEmptyListWithoutSamples() throws Exception {
        StartupLoadResult result = new StartupLoadResult(List.of(), SOURCE,
                StartupLoadResult.Source.STORED, Optional.empty(), 0);

        assertSuccessfulStartup(result, "Loaded 0 contacts from " + SOURCE + ".");
    }

    @Test
    void start_sampleContacts_showsSampleGuidanceAndClearCommand() throws Exception {
        StartupLoadResult result = new StartupLoadResult(SampleDataUtil.getSampleAddressBook().getPersonList(), SOURCE,
                StartupLoadResult.Source.SAMPLE, Optional.empty(), 0);

        assertSuccessfulStartup(result,
                "Welcome to Astra! You're viewing sample contacts to help you explore the app. "
                        + "Type clear to remove all sample contacts and start with an empty contact list.");
    }

    @Test
    void start_recoveredContacts_showsCountsAndArchiveLocations() throws Exception {
        RecoveryArchive archive = new RecoveryArchive(SOURCE.resolveSibling("recovery/original.json"),
                SOURCE.resolveSibling("recovery/report.txt"));
        StartupLoadResult result = new StartupLoadResult(List.of(ALICE), SOURCE,
                StartupLoadResult.Source.RECOVERED, Optional.of(archive), 2);

        assertSuccessfulStartup(result, String.format(
                "Recovered 1 contact(s); skipped 2 invalid or duplicate record(s).\n"
                        + "Active file: %s\nOriginal backup: %s\nRecovery report: %s",
                SOURCE, archive.backupFile(), archive.reportFile()));
    }

    @Test
    void start_resetContacts_showsEmptyListAndPreservedOriginalLocations() throws Exception {
        RecoveryArchive archive = resetArchive();
        StartupLoadResult result = new StartupLoadResult(List.of(), SOURCE,
                StartupLoadResult.Source.RESET, Optional.of(archive), 0);

        assertSuccessfulStartup(result,
                "Started anew with an empty contact list. Your original contacts file is preserved under reports."
                        + "\nOriginal backup: " + archive.backupFile() + "\nReport: " + archive.reportFile());
    }

    @Test
    void showStartupReset_acceptance_runsResetAfterBackupReassurance() throws Exception {
        assertResetChoice("Start anew", List.of("dialog", "reset"));
    }

    @Test
    void showStartupReset_exit_doesNotRunReset() throws Exception {
        assertResetChoice("Exit", List.of("dialog", "exit"));
    }

    @Test
    void showStartupReset_closeDialog_doesNotRunReset() throws Exception {
        assertResetChoice(null, List.of("dialog", "exit"));
    }

    @Test
    void start_windowConstructionFails_showsOriginalErrorBeforeShutdown() throws Exception {
        assertFatalStartup(true);
    }

    @Test
    void start_fillingWindowFails_showsOriginalErrorBeforeShutdown() throws Exception {
        assertFatalStartup(false);
    }

    @Test
    void showStartupError_loadingFails_showsDetailsBeforeExiting() throws Exception {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            List<String> events = new ArrayList<>();
            String details = "No valid contacts could be recovered.\nOriginal backup: original.json\n"
                    + "Recovery report: report.txt";
            CompletableFuture<Void> inspected = dismissNextDialog(pane -> {
                Stage dialog = (Stage) pane.getScene().getWindow();
                assertNull(dialog.getOwner(), "An owner with no scene cannot be attached to a JavaFX 17 dialog.");
                assertEquals("Astra startup failed", dialog.getTitle());
                assertEquals("Could not load contacts", pane.getHeaderText());
                assertEquals(details, pane.getContentText());
                assertFalse(owner.isShowing());
                assertTrue(events.isEmpty(), "Shutdown must wait for the user to dismiss the error.");
                events.add("dialog");
            });

            UiManager.showStartupError(owner, new IOException(details), () -> events.add("exit"));

            assertDialogInspected(inspected);
            assertEquals(List.of("dialog", "exit"), events);
            assertTrue(Window.getWindows().isEmpty());
        });
    }

    @Test
    void showStartupError_ownerHasScene_attachesDialogToOwner() throws Exception {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            List<String> events = new ArrayList<>();
            CompletableFuture<Void> inspected = dismissNextDialog(pane -> {
                assertSame(owner, ((Stage) pane.getScene().getWindow()).getOwner());
                assertEquals("Cannot read contacts.", pane.getContentText());
                events.add("dialog");
            });

            UiManager.showStartupError(owner, new IOException("Cannot read contacts."), () -> events.add("exit"));

            assertDialogInspected(inspected);
            assertEquals(List.of("dialog", "exit"), events);
        });
    }

    @Test
    void showStartupError_noOwner_showsStandaloneDialog() throws Exception {
        runOnFxThread(() -> {
            List<String> events = new ArrayList<>();
            CompletableFuture<Void> inspected = dismissNextDialog(pane -> {
                assertNull(((Stage) pane.getScene().getWindow()).getOwner());
                assertEquals("Cannot read contacts.", pane.getContentText());
                events.add("dialog");
            });

            UiManager.showStartupError(null, new IOException("Cannot read contacts."), () -> events.add("exit"));

            assertDialogInspected(inspected);
            assertEquals(List.of("dialog", "exit"), events);
        });
    }

    private void assertSuccessfulStartup(StartupLoadResult result, String expectedMessage) throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            List<String> exits = new ArrayList<>();
            UiManager manager = new UiManager(
                    new LogicStub(result.contacts()), result, () -> exits.add("platform"),
                    status -> exits.add("process: " + status));

            manager.start(stage);

            assertTrue(stage.isShowing());
            assertFalse(stage.getIcons().isEmpty());
            assertFalse(stage.getIcons().get(0).isError());
            TextArea feedback = assertInstanceOf(TextArea.class, stage.getScene().lookup("#resultDisplay"));
            assertEquals(expectedMessage, feedback.getText());
            ListView<?> contacts = assertInstanceOf(ListView.class, stage.getScene().lookup("#personListView"));
            assertEquals(result.contacts(), contacts.getItems());
            Label path = assertInstanceOf(Label.class, stage.getScene().lookup("#saveLocationStatus"));
            assertEquals(SOURCE.toString(), path.getText());
            assertNotNull(stage.getScene().lookup("#commandTextField"));
            assertTrue(exits.isEmpty());
        });
    }

    private void assertFatalStartup(boolean failDuringConstruction) throws Exception {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            List<String> events = new ArrayList<>();
            RuntimeException failure = new IllegalStateException("Simulated UI initialization failure.");
            Logic logic = new LogicStub(List.of()) {
                @Override
                public GuiSettings getGuiSettings() {
                    if (failDuringConstruction) {
                        throw failure;
                    }
                    return super.getGuiSettings();
                }

                @Override
                public ObservableList<Person> getFilteredPersonList() {
                    throw failure;
                }
            };
            StartupLoadResult result = new StartupLoadResult(List.of(), SOURCE,
                    StartupLoadResult.Source.STORED, Optional.empty(), 0);
            UiManager manager = new UiManager(logic, result, () -> events.add("platform"),
                    status -> events.add("process: " + status));
            CompletableFuture<Void> inspected = dismissNextDialog(pane -> {
                Stage dialog = (Stage) pane.getScene().getWindow();
                assertSame(owner, dialog.getOwner());
                assertEquals("Fatal error during initializing", dialog.getTitle());
                assertEquals(failure.getMessage(), pane.getHeaderText());
                assertEquals(failure.toString(), pane.getContentText());
                assertEquals(UiManager.ALERT_DIALOG_PANE_FIELD_ID, pane.getId());
                assertTrue(pane.getStylesheets().contains("view/DarkTheme.css"));
                assertTrue(events.isEmpty(), "Shutdown must wait for the user to dismiss the error.");
                events.add("dialog");
            });

            manager.start(owner);

            assertDialogInspected(inspected);
            assertEquals(List.of("dialog", "platform", "process: 1"), events);
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FxTestUtil.runOnFxThread(action);
    }

    private static void assertDialogInspected(CompletableFuture<Void> inspected) {
        assertTrue(inspected.isDone(), "Expected the dialog to wait for dismissal before returning.");
        inspected.join();
    }

    private static CompletableFuture<Void> dismissNextDialog(Consumer<DialogPane> assertions) {
        return dismissNextDialog(assertions, ButtonType.OK.getText());
    }

    private static CompletableFuture<Void> dismissNextDialog(Consumer<DialogPane> assertions, String selectedButton) {
        CompletableFuture<Void> inspected = new CompletableFuture<>();
        Platform.runLater(() -> {
            List<Window> dialogs = Window.getWindows().stream()
                    .filter(window -> window.getScene().getRoot() instanceof DialogPane).toList();
            try {
                assertEquals(1, dialogs.size(), "Expected one visible error dialog.");
                DialogPane pane = (DialogPane) dialogs.get(0).getScene().getRoot();
                assertions.accept(pane);
                inspected.complete(null);
            } catch (Throwable error) {
                inspected.completeExceptionally(error);
            } finally {
                for (Window window : dialogs) {
                    DialogPane pane = (DialogPane) window.getScene().getRoot();
                    if (selectedButton == null) {
                        window.hide();
                    } else {
                        ButtonType selected = pane.getButtonTypes().stream()
                                .filter(button -> button.getText().equals(selectedButton)).findFirst().orElseThrow();
                        Button confirmButton = (Button) pane.lookupButton(selected);
                        confirmButton.fire();
                    }
                }
            }
        });
        return inspected;
    }

    private void assertResetChoice(String selectedButton, List<String> expectedEvents) throws Exception {
        runOnFxThread(() -> {
            List<String> events = new ArrayList<>();
            RecoveryArchive archive = resetArchive();
            CompletableFuture<Void> inspected = dismissNextDialog(pane -> {
                assertEquals("Could not load contacts", pane.getHeaderText());
                assertTrue(pane.getContentText().contains("preserved unchanged under reports"));
                assertTrue(pane.getContentText().contains(archive.backupFile().toString()));
                assertTrue(pane.getContentText().contains(archive.reportFile().toString()));
                assertTrue(pane.getContentText().contains("clear the active address book"));
                assertEquals(List.of("Start anew", "Exit"), pane.getButtonTypes().stream()
                        .map(ButtonType::getText).toList());
                Button reset = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
                Button exit = (Button) pane.lookupButton(pane.getButtonTypes().get(1));
                assertFalse(reset.isDefaultButton());
                assertTrue(exit.isDefaultButton());
                assertTrue(exit.isCancelButton());
                assertTrue(events.isEmpty());
                assertFalse(Platform.isImplicitExit());
                events.add("dialog");
            }, selectedButton);

            Runnable resetAction = () -> events.add("reset");
            Runnable exitAction = () -> events.add("exit");
            UiManager.showStartupReset(
                    new Stage(), new IOException("Invalid contact file."), archive, resetAction, exitAction);

            assertDialogInspected(inspected);
            assertEquals(expectedEvents, events);
            assertFalse(Platform.isImplicitExit(), "The prior toolkit exit policy must be restored.");
        });
    }

    private static RecoveryArchive resetArchive() {
        return new RecoveryArchive(SOURCE.resolveSibling("reports/incident/addressbook.original.json"),
                SOURCE.resolveSibling("reports/incident/report.txt"));
    }

    /**
     * Supplies contacts without connecting UI tests to persistent storage.
     */
    private static class LogicStub implements Logic {
        private final ObservableList<Person> contacts;

        LogicStub(List<Person> contacts) {
            this.contacts = FXCollections.observableArrayList(contacts);
        }

        @Override
        public CommandResult execute(String commandText) {
            throw new AssertionError("Startup must not execute a command.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return contacts;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return new GuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("Startup must not change preferences.");
        }
    }
}
