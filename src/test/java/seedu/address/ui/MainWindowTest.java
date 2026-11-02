package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

class MainWindowTest {

    @Test
    void savedWindowPosition_validScreenBounds_remainsUnchanged() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);
        Rectangle2D leftMonitor = new Rectangle2D(-1920, 0, 1920, 1080);

        assertEquals(new Point2D(100, 100),
                MainWindow.getReachableWindowPosition(100, 100, 1000, 700, List.of(primary)));
        assertEquals(new Point2D(-1700, 100),
                MainWindow.getReachableWindowPosition(-1700, 100, 1000, 700, List.of(primary, leftMonitor)));
    }

    @Test
    void savedWindowPosition_offscreen_recentersOnPrimaryScreen() {
        Rectangle2D primary = new Rectangle2D(0, 0, 1920, 1080);

        assertEquals(new Point2D(460, 190),
                MainWindow.getReachableWindowPosition(100000, 100000, 1000, 700, List.of(primary)));
        assertEquals(new Point2D(460, 190),
                MainWindow.getReachableWindowPosition(-1700, 100, 1000, 700, List.of(primary)));
    }
}
