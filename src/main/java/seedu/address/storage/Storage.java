package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the ContactFile data file.
     */
    Path getContactFilePath();

    /**
     * Returns ContactFile data as a {@link ReadOnlyContactFile}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyContactFile> readContactFile() throws DataLoadingException;

    /**
     * Saves the given {@link ReadOnlyContactFile} to the storage.
     * @param contactFile cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveContactFile(ReadOnlyContactFile contactFile) throws IOException;

    /**
     * Loads startup contacts and returns any completed recovery information.
     *
     * @throws IOException if startup loading or recovery fails
     */
    StartupLoadResult loadForStartup() throws IOException;

    /**
     * Preserves the failed contact file under reports before offering to start anew.
     * @throws IOException if the original file cannot be preserved safely
     */
    StartupResetPlan prepareStartupReset(IOException failure) throws IOException;

    /**
     * Clears the active contact file after the user accepts a prepared reset.
     * @throws IOException if the backup or active file has changed, or safe replacement fails
     */
    StartupLoadResult resetForStartup(StartupResetPlan plan) throws IOException;

}
