package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.util.SampleDataUtil;

/**
 * Loads startup contacts and safely applies recoverable repairs.
 */
final class ContactStartupLoader {

    private static final Logger logger =
            LogsCenter.getLogger(ContactStartupLoader.class);

    private final ContactRecoveryParser parser = new ContactRecoveryParser();
    private final ArchiveWriter archiveWriter;
    private final AtomicReplacement replacement;

    ContactStartupLoader() {
        this(new RecoveryArchiveWriter()::write, (temporary, target) ->
                Files.move(temporary, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING));
    }

    ContactStartupLoader(ArchiveWriter archiveWriter,
                         AtomicReplacement replacement) {
        this.archiveWriter = archiveWriter;
        this.replacement = replacement;
    }

    /**
     * Loads saved contacts, sample contacts, or recovered contacts.
     *
     * @throws IOException if loading or recovery cannot finish safely
     */
    public StartupLoadResult load(Path sourceFile) throws IOException {
        Path source = sourceFile.toAbsolutePath().normalize();
        byte[] originalBytes;

        try {
            originalBytes = Files.readAllBytes(source);
        } catch (NoSuchFileException e) {
            return new StartupLoadResult(
                    SampleDataUtil.getSampleAddressBook().getPersonList(),
                    source, StartupLoadResult.Source.SAMPLE, Optional.empty(), 0);
        }

        ContactRecoveryResult result = parser.parse(originalBytes);

        if (result.getSkippedCount() == 0) {
            return new StartupLoadResult(
                    result.recoveredContacts(), source,
                    StartupLoadResult.Source.STORED, Optional.empty(), 0);
        }

        return recover(source, originalBytes, result);
    }

    private StartupLoadResult recover(Path source, byte[] originalBytes,
                                      ContactRecoveryResult result) throws IOException {
        RecoveryArchive archive = archiveWriter.write(source, originalBytes, result);

        if (result.getRecoveredCount() == 0) {
            throw new IOException(
                    "No valid contacts could be recovered. The active file was not replaced.\n"
                            + describeArchive(archive));
        }

        try {
            replaceActiveFile(source, originalBytes, result);
        } catch (IOException e) {
            throw new IOException(
                    "Could not install recovered contacts: " + e.getMessage() + "\n"
                            + describeArchive(archive), e);
        }

        return new StartupLoadResult(
                result.recoveredContacts(), source,
                StartupLoadResult.Source.RECOVERED,
                Optional.of(archive), result.getSkippedCount());
    }

    private void replaceActiveFile(Path source, byte[] originalBytes,
                                   ContactRecoveryResult result) throws IOException {
        Path temporary = Files.createTempFile(
                source.getParent(), "addressbook-recovery-", ".json");

        try {
            writeRecoveredFile(temporary, result);
            verifyRecoveredFile(temporary, result);
            verifyOriginalUnchanged(source, originalBytes);
            replacement.replace(temporary, source);
        } finally {
            removeTemporaryFile(temporary);
        }
    }

    private void writeRecoveredFile(Path temporary,
                                    ContactRecoveryResult result) throws IOException {
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(result.recoveredContacts());

        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), temporary);
    }

    private void verifyRecoveredFile(Path temporary,
                                     ContactRecoveryResult expected) throws IOException {
        ContactRecoveryResult actual = parser.parse(Files.readAllBytes(temporary));

        if (actual.getSkippedCount() != 0
                || !actual.recoveredContacts().equals(expected.recoveredContacts())) {
            throw new IOException("The recovered contact file failed verification.");
        }
    }

    private void verifyOriginalUnchanged(Path source, byte[] originalBytes) throws IOException {
        if (!Arrays.equals(originalBytes, Files.readAllBytes(source))) {
            throw new IOException(
                    "The contact file changed during recovery. Restart Astra and try again.");
        }
    }

    private void removeTemporaryFile(Path temporary) {
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException e) {
            logger.warning("Could not remove recovery temporary file " + temporary
                    + ": " + e.getMessage());
        }
    }

    private String describeArchive(RecoveryArchive archive) {
        return "Original backup: " + archive.backupFile()
                + "\nRecovery report: " + archive.reportFile();
    }

    /**
     * Preserves the original bytes and records validation results before replacement.
     */
    @FunctionalInterface
    interface ArchiveWriter {
        RecoveryArchive write(Path source, byte[] originalBytes, ContactRecoveryResult result) throws IOException;
    }

    /**
     * Replaces a file atomically or throws without using a non-atomic fallback.
     */
    @FunctionalInterface
    interface AtomicReplacement {
        void replace(Path temporary, Path target) throws IOException;
    }
}
