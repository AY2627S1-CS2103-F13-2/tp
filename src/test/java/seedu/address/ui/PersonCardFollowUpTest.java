package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

@Timeout(20)
class PersonCardFollowUpTest {
    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void card_history_showsEveryActionInStoredOrderWithStatusAndWrapping() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Person person = new PersonBuilder(ALICE).withFollowUps(
                    new FollowUp(8, "Call", FollowUp.Status.COMPLETED),
                    new FollowUp(2, "Send a long portfolio description that should wrap within the contact card",
                            FollowUp.Status.PENDING)).build();
            Label label = (Label) new PersonCard(person, 1).getRoot().lookup("#followUps");
            assertEquals("Follow-ups:\n" + Messages.formatFollowUps(person), label.getText());
            assertTrue(label.isVisible());
            assertTrue(label.isManaged());
            assertTrue(label.isWrapText());
        });
    }

    @Test
    void card_withoutActions_hidesAndUnmanagesFollowUpRow() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Label label = (Label) new PersonCard(ALICE, 1).getRoot().lookup("#followUps");
            assertFalse(label.isVisible());
            assertFalse(label.isManaged());
        });
    }

    @Test
    void card_withIntroducer_placesIntroducerInRequestedOrder() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Person person = new PersonBuilder(ALICE).withIntroducer("Jane Doe").build();
            FieldOrder order = new FieldOrder(List.of("introducer", "name", "tags", "company", "role", "phone",
                    "email", "linkedin", "followups", "interactions"));
            PersonCard card = new PersonCard(person, 1, order);
            VBox fields = (VBox) card.getRoot().lookup("#fields");
            Label introducer = (Label) card.getRoot().lookup("#introducer");

            assertEquals("Introduced by: Jane Doe", introducer.getText());
            assertTrue(introducer.isVisible());
            assertEquals(0, fields.getChildren().indexOf(introducer));
        });
    }

    @Test
    void card_datedActions_showsDatesOverdueAndUndatedLabels() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Person person = new PersonBuilder(ALICE).withFollowUps(
                    new FollowUp(1, "Call", FollowUp.Status.PENDING, LocalDate.of(2000, 2, 29)),
                    new FollowUp(2, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(9999, 12, 31)),
                    new FollowUp(3, "Check in", FollowUp.Status.PENDING),
                    new FollowUp(4, "Done", FollowUp.Status.COMPLETED, LocalDate.of(2000, 2, 29))).build();
            Label label = (Label) new PersonCard(person, 1).getRoot().lookup("#followUps");
            assertEquals("Follow-ups:\n#1 [PENDING] Call | Due: 2000-02-29 [OVERDUE]\n"
                    + "#2 [PENDING] Send portfolio | Due: 9999-12-31\n"
                    + "#3 [PENDING] Check in | No due date\n"
                    + "#4 [COMPLETED] Done | Due: 2000-02-29", label.getText());
        });
    }

    @Test
    void list_contactReplacement_refreshesExistingDisplayedCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            ObservableList<Person> people = FXCollections.observableArrayList(ALICE);
            PersonListPanel panel = new PersonListPanel(people);
            new Scene(panel.getRoot(), 500, 600);
            panel.getRoot().resize(500, 600);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            Label before = (Label) panel.getRoot().lookup("#followUps");
            assertFalse(before.isManaged());
            people.set(0, ALICE.appendFollowUp(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING,
                    LocalDate.of(2000, 2, 29))));
            panel.getRoot().layout();
            Label after = (Label) panel.getRoot().lookup("#followUps");
            assertTrue(after.isManaged());
            assertTrue(after.getText().contains("#1 [PENDING] Send portfolio | Due: 2000-02-29 [OVERDUE]"));
        });
    }
}
