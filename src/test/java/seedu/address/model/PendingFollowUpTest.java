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

class PendingFollowUpTest {
    @Test
    void getPendingFollowUps_noContactsOrNoPendingActions_returnsEmpty() {
        ModelManager model = new ModelManager();
        assertTrue(model.getPendingFollowUps().isEmpty());
        model.addPerson(ALICE);
        model.addPerson(new PersonBuilder(BENSON).withFollowUps(
                new FollowUp(1, "Done", FollowUp.Status.COMPLETED, LocalDate.MIN),
                new FollowUp(2, "Also done", FollowUp.Status.COMPLETED)).build());
        assertTrue(model.getPendingFollowUps().isEmpty());
    }

    @Test
    void getPendingFollowUps_mixedContacts_sortsDatesThenUndatedAndExcludesCompleted() {
        FollowUp late = pending(1, LocalDate.MAX);
        FollowUp early = pending(2, LocalDate.MIN);
        FollowUp middle = pending(3, LocalDate.of(2026, 10, 21));
        FollowUp undated = pending(4, null);
        Person alice = new PersonBuilder(ALICE).withFollowUps(late, undated).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(middle,
                new FollowUp(5, "Completed", FollowUp.Status.COMPLETED, LocalDate.MIN), early).build();
        ModelManager model = modelWith(alice, benson);
        assertEquals(List.of(new FollowUpEntry(benson, early), new FollowUpEntry(benson, middle),
                new FollowUpEntry(alice, late), new FollowUpEntry(alice, undated)), model.getPendingFollowUps());
        assertEquals(List.of(late, undated), alice.getFollowUps());
    }

    @Test
    void getPendingFollowUps_tiedDatesAndUndated_preservesContactAndActionOrder() {
        LocalDate date = LocalDate.of(2026, 10, 21);
        Person alice = new PersonBuilder(ALICE).withFollowUps(
                pending(9, null), pending(7, date), pending(2, date), pending(1, null)).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(pending(7, date), pending(9, null)).build();
        List<FollowUpEntry> entries = modelWith(alice, benson).getPendingFollowUps();
        assertEquals(List.of(7, 2, 7, 9, 1, 9), entries.stream().map(entry -> entry.followUp().getId()).toList());
        assertEquals(List.of(alice, alice, benson, alice, alice, benson),
                entries.stream().map(FollowUpEntry::contact).toList());
        assertSame(alice, entries.getFirst().contact());
        assertSame(alice.getFollowUps().get(1), entries.getFirst().followUp());
    }

    @Test
    void getPendingFollowUps_activeFilter_includesHiddenContactsAndPreservesView() {
        Person alice = new PersonBuilder(ALICE).withFollowUps(pending(1, LocalDate.MIN)).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(pending(1, null)).build();
        ModelManager model = modelWith(alice, benson);
        model.updateFilteredPersonList(person -> person.equals(benson));
        assertEquals(List.of(new FollowUpEntry(alice, alice.getFollowUps().getFirst()),
                new FollowUpEntry(benson, benson.getFollowUps().getFirst())), model.getPendingFollowUps());
        assertEquals(List.of(benson), model.getFilteredPersonList());
    }

    @Test
    void getPendingFollowUps_contactChanges_returnsFreshUnmodifiableSnapshot() {
        Person alice = new PersonBuilder(ALICE).withFollowUps(pending(1, null)).build();
        ModelManager model = modelWith(alice);
        List<FollowUpEntry> before = model.getPendingFollowUps();
        assertThrows(UnsupportedOperationException.class, before::clear);
        Person updated = alice.appendFollowUp(pending(2, LocalDate.MIN));
        model.setPerson(alice, updated);
        assertEquals(List.of(new FollowUpEntry(updated, updated.getFollowUps().getLast()),
                new FollowUpEntry(updated, updated.getFollowUps().getFirst())), model.getPendingFollowUps());
        assertEquals(List.of(new FollowUpEntry(alice, alice.getFollowUps().getFirst())), before);
        model.deletePerson(updated);
        assertTrue(model.getPendingFollowUps().isEmpty());
        model.setContactFile(modelWith(alice).getContactFile());
        assertEquals(before, model.getPendingFollowUps());
    }

    @Test
    void constructor_missingContactOrAction_rejectsEntry() {
        FollowUp action = pending(1, null);
        assertThrows(NullPointerException.class, () -> new FollowUpEntry(null, action));
        assertThrows(NullPointerException.class, () -> new FollowUpEntry(ALICE, null));
    }

    private static FollowUp pending(int id, LocalDate date) {
        return new FollowUp(id, "Call", FollowUp.Status.PENDING, date);
    }

    private static ModelManager modelWith(Person... contacts) {
        ModelManager model = new ModelManager();
        for (Person contact : contacts) {
            model.addPerson(contact);
        }
        return model;
    }
}
