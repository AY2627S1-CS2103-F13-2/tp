package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.FieldOrder;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

class FieldOrderStartupTest {
    @TempDir
    public Path testFolder;

    @Test
    void startupAndShutdown_preserveOrderWithoutChangingContacts() throws Exception {
        Path contacts = testFolder.resolve("contacts.json");
        Path prefs = testFolder.resolve("preferences.json");
        Files.writeString(contacts, "{\"persons\":[{\"name\":\"Alice\"}]}");
        byte[] original = Files.readAllBytes(contacts);
        FieldOrder order = new FieldOrder(
                List.of("phone", "email", "name", "tags", "company", "role", "linkedin", "followups"));
        UserPrefs settings = new UserPrefs();
        settings.setFieldOrder(order);
        new JsonUserPrefsStorage(prefs).saveUserPrefs(settings);
        MainApp first = new MainApp(prefs, contacts);
        try {
            first.init();
            assertEquals(order, first.model.getFieldOrder());
        } finally {
            first.stop();
        }
        MainApp second = new MainApp(prefs, contacts);
        try {
            second.init();
            assertEquals(order, second.logic.getFieldOrderProperty().get());
            assertArrayEquals(original, Files.readAllBytes(contacts));
        } finally {
            second.stop();
        }
    }
}
