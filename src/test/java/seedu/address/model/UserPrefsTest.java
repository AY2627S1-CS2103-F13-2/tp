package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class UserPrefsTest {

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void commandAliases_setAndRemove() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setCommandAlias("fn", "find /notes");
        assertEquals(Map.of("fn", "find /notes"), userPrefs.getCommandAliases());
        assertThrows(UnsupportedOperationException.class, () -> userPrefs.getCommandAliases().clear());

        UserPrefs copy = new UserPrefs(userPrefs);
        assertEquals(userPrefs, copy);
        assertEquals(userPrefs.hashCode(), copy.hashCode());

        assertTrue(userPrefs.equals(userPrefs));
        assertFalse(userPrefs.equals(null));
        assertFalse(userPrefs.equals(1));

        copy.removeCommandAlias("fn");
        assertNotEquals(userPrefs, copy);
        assertEquals(Map.of(), copy.getCommandAliases());
        assertThrows(NullPointerException.class, () -> copy.setCommandAliases(null));
    }

    @Test
    public void toStringMethod() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setCommandAliases(Map.of("fn", "find /notes"));
        assertEquals("Gui Settings : " + userPrefs.getGuiSettings() + "\nCommand Aliases : {fn=find /notes}",
                userPrefs.toString());
    }

}
