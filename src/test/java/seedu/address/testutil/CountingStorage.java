package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.model.ReadOnlyContactFile;
import seedu.address.storage.JsonContactFileStorage;

/**
 * A contact file storage that counts save attempts and can simulate a save failure before
 * anything is written to disk.
 */
public class CountingStorage extends JsonContactFileStorage {
    private int saveCount;
    private boolean shouldFail;

    /**
     * Creates a storage that reads and writes the contact file at {@code filePath}.
     */
    public CountingStorage(Path filePath) {
        super(filePath);
    }

    /**
     * Counts the attempt, then throws an {@code IOException} if failures are switched on, or
     * saves {@code contactFile} to disk otherwise.
     */
    @Override
    public void saveContactFile(ReadOnlyContactFile contactFile) throws IOException {
        saveCount++;
        if (shouldFail) {
            throw new IOException("Simulated save failure");
        }
        super.saveContactFile(contactFile);
    }

    /**
     * Returns the number of save attempts since creation or the last reset, including failed ones.
     */
    public int getSaveCount() {
        return saveCount;
    }

    /**
     * Resets the number of save attempts to zero.
     */
    public void resetSaveCount() {
        saveCount = 0;
    }

    /**
     * Sets whether later save attempts should fail before writing to disk.
     */
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
}
