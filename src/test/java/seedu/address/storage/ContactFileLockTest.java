package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

class ContactFileLockTest {
    @TempDir
    private Path folder;

    @Test
    @Timeout(20)
    void acquire_otherProcessOwnsFile_thenCrashReleasesOwnership() throws Exception {
        Path source = folder.resolve("addressbook.json");
        String classpath = Path.of(LockProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(ContactFileLock.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classpath, LockProcess.class.getName(), source.toString()).start();
        try {
            assertEquals("locked", process.inputReader(StandardCharsets.UTF_8).readLine());
            assertThrows(StorageInUseException.class, () -> ContactFileLock.acquire(source));
            process.destroyForcibly();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
            try (ContactFileLock restarted = ContactFileLock.acquire(source)) {
                assertFalse(Files.exists(source));
            }
        } finally {
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Separate JVM used to exercise operating-system locking and crash cleanup.
     */
    public static class LockProcess {
        /**
         * Holds ownership until terminated by the parent test.
         */
        public static void main(String[] args) throws Exception {
            try (ContactFileLock lock = ContactFileLock.acquire(Path.of(args[0]))) {
                System.out.println("locked");
                System.out.flush();
                System.in.read();
            }
        }
    }

    @Test
    void acquire_missingContactFile_excludesSecondOwnerAndAllowsRestart() throws Exception {
        Path source = folder.resolve("nested/addressbook.json");
        try (ContactFileLock first = ContactFileLock.acquire(source)) {
            assertFalse(Files.exists(source));
            assertThrows(StorageInUseException.class, () -> ContactFileLock.acquire(source));
            assertThrows(StorageInUseException.class, () ->
                    ContactFileLock.acquire(source.getParent().resolve("../nested/addressbook.json")));
        }
        try (ContactFileLock restarted = ContactFileLock.acquire(source)) {
            assertTrue(Files.exists(source.resolveSibling("addressbook.json.lock")));
        }
    }

    @Test
    void acquire_differentFiles_canRunTogether() throws Exception {
        try (ContactFileLock first = ContactFileLock.acquire(folder.resolve("first.json"));
                ContactFileLock second = ContactFileLock.acquire(folder.resolve("second.json"))) {
            assertFalse(Files.exists(folder.resolve("first.json")));
            assertFalse(Files.exists(folder.resolve("second.json")));
        }
    }

    @Test
    void acquire_contactFileReplaced_lockRemainsHeld() throws Exception {
        Path source = folder.resolve("addressbook.json");
        Files.writeString(source, "original");
        try (ContactFileLock first = ContactFileLock.acquire(source)) {
            Files.delete(source);
            Files.writeString(source, "replacement");
            assertThrows(StorageInUseException.class, () -> ContactFileLock.acquire(source));
        }
    }
}
