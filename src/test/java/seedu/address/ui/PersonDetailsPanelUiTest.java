package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

class PersonDetailsPanelUiTest {
    @BeforeAll
    static void startToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    void optionalContext_sortsEventsAndClearsAbsentFieldsWhenContactChanges() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonDetailsPanel panel = new PersonDetailsPanel();
            new Scene(panel.getRoot(), 400, 600);
            panel.getRoot().applyCss();
            Person contact = new PersonBuilder(ALICE).withIntroducer("Benson Meier").build()
                    .withEvent(new Event("Zeta fair")).withEvent(new Event("Alpha fair"));
            panel.showPerson(contact, FieldOrder.DEFAULT);
            assertEquals("Benson Meier", ((Label) panel.getRoot().lookup("#introducerValue")).getText());
            assertEquals("Alpha fair\nZeta fair", ((Label) panel.getRoot().lookup("#eventsValue")).getText());
            assertTrue(panel.getRoot().lookup("#events").isManaged());
            panel.showPerson(NAME_ONLY, FieldOrder.DEFAULT);
            assertFalse(panel.getRoot().lookup("#company").isManaged());
            assertFalse(panel.getRoot().lookup("#phone").isManaged());
            assertFalse(panel.getRoot().lookup("#tags").isManaged());
            assertEquals(NAME_ONLY.getName().fullName, ((Label) panel.getRoot().lookup("#detailName")).getText());
        });
    }
}
