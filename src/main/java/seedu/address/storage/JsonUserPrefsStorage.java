package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Reads preferences and saves validated snapshots through atomic file replacement.
 */
public class JsonUserPrefsStorage {

    private final Path filePath;

    /**
     * Creates preference storage at the given path.
     */
    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns preferences, or an empty result when the file is absent.
     *
     * @throws DataLoadingException if the file cannot be read or validated.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Returns validated preferences from the specified file.
     *
     * @throws DataLoadingException if the file cannot be read or validated.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        Optional<UserPrefs> prefs = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        if (prefs.isPresent() && (prefs.get().getGuiSettings() == null || prefs.get().getFieldOrder() == null)) {
            throw new DataLoadingException(new IOException("User preferences must contain non-null settings."));
        }
        return prefs;
    }

    /**
     * Saves a snapshot after verifying a sibling temporary file, preserving the active file on failure.
     *
     * @throws IOException if writing, validation, or atomic replacement fails.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        UserPrefs expected = new UserPrefs(requireNonNull(userPrefs));
        Path target = requireNonNull(filePath).toAbsolutePath().normalize();
        AtomicFileWriter.write(target, ".astra-prefs-",
                temporary -> writeCandidate(expected, temporary),
                temporary -> verifyCandidate(expected, temporary), this::replaceActiveFile);
    }

    void writeCandidate(UserPrefs expected, Path temporary) throws IOException {
        JsonUtil.saveJsonFile(expected, temporary);
    }

    void verifyCandidate(UserPrefs expected, Path temporary) throws IOException {
        try {
            UserPrefs reloaded = readUserPrefs(temporary)
                    .orElseThrow(() -> new IOException("Temporary preference file is missing."));
            if (!expected.equals(reloaded)) {
                throw new IOException("Temporary preferences do not match the requested settings.");
            }
        } catch (DataLoadingException e) {
            throw new IOException("Could not validate temporary preferences.", e);
        }
    }

    void replaceActiveFile(Path temporary, Path target) throws IOException {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
