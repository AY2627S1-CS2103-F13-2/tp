package seedu.address.ui;

import java.nio.file.Path;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.storage.RecoveryArchive;
import seedu.address.storage.StartupLoadResult;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/address_book_32.png";

    private Logic logic;
    private Path dataFilePath;
    private final StartupLoadResult startupLoadResult;
    private final Runnable exitPlatform;
    private final IntConsumer exitProcess;

    /**
     * Creates the UI with the completed startup loading result.
     */
    public UiManager(Logic logic, StartupLoadResult startupLoadResult) {
        this(logic, startupLoadResult, Platform::exit, System::exit);
    }

    UiManager(Logic logic, StartupLoadResult startupLoadResult, Runnable exitPlatform, IntConsumer exitProcess) {
        this.logic = logic;
        this.dataFilePath = startupLoadResult.sourceFile();
        this.startupLoadResult = startupLoadResult;
        this.exitPlatform = exitPlatform;
        this.exitProcess = exitProcess;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        try {
            //Set the application icon.
            primaryStage.getIcons().add(getImage(ICON_APPLICATION));

            MainWindow mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts(
                StartupMessageFormatter.formatStartup(startupLoadResult));
        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown(primaryStage, "Fatal error during initializing", e);
        }
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    /**
     * Shows an alert dialog on {@code owner} with the given parameters.
     * This method only returns after the user has closed the alert dialog.
     */
    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        createAlert(owner, type, title, headerText, contentText).showAndWait();
    }

    private static Alert createAlert(Stage owner, AlertType type, String title, String headerText, String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        // JavaFX 17 reads the owner's scene when attaching a dialog. Startup may fail before a scene exists.
        if (owner != null && owner.getScene() != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        return alert;
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    private void showFatalErrorDialogAndShutdown(Stage owner, String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(owner, AlertType.ERROR, title, e.getMessage(), e.toString());
        exitPlatform.run();
        exitProcess.accept(1);
    }

    /**
     * Displays a contact-loading failure before the main window is created.
     */
    public static void showStartupError(Stage owner, Exception error) {
        showStartupError(owner, error, Platform::exit);
    }

    static void showStartupError(Stage owner, Exception error, Runnable exitPlatform) {
        showAlertDialogAndWait(owner, AlertType.ERROR,
                "Astra startup failed", "Could not load contacts", error.getMessage());
        exitPlatform.run();
    }

    /**
     * Offers a fresh start after the original contact file has been preserved under reports.
     */
    public static void showStartupReset(Stage owner, Exception error, RecoveryArchive archive, Runnable startAnew) {
        showStartupReset(owner, error, archive, startAnew, Platform::exit);
    }

    static void showStartupReset(Stage owner, Exception error, RecoveryArchive archive,
                                 Runnable startAnew, Runnable exitPlatform) {
        Alert alert = createAlert(owner, AlertType.ERROR, "Astra startup failed", "Could not load contacts",
                StartupMessageFormatter.formatStartupResetOffer(error, archive));
        ButtonType resetButton = new ButtonType("Start anew", ButtonData.OTHER);
        ButtonType exitButton = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(resetButton, exitButton);
        Button resetControl = (Button) alert.getDialogPane().lookupButton(resetButton);
        Button exitControl = (Button) alert.getDialogPane().lookupButton(exitButton);
        resetControl.setDefaultButton(false);
        exitControl.setDefaultButton(true);

        // Keep JavaFX alive while its only dialog closes and the fresh main window is created.
        boolean implicitExit = Platform.isImplicitExit();
        Platform.setImplicitExit(false);
        try {
            if (alert.showAndWait().orElse(exitButton).equals(resetButton)) {
                startAnew.run();
            } else {
                exitPlatform.run();
            }
        } finally {
            Platform.setImplicitExit(implicitExit);
        }
    }
}
