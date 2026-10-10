package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Signals that another instance owns the contact file; recovery must not be attempted.
 */
public class StorageInUseException extends IOException {
    /**
     * Identifies the contact file that is already in use.
     */
    public StorageInUseException(Path source) {
        super("Another Astra instance is using the contact file: " + source
                + "\nClose that instance and launch Astra again.");
    }
}
