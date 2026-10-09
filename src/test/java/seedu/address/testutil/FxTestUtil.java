package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Shares the headless JavaFX toolkit across tests that create windows and dialogs.
 */
public final class FxTestUtil {

    private FxTestUtil() {
    }

    /**
     * Initializes JavaFX once and keeps it alive between tests.
     */
    public static void initialize() throws Exception {
        CompletableFuture<Void> initialized = new CompletableFuture<>();
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            initialized.complete(null);
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }
        initialized.get(10, TimeUnit.SECONDS);
    }

    /**
     * Runs assertions on the JavaFX thread and propagates failures to JUnit.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
