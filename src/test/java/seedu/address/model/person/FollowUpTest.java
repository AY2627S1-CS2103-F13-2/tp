package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

class FollowUpTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void constructor_noDueDate_isNotOverdue() {
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING);
        assertTrue(action.getDueDate().isEmpty());
        assertFalse(action.isOverdue());
    }

    @Test
    void isOverdue_pendingAction_checksDueDateAgainstToday() {
        FollowUp past = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY.minusDays(1), CLOCK);
        FollowUp today = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY, CLOCK);
        FollowUp future = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY.plusDays(1), CLOCK);
        assertEquals(TODAY.minusDays(1), past.getDueDate().orElseThrow());
        assertTrue(past.isOverdue());
        assertFalse(today.isOverdue());
        assertFalse(future.isOverdue());
    }

    @Test
    void isOverdue_completedAction_isNotOverdue() {
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.COMPLETED, TODAY.minusDays(1), CLOCK);
        assertFalse(action.isOverdue());
    }

    @Test
    void isOverdue_dayChanges_refreshesWithoutChangingEqualityOrHashCode() {
        AdjustableClock clock = new AdjustableClock();
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY, clock);
        FollowUp same = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY, CLOCK);
        int originalHashCode = action.hashCode();
        assertFalse(action.isOverdue());
        clock.instant = CLOCK.instant().plusSeconds(24 * 60 * 60);
        assertTrue(action.isOverdue());
        assertEquals(same, action);
        assertEquals(originalHashCode, action.hashCode());
        clock.instant = CLOCK.instant();
        assertFalse(action.isOverdue());
    }

    @Test
    void isOverdue_midnightBoundary_usesClockTimeZone() {
        Instant instant = Instant.parse("2026-10-21T16:00:00Z");
        FollowUp utc = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY,
                Clock.fixed(instant, ZoneOffset.UTC));
        FollowUp singapore = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY,
                Clock.fixed(instant, ZoneId.of("Asia/Singapore")));
        assertFalse(utc.isOverdue());
        assertTrue(singapore.isOverdue());
    }

    @Test
    void constructor_nullClock_rejectsAction() {
        assertThrows(NullPointerException.class, () ->
                new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY, null));
    }

    @Test
    void equals_dueDates_comparesDatesAndAgreesWithHashCode() {
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY);
        FollowUp same = new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY);
        assertEquals(action, same);
        assertEquals(action.hashCode(), same.hashCode());
        assertNotEquals(action, new FollowUp(1, "Call", FollowUp.Status.PENDING));
        assertNotEquals(action, new FollowUp(1, "Call", FollowUp.Status.PENDING, TODAY.plusDays(1)));
    }

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
    /**
     * Allows tests to advance time without waiting for the system clock.
     */
    private static final class AdjustableClock extends Clock {
        private Instant instant = CLOCK.instant();

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
