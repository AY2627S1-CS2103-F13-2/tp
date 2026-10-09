package seedu.address.ui;

import java.nio.file.Path;

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
}
