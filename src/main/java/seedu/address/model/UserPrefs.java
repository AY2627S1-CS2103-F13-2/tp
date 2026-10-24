package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import seedu.address.commons.core.GuiSettings;

/**
 * Represents User's preferences.
 */
public class UserPrefs implements ReadOnlyUserPrefs {

    private GuiSettings guiSettings = new GuiSettings();
    private FieldOrder fieldOrder = FieldOrder.DEFAULT;
    private Map<String, String> commandAliases = new TreeMap<>();

    /**
     * Creates a {@code UserPrefs} with default values.
     */
    public UserPrefs() {}

    /**
     * Creates a {@code UserPrefs} with the prefs in {@code userPrefs}.
     */
    public UserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        setGuiSettings(userPrefs.getGuiSettings());
        setFieldOrder(userPrefs.getFieldOrder());
        setCommandAliases(userPrefs.getCommandAliases());
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        this.guiSettings = guiSettings;
    }

    public FieldOrder getFieldOrder() {
        return fieldOrder;
    }

    public void setFieldOrder(FieldOrder fieldOrder) {
        this.fieldOrder = requireNonNull(fieldOrder);
    }

    @Override
    public Map<String, String> getCommandAliases() {
        // A preferences file with "commandAliases": null is treated as having no shortcuts. Entries are
        // checked against the alias rules when Logic starts (see AliasCommandParser#validAliases).
        return commandAliases == null ? Map.of() : Collections.unmodifiableMap(commandAliases);
    }

    /**
     * Replaces all command shortcuts with those in {@code commandAliases}.
     */
    public void setCommandAliases(Map<String, String> commandAliases) {
        requireNonNull(commandAliases);
        this.commandAliases = new TreeMap<>(commandAliases);
    }

    /**
     * Saves {@code command} as the expansion of the shortcut {@code shortcut}, replacing any earlier expansion.
     */
    public void setCommandAlias(String shortcut, String command) {
        requireNonNull(shortcut);
        requireNonNull(command);
        Map<String, String> updated = new TreeMap<>(getCommandAliases());
        updated.put(shortcut, command);
        commandAliases = updated;
    }

    /**
     * Removes the shortcut {@code shortcut}. Does nothing if there is no such shortcut.
     */
    public void removeCommandAlias(String shortcut) {
        requireNonNull(shortcut);
        Map<String, String> updated = new TreeMap<>(getCommandAliases());
        updated.remove(shortcut);
        commandAliases = updated;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UserPrefs otherUserPrefs)) {
            return false;
        }

        return guiSettings.equals(otherUserPrefs.guiSettings) && fieldOrder.equals(otherUserPrefs.fieldOrder)
                && getCommandAliases().equals(otherUserPrefs.getCommandAliases());
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiSettings, fieldOrder, getCommandAliases());
    }

    @Override
    public String toString() {
        return "Gui Settings : " + guiSettings + "; Field Order : " + fieldOrder
                + "; Command Aliases : " + getCommandAliases();
    }

}
