package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Preserves original contact bytes and writes a recovery report.
 */
final class RecoveryArchiveWriter {

    private static final DateTimeFormatter DIRECTORY_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmmss");

    private final Clock clock;

    RecoveryArchiveWriter() {
        this(Clock.systemDefaultZone());
    }

    RecoveryArchiveWriter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Creates a unique recovery directory containing a backup and report.
     *
     * @throws IOException if either file cannot be created
     */
    public RecoveryArchive write(Path sourceFile, byte[] originalBytes,
                                 ContactRecoveryResult result) throws IOException {
        Path absoluteSource = sourceFile.toAbsolutePath().normalize();
        return writeArchive(absoluteSource, originalBytes, "recovery", (backup, timestamp) ->
                formatReport(absoluteSource, backup, timestamp, result));
    }

    /**
     * Preserves an unreadable contact document under reports before offering to start anew.
     */
    public RecoveryArchive writeStartupFailure(Path sourceFile, byte[] originalBytes,
                                               String failureReason) throws IOException {
        Path source = sourceFile.toAbsolutePath().normalize();
        return writeArchive(source, originalBytes, "reports", (backup, timestamp) ->
                "Astra startup failure and reset report\n"
                        + "Time: " + timestamp + "\nSource: " + source
                        + "\nOriginal backup: " + backup
                        + "\n\nStartup failure: " + failureReason
                        + "\n\nThe original contact file has been preserved unchanged.\n"
                        + "This report is created before the user chooses whether to start anew.\n"
                        + "The active address book is cleared only after the user chooses Start anew\n"
                        + "and Astra reports that it has started with an empty contact list.\n"
                        + "Keep the original backup unchanged; it may contain details you can restore manually.\n");
    }

    private RecoveryArchive writeArchive(Path source, byte[] originalBytes, String directoryName,
                                          BiFunction<Path, OffsetDateTime, String> reportFormatter) throws IOException {
        OffsetDateTime timestamp = OffsetDateTime.now(clock);

        Path recoveryRoot = source.getParent().resolve(directoryName);
        Files.createDirectories(recoveryRoot);

        String prefix = DIRECTORY_TIME.format(timestamp) + "_";
        Path incidentDirectory = Files.createTempDirectory(recoveryRoot, prefix);

        Path backup = incidentDirectory.resolve("addressbook.original.json");
        Path report = incidentDirectory.resolve("report.txt");

        try {
            Files.write(backup, originalBytes,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            if (!Arrays.equals(originalBytes, Files.readAllBytes(backup))) {
                throw new IOException("The original contact backup failed verification.");
            }

            Files.writeString(report,
                    reportFormatter.apply(backup, timestamp),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IOException(
                    "Could not finish the recovery archive at " + incidentDirectory, e);
        }

        return new RecoveryArchive(backup, report);
    }

    private String formatReport(Path source, Path backup, OffsetDateTime timestamp,
                                ContactRecoveryResult result) {
        StringBuilder report = new StringBuilder();

        report.append("Astra contact recovery validation report\n");
        report.append("Time: ").append(timestamp).append('\n');
        report.append("Source: ").append(source).append('\n');
        report.append("Original backup: ").append(backup).append("\n\n");

        report.append("Records inspected: ")
                .append(result.getRecoveredCount() + result.getSkippedCount())
                .append('\n');
        report.append("Valid contacts available for recovery: ")
                .append(result.getRecoveredCount()).append('\n');
        report.append("Skipped records: ")
                .append(result.getSkippedCount()).append("\n\n");

        report.append("Skipped records use positions from the original persons array.\n");
        for (SkippedContact skipped : result.skippedContacts()) {
            report.append("- Record ").append(skipped.recordNumber()).append(": ")
                    .append(skipped.reason().replaceAll("\\R", " "))
                    .append('\n');
        }

        report.append("\nThis report is written before replacing the active file.\n");
        report.append("Recovery is complete only when Astra reports successful recovery.\n");

        if (result.getRecoveredCount() == 0) {
            report.append("No valid contacts were found. Astra will not replace the active file.\n");
        }

        report.append("\nKeep the original backup unchanged.\n");
        report.append("After successful recovery, correct the skipped details and add ")
                .append("those contacts using Astra.\n");

        return report.toString();
    }
}
