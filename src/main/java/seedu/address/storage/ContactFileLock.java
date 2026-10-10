package seedu.address.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Owns a contact file for one application lifetime, including atomic file replacements.
 * The stable sibling lock file is deliberately never deleted.
 */
public final class ContactFileLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private ContactFileLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Acquires exclusive ownership without waiting for another instance to exit.
     */
    public static ContactFileLock acquire(Path contactFile) throws IOException {
        Path source = contactFile.toAbsolutePath().normalize();
        Files.createDirectories(source.getParent());
        source = Files.exists(source) ? source.toRealPath()
                : source.getParent().toRealPath().resolve(source.getFileName());
        Path lockFile = source.resolveSibling(source.getFileName() + ".lock");
        FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                throw new StorageInUseException(source);
            }
            return new ContactFileLock(channel, lock);
        } catch (OverlappingFileLockException e) {
            StorageInUseException failure = new StorageInUseException(source);
            closeAfterFailure(channel, failure);
            throw failure;
        } catch (IOException | RuntimeException e) {
            closeAfterFailure(channel, e);
            throw e;
        }
    }

    private static void closeAfterFailure(FileChannel channel, Exception failure) {
        try {
            channel.close();
        } catch (IOException closeFailure) {
            failure.addSuppressed(closeFailure);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (lock.isValid()) {
                lock.release();
            }
        } finally {
            channel.close();
        }
    }
}
