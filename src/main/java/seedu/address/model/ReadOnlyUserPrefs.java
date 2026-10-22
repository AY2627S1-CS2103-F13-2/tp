package seedu.address.model;

import java.util.Map;

import seedu.address.commons.core.GuiSettings;

/**
 * Unmodifiable view of user prefs.
 */
public interface ReadOnlyUserPrefs {

    GuiSettings getGuiSettings();

    FieldOrder getFieldOrder();

    /**
     * Returns the command shortcuts, mapping each shortcut name to the command it stands for.
     */
    Map<String, String> getCommandAliases();

}
