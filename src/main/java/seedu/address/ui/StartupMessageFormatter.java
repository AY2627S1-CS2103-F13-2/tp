package seedu.address.ui;

import java.nio.file.Path;

import seedu.address.storage.RecoveryArchive;
import seedu.address.storage.StartupLoadResult;

/**
 * Formats user-facing startup messages.
 */
public final class StartupMessageFormatter {

    private StartupMessageFormatter() {
        // Utility class.
    }

    /**
     * Returns feedback for contacts successfully loaded from storage.
     */
    public static String formatLoadedContacts(int contactCount, Path sourceFile) {
        String contactLabel = contactCount == 1 ? "contact" : "contacts";
        Path absolutePath = sourceFile.toAbsolutePath().normalize();

        return String.format(
                "Loaded %d %s from %s.",
                contactCount, contactLabel, absolutePath);
    }

    /**
     * Returns guidance for new users viewing sample contacts.
     */
    public static String formatSampleContacts() {
        return "Welcome to Astra! You're viewing sample contacts to help you explore the app. "
                + "Type clear to remove all sample contacts and start with an empty contact list.";
    }

    /**
     * Returns feedback for the completed startup operation.
     */
    public static String formatStartup(StartupLoadResult result) {
        return switch (result.source()) {
            case STORED -> formatLoadedContacts(
                    result.contacts().size(), result.sourceFile());
            case SAMPLE -> formatSampleContacts();
            case RECOVERED -> formatRecoveredContacts(result);
        };
    }

    private static String formatRecoveredContacts(StartupLoadResult result) {
        RecoveryArchive archive = result.recoveryArchive().orElseThrow();

        return String.format(
                "Recovered %d contact(s); skipped %d invalid or duplicate record(s).%n"
                        + "Active file: %s%n"
                        + "Original backup: %s%n"
                        + "Recovery report: %s",
                result.contacts().size(),
                result.skippedCount(),
                result.sourceFile(),
                archive.backupFile(),
                archive.reportFile());
    }
}
