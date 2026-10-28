package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class FollowUpDateFilterTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 26);

    @Test
    void apply_boundaries_excludesCompletedAndUndatedFromDateViews() {
        FollowUpEntry overdue = entry(1, FollowUp.Status.PENDING, TODAY.minusDays(1));
        FollowUpEntry today = entry(2, FollowUp.Status.PENDING, TODAY);
        FollowUpEntry upcoming = entry(3, FollowUp.Status.PENDING, TODAY.plusDays(1));
        FollowUpEntry distant = entry(4, FollowUp.Status.PENDING, LocalDate.MAX);
        FollowUpEntry undated = entry(5, FollowUp.Status.PENDING, null);
        List<FollowUpEntry> entries = List.of(overdue, today, upcoming, distant, undated,
                entry(6, FollowUp.Status.COMPLETED, TODAY.minusDays(1)),
                entry(7, FollowUp.Status.COMPLETED, TODAY),
                entry(8, FollowUp.Status.COMPLETED, TODAY.plusDays(1)),
                entry(9, FollowUp.Status.COMPLETED, null));

        assertEquals(List.of(overdue), FollowUpDateFilter.OVERDUE.apply(entries, TODAY));
        assertEquals(List.of(today), FollowUpDateFilter.TODAY.apply(entries, TODAY));
        assertEquals(List.of(upcoming, distant), FollowUpDateFilter.UPCOMING.apply(entries, TODAY));
        assertEquals(List.of(overdue, today, upcoming, distant, undated), FollowUpDateFilter.ALL.apply(entries, TODAY));
    }

    @Test
    void apply_modelSnapshot_preservesHiddenContactsAndStableDateTies() {
        Person alice = new PersonBuilder(ALICE).withFollowUps(
                pending(9, TODAY.plusDays(2)), pending(7, TODAY.plusDays(1)), pending(2, TODAY.plusDays(1))).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(pending(7, TODAY.plusDays(1))).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(benson);
        model.updateFilteredPersonList(person -> person.equals(benson));

        List<FollowUpEntry> result = FollowUpDateFilter.UPCOMING.apply(model.getPendingFollowUps(), TODAY);

        assertEquals(List.of(7, 2, 7, 9), result.stream().map(entry -> entry.followUp().getId()).toList());
        assertEquals(List.of(alice, alice, benson, alice), result.stream().map(FollowUpEntry::contact).toList());
        assertEquals(List.of(benson), model.getFilteredPersonList());
    }

    @Test
    void apply_newDate_reclassifiesActionsWithoutMutatingSnapshot() {
        FollowUpEntry action = entry(1, FollowUp.Status.PENDING, TODAY);
        List<FollowUpEntry> source = new ArrayList<>(List.of(action));
        List<FollowUpEntry> result = FollowUpDateFilter.TODAY.apply(source, TODAY);
        source.clear();

        assertEquals(List.of(action), result);
        assertThrows(UnsupportedOperationException.class, result::clear);
        assertTrue(FollowUpDateFilter.TODAY.apply(result, TODAY.plusDays(1)).isEmpty());
        assertEquals(result, FollowUpDateFilter.OVERDUE.apply(result, TODAY.plusDays(1)));
    }

    @Test
    void apply_extremeDates_doesNotOverflow() {
        List<FollowUpEntry> entries = List.of(entry(1, FollowUp.Status.PENDING, LocalDate.MIN),
                entry(2, FollowUp.Status.PENDING, LocalDate.MAX));

        assertTrue(FollowUpDateFilter.OVERDUE.apply(entries, LocalDate.MIN).isEmpty());
        assertTrue(FollowUpDateFilter.UPCOMING.apply(entries, LocalDate.MAX).isEmpty());
        assertEquals(List.of(entries.get(1)), FollowUpDateFilter.UPCOMING.apply(entries, LocalDate.MIN));
    }

    @Test
    void apply_emptyInput_returnsEmptyForEveryFilter() {
        for (FollowUpDateFilter filter : FollowUpDateFilter.values()) {
            assertTrue(filter.apply(List.of(), TODAY).isEmpty());
        }
    }

    private static FollowUp pending(int id, LocalDate date) {
        return new FollowUp(id, "Call", FollowUp.Status.PENDING, date);
    }

    private static FollowUpEntry entry(int id, FollowUp.Status status, LocalDate date) {
        return new FollowUpEntry(ALICE, new FollowUp(id, "Call", status, date));
    }
}
