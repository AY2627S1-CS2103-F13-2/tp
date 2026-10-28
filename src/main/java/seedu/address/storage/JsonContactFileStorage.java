package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ContactFile;
import seedu.address.model.ReadOnlyContactFile;

/**
 * A class to access ContactFile data stored as a JSON file on the hard disk.
 */
public class JsonContactFileStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonContactFileStorage.class);

    private final Path filePath;

    /**
     * Creates storage for the given active contact file.
     */
    public JsonContactFileStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getContactFilePath() {
        return filePath;
    }

    /**
     * Returns ContactFile data as a {@link ReadOnlyContactFile}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyContactFile> readContactFile() throws DataLoadingException {
        return readContactFile(filePath);
    }

    /**
     * Similar to {@link #readContactFile()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyContactFile> readContactFile(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableContactFile> jsonContactFile = JsonUtil.readJsonFile(
                filePath, JsonSerializableContactFile.class);
        if (!jsonContactFile.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonContactFile.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyContactFile} to the storage.
     *
     * @param contactFile cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
        saveContactFile(contactFile, filePath);
    }

    /**
     * Similar to {@link #saveContactFile(ReadOnlyContactFile)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveContactFile(ReadOnlyContactFile contactFile, Path filePath) throws IOException {
        requireNonNull(contactFile);
        requireNonNull(filePath);

        ContactFile expected = new ContactFile(contactFile);
        Path target = filePath.toAbsolutePath().normalize();
        Files.createDirectories(target.getParent());
        logger.fine("Writing and validating a temporary contact snapshot");
        AtomicFileWriter.write(target, ".astra-save-",
                temporary -> writeCandidate(expected, temporary),
                temporary -> verifyCandidate(expected, temporary), this::replaceActiveFile);
        logger.fine("Atomically replaced active contact file with " + expected.getPersonList().size()
                + " validated contacts");
    }

    void writeCandidate(ReadOnlyContactFile contactFile, Path temporary) throws IOException {
        JsonUtil.saveJsonFile(new JsonSerializableContactFile(contactFile), temporary);
    }

    void verifyCandidate(ReadOnlyContactFile contactFile, Path temporary) throws IOException {
        try {
            ReadOnlyContactFile reloaded = readContactFile(temporary)
                    .orElseThrow(() -> new IOException("Temporary contact file is missing."));
            if (!new ContactFile(contactFile).equals(reloaded)) {
                throw new IOException("Temporary contact file does not match the requested data.");
            }
        } catch (DataLoadingException e) {
            throw new IOException("Could not validate temporary contact file.", e);
        }
    }

    void replaceActiveFile(Path temporary, Path target) throws IOException {
        AtomicFileWriter.replace(temporary, target);
    }

    /**
     * Loads contacts for startup, recovering individual invalid records when possible.
     *
     * @throws IOException if startup loading or recovery fails
     */
    public StartupLoadResult loadForStartup() throws IOException {
        return new ContactStartupLoader().load(filePath);
    }

    /**
     * Creates an unchanged backup and failure report before offering a fresh start.
     */
    public StartupResetPlan prepareStartupReset(IOException failure) throws IOException {
        return new ContactStartupLoader().prepareStartupReset(filePath, failure);
    }

    /**
     * Safely replaces the active file with an empty contact file after confirmation.
     */
    public StartupLoadResult resetForStartup(StartupResetPlan plan) throws IOException {
        return new ContactStartupLoader().resetForStartup(filePath, plan);
    }

}
