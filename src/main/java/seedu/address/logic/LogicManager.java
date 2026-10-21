package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.PendingRequest;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ContactFileParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FieldOrder;
import seedu.address.model.Model;
import seedu.address.model.ModelSnapshot;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final ContactFileParser contactFileParser;

    private PendingRequest pendingRequest;
    private ModelSnapshot pendingSnapshot;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        contactFileParser = new ContactFileParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        ModelSnapshot snapshot = pendingRequest == null ? new ModelSnapshot(model) : pendingSnapshot;
        try {
            CommandResult commandResult;
            if (pendingRequest != null) {
                String response = commandText.trim();
                if (response.equalsIgnoreCase("n") || response.equalsIgnoreCase("cancel")) {
                    snapshot.restore(model);
                    clearPendingRequest();
                    return new CommandResult("Request cancelled. No contacts changed.");
                }
                commandResult = pendingRequest.respond(response, model);
            } else {
                Command command = contactFileParser.parseCommand(commandText);
                commandResult = command.execute(model);
            }

            if (commandResult.getPendingRequest().isPresent()) {
                pendingRequest = commandResult.getPendingRequest().orElseThrow();
                pendingSnapshot = snapshot;
            } else {
                if (snapshot.hasDataChanged(model)) {
                    storage.saveContactFile(model.getContactFile());
                }
                if (snapshot.hasPreferencesChanged(model)) {
                    storage.saveUserPrefs(model.getUserPrefs());
                }
                clearPendingRequest();
            }
            return commandResult;
        } catch (AccessDeniedException e) {
            restoreFailedRequest(snapshot);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            restoreFailedRequest(snapshot);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        } catch (CommandException | ParseException e) {
            restoreFailedRequest(snapshot);
            throw e;
        } catch (RuntimeException e) {
            restoreFailedRequest(snapshot);
            throw e;
        }
    }

    private void restoreFailedRequest(ModelSnapshot snapshot) {
        snapshot.restore(model);
        clearPendingRequest();
    }

    private void clearPendingRequest() {
        pendingRequest = null;
        pendingSnapshot = null;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ReadOnlyObjectProperty<FieldOrder> getFieldOrderProperty() {
        return model.getFieldOrderProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
