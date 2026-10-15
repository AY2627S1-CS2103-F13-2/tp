package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of ContactFile data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonContactFileStorage contactFileStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given contact file and user prefs storage.
     */
    public StorageManager(JsonContactFileStorage contactFileStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.contactFileStorage = contactFileStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ ContactFile methods ==============================

    @Override
    public Path getContactFilePath() {
        return contactFileStorage.getContactFilePath();
    }

    @Override
    public Optional<ReadOnlyContactFile> readContactFile() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + contactFileStorage.getContactFilePath());
        return contactFileStorage.readContactFile();
    }

    @Override
    public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
        logger.fine("Attempting to write to data file: " + contactFileStorage.getContactFilePath());
        contactFileStorage.saveContactFile(contactFile);
    }

    @Override
    public StartupLoadResult loadForStartup() throws IOException {
        return contactFileStorage.loadForStartup();
    }

    @Override
    public StartupResetPlan prepareStartupReset(IOException failure) throws IOException {
        return contactFileStorage.prepareStartupReset(failure);
    }

    @Override
    public StartupLoadResult resetForStartup(StartupResetPlan plan) throws IOException {
        return contactFileStorage.resetForStartup(plan);
    }
}
