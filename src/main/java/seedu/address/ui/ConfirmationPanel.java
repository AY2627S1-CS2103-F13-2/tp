package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.ConfirmationRequest;
import seedu.address.logic.commands.PendingRequest;

/**
 * Displays the active confirmation and sends replies through the same logic path as typed input.
 */
public class ConfirmationPanel extends UiPart<Region> {
    private static final String FXML = "ConfirmationPanel.fxml";

    private final Consumer<String> replyHandler;
    private long revision;

    @FXML
    private TextArea confirmationPreview;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    /**
     * Creates a hidden panel using the shared command execution callback.
     */
    public ConfirmationPanel(Consumer<String> replyHandler) {
        super(FXML);
        this.replyHandler = requireNonNull(replyHandler);
        showRequest(null);
    }

    /**
     * Shows confirmations only; selection requests and completed operations hide the panel.
     */
    public void showRequest(PendingRequest request) {
        long currentRevision = ++revision;
        boolean isConfirmation = request instanceof ConfirmationRequest;
        getRoot().setVisible(isConfirmation);
        getRoot().setManaged(isConfirmation);
        confirmationPreview.setText(isConfirmation ? request.getPrompt() : "");
        confirmationPreview.positionCaret(0);
        confirmButton.setOnAction(event -> sendReply("y", currentRevision));
        cancelButton.setOnAction(event -> sendReply("cancel", currentRevision));
    }

    private void sendReply(String response, long expectedRevision) {
        // Ignore queued events from a completed or replaced preview.
        if (expectedRevision != revision || !getRoot().isVisible()) {
            return;
        }
        showRequest(null);
        replyHandler.accept(response);
    }
}
