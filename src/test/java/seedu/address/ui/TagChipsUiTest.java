package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

class TagChipsUiTest {
    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void separateChips_keepTextAndColourWhenSwitchingContacts() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            Scene scene = new Scene(panel.getRoot(), 400, 600);
            scene.getStylesheets().addAll(PersonDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm(),
                    PersonDetailsPanel.class.getResource("/view/AstraTheme.css").toExternalForm());
            panel.getRoot().applyCss();
            Person first = new PersonBuilder(ALICE).withTags("friends", "work", "Aa", "BB").build();
            panel.showPerson(first, FieldOrder.DEFAULT);
            FlowPane chips = (FlowPane) panel.getRoot().lookup("#tagChips");
            panel.getRoot().applyCss();
            assertEquals(new Insets(3, 9, 3, 9), ((Label) chips.getChildren().getFirst()).getPadding());
            assertEquals(List.of("Aa", "BB", "friends", "work"), chips.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
            assertEquals(4, new HashSet<>(chips.getChildren().stream().map(node -> node.getStyle()).toList()).size());
            String friendsColour = chips.getChildren().get(2).getStyle();
            panel.showPerson(new PersonBuilder(ALICE).withTags("friends", "newTag").build(), FieldOrder.DEFAULT);
            FlowPane next = (FlowPane) panel.getRoot().lookup("#tagChips");
            assertEquals(friendsColour, next.getChildren().getFirst().getStyle());
            assertEquals(Set.of("[Aa]", "[BB]", "[friends]", "[work]"),
                    first.getTags().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()));
        });
    }

    @Test
    void manyTags_wrapAndDoNotReuseAShortPalette() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            String[] tags = IntStream.range(0, 32).mapToObj(index -> "Tag" + index).toArray(String[]::new);
            panel.showPerson(new PersonBuilder(ALICE).withTags(tags).build(), FieldOrder.DEFAULT);
            new Scene(panel.getRoot(), 340, 600);
            panel.getRoot().resize(340, 600);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            FlowPane chips = (FlowPane) panel.getRoot().lookup("#tagChips");
            assertEquals(32, chips.getChildren().size());
            assertEquals(32, new HashSet<>(chips.getChildren().stream().map(node -> node.getStyle()).toList()).size());
            assertTrue(chips.getChildren().getLast().getLayoutY() > chips.getChildren().getFirst().getLayoutY());
            panel.showPerson(new PersonBuilder(ALICE).withTags().build(), FieldOrder.DEFAULT);
            assertFalse(panel.getRoot().lookup("#tags").isManaged());
            assertFalse(panel.getRoot().lookup("#tags").isVisible());
        });
    }
}
