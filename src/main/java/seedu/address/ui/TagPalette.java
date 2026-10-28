package seedu.address.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Retains one distinct chip colour for each tag throughout a workspace's lifetime.
 */
final class TagPalette {
    private static final double HUE_STEP = 137.50776405003785;
    private final Map<String, String> styles = new HashMap<>();

    /**
     * Returns the existing colour or allocates the next hue without cycling a fixed palette.
     */
    String styleFor(String tagName) {
        return styles.computeIfAbsent(tagName, name -> {
            double hue = styles.size() * HUE_STEP % 360;
            return String.format(Locale.ROOT,
                    "-fx-background-color: hsb(%.8f, 24%%, 96%%); -fx-text-fill: hsb(%.8f, 78%%, 34%%);", hue, hue);
        });
    }
}
