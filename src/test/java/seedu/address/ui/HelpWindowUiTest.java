package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javafx.stage.Stage;
import seedu.address.testutil.FxTestUtil;

@Timeout(20)
class HelpWindowUiTest {
    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void focus_minimizedWindow_restoresExistingHelpWindow() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            HelpWindow help = new HelpWindow();
            Stage stage = help.getRoot();
            try {
                help.show();
                stage.setIconified(true);
                assertTrue(stage.isIconified());
                help.focus();
                assertFalse(stage.isIconified());
                assertTrue(help.isShowing());
                help.focus();
                assertTrue(help.isShowing());
            } finally {
                help.hide();
            }
        });
    }
}
