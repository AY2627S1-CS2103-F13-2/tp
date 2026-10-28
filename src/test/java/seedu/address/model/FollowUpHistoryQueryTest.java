package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class FollowUpHistoryQueryTest {
    @Test
    void getFollowUps_mixedStatuses_sortsDatesAndRetainsStableTies() {
        LocalDate date = LocalDate.of(2026, 10, 26);
        FollowUp undated = new FollowUp(9, "Undated", FollowUp.Status.COMPLETED);
        FollowUp firstTie = new FollowUp(7, "First", FollowUp.Status.COMPLETED, date);
        FollowUp secondTie = new FollowUp(2, "Second", FollowUp.Status.PENDING, date);
        FollowUp early = new FollowUp(8, "Early", FollowUp.Status.COMPLETED, LocalDate.MIN);
        FollowUp thirdTie = new FollowUp(7, "Third", FollowUp.Status.COMPLETED, date);
        FollowUp pendingUndated = new FollowUp(9, "Pending", FollowUp.Status.PENDING);
        Person alice = new PersonBuilder(ALICE).withFollowUps(undated, firstTie, secondTie).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(thirdTie, early, pendingUndated).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(benson);
        model.updateFilteredPersonList(person -> person.equals(benson));

        assertEquals(List.of(new FollowUpEntry(benson, early), new FollowUpEntry(alice, firstTie),
                new FollowUpEntry(alice, secondTie), new FollowUpEntry(benson, thirdTie),
                new FollowUpEntry(alice, undated), new FollowUpEntry(benson, pendingUndated)), model.getFollowUps());
        assertEquals(List.of(new FollowUpEntry(alice, secondTie), new FollowUpEntry(benson, pendingUndated)),
                model.getPendingFollowUps());
        assertEquals(List.of(benson), model.getFilteredPersonList());
    }

    @Test
    void getFollowUps_changes_returnsFreshUnmodifiableSnapshot() {
        FollowUp done = new FollowUp(1, "Done", FollowUp.Status.COMPLETED);
        Person alice = new PersonBuilder(ALICE).withFollowUps(done).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        List<FollowUpEntry> before = model.getFollowUps();
        assertThrows(UnsupportedOperationException.class, before::clear);
        assertSame(done, before.getFirst().followUp());
        Person updated = alice.appendFollowUp(new FollowUp(2, "Call", FollowUp.Status.PENDING, LocalDate.MAX));
        model.setPerson(alice, updated);

        assertEquals(List.of(2, 1), model.getFollowUps().stream().map(entry -> entry.followUp().getId()).toList());
        assertEquals(List.of(new FollowUpEntry(alice, done)), before);
        model.deletePerson(updated);
        assertTrue(model.getFollowUps().isEmpty());
    }

    @Test
    void getFollowUps_noActions_returnsEmpty() {
        ModelManager model = new ModelManager();
        assertTrue(model.getFollowUps().isEmpty());
        model.addPerson(ALICE);
        assertTrue(model.getFollowUps().isEmpty());
    }
}
