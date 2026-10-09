package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StartupLoadResult;
import seedu.address.storage.StartupResetPlan;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private final Path userPrefsFilePath;
    private final Path addressBookFilePath;
    private IOException startupFailure;
    private StartupResetPlan startupResetPlan;
    private UserPrefs startupPrefs;

    public MainApp() {
        this(USER_PREFS_FILE_PATH, ADDRESS_BOOK_FILE_PATH);
    }

    MainApp(Path userPrefsFilePath, Path addressBookFilePath) {
        this.userPrefsFilePath = userPrefsFilePath;
        this.addressBookFilePath = addressBookFilePath;
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsFilePath);
        startupPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(addressBookFilePath);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        try {
            StartupLoadResult loadResult = storage.loadForStartup();
            initializeComponents(loadResult);
        } catch (IOException e) {
            startupFailure = e;
            logger.severe("Contact startup failed: " + StringUtil.getDetails(e));
            try {
                startupResetPlan = storage.prepareStartupReset(e);
            } catch (IOException archiveFailure) {
                startupFailure = new IOException(e.getMessage()
                        + "\n\nStarting anew is unavailable because the original contact file could not be "
                        + "preserved safely under reports: " + archiveFailure.getMessage()
                        + "\nAstra has not cleared your address book.", e);
            }
        }
    }

    private void initializeComponents(StartupLoadResult loadResult) {
        model = initModelManager(loadResult, startupPrefs);
        logic = new LogicManager(model, storage);
        ui = createUi(logic, loadResult);
    }

    /**
     * Creates the command interface after contact loading or an explicit reset succeeds.
     */
    protected Ui createUi(Logic initializedLogic, StartupLoadResult loadResult) {
        return new UiManager(initializedLogic, loadResult);
    }

    /**
     * Creates the model from a completed startup loading result.
     */
    private Model initModelManager(StartupLoadResult loadResult, ReadOnlyUserPrefs userPrefs) {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(loadResult.contacts());
        return new ModelManager(addressBook, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        if (startupFailure != null) {
            showStartupError(primaryStage, startupFailure);
            return;
        }

        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    /**
     * Presents a loading failure without starting the command interface.
     */
    protected void showStartupError(Stage primaryStage, IOException failure) {
        if (startupResetPlan == null) {
            UiManager.showStartupError(primaryStage, failure);
        } else {
            UiManager.showStartupReset(
                    primaryStage, failure, startupResetPlan.archive(), () -> startAnew(primaryStage));
        }
    }

    /**
     * Opens an empty address book after the user explicitly chooses to start anew.
     */
    protected void startAnew(Stage primaryStage) {
        try {
            StartupLoadResult reset = storage.resetForStartup(startupResetPlan);
            startupFailure = null;
            startupResetPlan = null;
            initializeComponents(reset);
            ui.start(primaryStage);
        } catch (IOException e) {
            startupResetPlan = null;
            startupFailure = e;
            showStartupError(primaryStage, e);
        }
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");

        if (model == null) {
            return;
        }

        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
