package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

class FollowUpTest {
    @Test
    void constructor_validAction_trimsDescriptionAndDisplaysIdentityAndStatus() {
        FollowUp action = new FollowUp(7, "  Send portfolio  ", FollowUp.Status.PENDING);
        assertEquals(7, action.getId());
        assertEquals("Send portfolio", action.getDescription());
        assertEquals(FollowUp.Status.PENDING, action.getStatus());
        assertEquals("#7 [PENDING] Send portfolio", action.toString());
    }

    @Test
    void constructor_invalidFields_rejectsAction() {
        assertThrows(IllegalArgumentException.class, () -> new FollowUp(0, "Action", FollowUp.Status.PENDING));
        assertThrows(IllegalArgumentException.class, () -> new FollowUp(-1, "Action", FollowUp.Status.PENDING));
        assertThrows(NullPointerException.class, () -> new FollowUp(1, null, FollowUp.Status.PENDING));
        assertThrows(NullPointerException.class, () -> new FollowUp(1, "Action", null));
        for (String blank : List.of("", " ", "\t\r\n", "\u2003")) {
            assertFalse(FollowUp.isValidDescription(blank));
            assertThrows(IllegalArgumentException.class, () -> new FollowUp(1, blank, FollowUp.Status.PENDING));
        }
        assertFalse(FollowUp.isValidDescription(null));
        assertTrue(FollowUp.isValidDescription("Call after 5pm!"));
    }

    @Test
    void equals_actionData_agreesWithHashCode() {
        FollowUp action = new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING);
        FollowUp same = new FollowUp(1, " Send portfolio ", FollowUp.Status.PENDING);
        assertEquals(action, action);
        assertEquals(action, same);
        assertEquals(action.hashCode(), same.hashCode());
        assertNotEquals(action, new FollowUp(2, "Send portfolio", FollowUp.Status.PENDING));
        assertNotEquals(action, new FollowUp(1, "Call", FollowUp.Status.PENDING));
        assertNotEquals(action, new FollowUp(1, "Send portfolio", FollowUp.Status.COMPLETED));
        assertNotEquals(action, null);
        assertNotEquals(action, "Send portfolio");
    }

    @Test
    void person_actionHistory_isCopiedAndUnmodifiable() {
        FollowUp pending = new FollowUp(4, "Send portfolio", FollowUp.Status.PENDING);
        FollowUp completed = new FollowUp(9, "Call", FollowUp.Status.COMPLETED);
        List<FollowUp> actions = new ArrayList<>(List.of(completed, pending));
        Person contact = new Person(ALICE.getName(), null, null, null, null, null, ALICE.getTags(), actions);
        actions.clear();
        assertEquals(List.of(completed, pending), contact.getFollowUps());
        assertThrows(UnsupportedOperationException.class, () -> contact.getFollowUps().clear());
        assertThrows(IllegalArgumentException.class, () -> contact.appendFollowUp(pending));
        assertThrows(NullPointerException.class, () -> contact.appendFollowUp(null));
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), null, null, null, null,
                null, ALICE.getTags(), null));
    }

    @Test
    void person_actionChange_changesStrongEqualityButPreservesContactIdentity() {
        FollowUp action = new FollowUp(8, "Send portfolio", FollowUp.Status.PENDING);
        Person updated = ALICE.appendFollowUp(action);
        Person same = new PersonBuilder(ALICE).withFollowUps(action).build();
        assertNotEquals(ALICE, updated);
        assertEquals(updated, same);
        assertEquals(updated.hashCode(), same.hashCode());
        assertTrue(ALICE.isSamePerson(updated));
        assertTrue(ALICE.hasSamePhone(updated));
        assertTrue(ALICE.hasSameEmail(updated));
        assertTrue(ALICE.getFollowUps().isEmpty());
        assertTrue(updated.toString().contains("followUps=[#8 [PENDING] Send portfolio]"));
        assertEquals(updated, new PersonBuilder(updated).build());
    }
}
