package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.FollowUpFilter;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class PendingFollowUpsCommandTest {
    private final PendingFollowUpsCommand command = new PendingFollowUpsCommand();

    @Test
    void execute_emptyOrCompletedOnly_showsEmptyMessage() {
        ModelManager model = new ModelManager();
        assertEquals(PendingFollowUpsCommand.MESSAGE_EMPTY, command.execute(model).getFeedbackToUser());
        model.addPerson(ALICE);
        model.addPerson(new PersonBuilder(BENSON).withFollowUps(
                new FollowUp(1, "Done", FollowUp.Status.COMPLETED, LocalDate.of(2000, 1, 1))).build());
        assertEquals(PendingFollowUpsCommand.MESSAGE_EMPTY, command.execute(model).getFeedbackToUser());
    }

    @Test
    void execute_mixedActions_showsSortedContactsDatesAndOverdueWithoutChangingModel() {
        Person alice = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(9999, 12, 31)),
                new FollowUp(2, "Check in", FollowUp.Status.PENDING),
                new FollowUp(3, "Done", FollowUp.Status.COMPLETED)).build();
        Person benson = new PersonBuilder(BENSON).withFollowUps(
                new FollowUp(1, "Call", FollowUp.Status.PENDING, LocalDate.of(2000, 1, 1))).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(benson);
        model.updateFilteredPersonList(person -> person.equals(alice));
        ContactFile before = new ContactFile(model.getContactFile());
        CommandResult result = command.execute(model);
        assertEquals("Pending follow-ups (3):\n"
                + "1. " + benson.getName() + " | #1 [PENDING] Call | Due: 2000-01-01 [OVERDUE]\n"
                + "2. " + alice.getName() + " | #1 [PENDING] Send portfolio | Due: 9999-12-31\n"
                + "3. " + alice.getName() + " | #2 [PENDING] Check in | No due date\n"
                + FollowUpSelectionRequest.MESSAGE_SELECTION, result.getFeedbackToUser());
        assertEquals(before, model.getContactFile());
        assertEquals(List.of(alice), model.getFilteredPersonList());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
        assertTrue(result.getPendingRequest().isPresent());
    }

    @Test
    void execute_dateViews_selectCorrectActionAndRetainHeadingAfterInvalidReply() throws Exception {
        LocalDate today = LocalDate.of(2026, 10, 26);
        Clock clock = Clock.fixed(Instant.parse("2026-10-26T12:00:00Z"), ZoneOffset.UTC);
        Person alice = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Past", FollowUp.Status.PENDING, today.minusDays(1)),
                new FollowUp(2, "Today", FollowUp.Status.PENDING, today),
                new FollowUp(3, "Future", FollowUp.Status.PENDING, LocalDate.MAX),
                new FollowUp(4, "Undated", FollowUp.Status.PENDING),
                new FollowUp(5, "Done", FollowUp.Status.COMPLETED, today)).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(BENSON);
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        ContactFile before = new ContactFile(model.getContactFile());
        Map<FollowUpFilter, String> headings = Map.of(FollowUpFilter.OVERDUE, "Overdue follow-ups",
                FollowUpFilter.TODAY, "Follow-ups due today", FollowUpFilter.UPCOMING, "Upcoming follow-ups");
        Map<FollowUpFilter, Integer> expectedIds = Map.of(FollowUpFilter.OVERDUE, 1,
                FollowUpFilter.TODAY, 2, FollowUpFilter.UPCOMING, 3);
        for (FollowUpFilter filter : headings.keySet()) {
            CommandResult result = new PendingFollowUpsCommand(filter, clock).execute(model);
            assertTrue(result.getFeedbackToUser().startsWith(headings.get(filter) + " (1):"));
            assertFalse(result.shouldSaveContactFile());
            PendingRequest request = result.getPendingRequest().orElseThrow();
            assertTrue(request.respond("99", model).getFeedbackToUser().contains(headings.get(filter) + " (1):"));
            CommandResult selected = request.respond("1", model);
            assertEquals(alice, selected.getPersonForDetails().orElseThrow());
            assertEquals(expectedIds.get(filter).intValue(), selected.getFollowUpForDetails().orElseThrow().getId());
        }
        assertEquals(before, model.getContactFile());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    void execute_emptyDateViews_showSpecificReadOnlyMessages() {
        Map<FollowUpFilter, String> expected = Map.of(FollowUpFilter.PENDING, PendingFollowUpsCommand.MESSAGE_EMPTY,
                FollowUpFilter.OVERDUE, "No overdue follow-ups remain.", FollowUpFilter.TODAY,
                "No follow-ups are due today.", FollowUpFilter.UPCOMING, "No upcoming follow-ups remain.",
                FollowUpFilter.COMPLETED, "No completed follow-ups found.",
                FollowUpFilter.UNDATED, "No undated pending follow-ups remain.");
        for (Map.Entry<FollowUpFilter, String> entry : expected.entrySet()) {
            CommandResult result = new PendingFollowUpsCommand(entry.getKey()).execute(new ModelManager());
            assertEquals(entry.getValue(), result.getFeedbackToUser());
            assertTrue(result.getPendingRequest().isEmpty());
            assertFalse(result.shouldSaveContactFile());
        }
    }

    @Test
    void execute_localDate_usesClockTimezone() {
        Instant instant = Instant.parse("2026-10-26T00:30:00Z");
        Clock utc = Clock.fixed(instant, ZoneOffset.UTC);
        Clock previousDay = Clock.fixed(instant, ZoneOffset.ofHours(-1));
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Call", FollowUp.Status.PENDING, LocalDate.of(2026, 10, 26))).build());

        assertTrue(new PendingFollowUpsCommand(FollowUpFilter.TODAY, utc).execute(model)
                .getPendingRequest().isPresent());
        assertTrue(new PendingFollowUpsCommand(FollowUpFilter.TODAY, previousDay).execute(model)
                .getPendingRequest().isEmpty());
        assertTrue(new PendingFollowUpsCommand(FollowUpFilter.UPCOMING, previousDay).execute(model)
                .getPendingRequest().isPresent());
    }

    @Test
    void execute_completedAndUndatedViews_selectStoredActionsFromHiddenContacts() throws Exception {
        Person alice = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(8, "Done undated", FollowUp.Status.COMPLETED),
                new FollowUp(4, "Done dated", FollowUp.Status.COMPLETED, LocalDate.MIN),
                new FollowUp(7, "Pending undated", FollowUp.Status.PENDING),
                new FollowUp(9, "Pending dated", FollowUp.Status.PENDING, LocalDate.MAX)).build();
        ModelManager model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(BENSON);
        model.updateFilteredPersonList(person -> person.equals(BENSON));

        CommandResult completed = new PendingFollowUpsCommand(FollowUpFilter.COMPLETED).execute(model);
        assertTrue(completed.getFeedbackToUser().startsWith("Completed follow-ups (2):"));
        PendingRequest request = completed.getPendingRequest().orElseThrow();
        assertTrue(request.respond("99", model).getFeedbackToUser().contains("Completed follow-ups (2):"));
        CommandResult selected = request.respond("2", model);
        assertEquals(8, selected.getFollowUpForDetails().orElseThrow().getId());
        assertEquals(alice, selected.getPersonForDetails().orElseThrow());
        assertFalse(completed.getFeedbackToUser().contains("[OVERDUE]"));

        CommandResult undated = new PendingFollowUpsCommand(FollowUpFilter.UNDATED).execute(model);
        assertTrue(undated.getFeedbackToUser().startsWith("Undated pending follow-ups (1):"));
        assertEquals(7, undated.getPendingRequest().orElseThrow().respond("1", model)
                .getFollowUpForDetails().orElseThrow().getId());
        assertFalse(completed.shouldSaveContactFile());
        assertFalse(undated.shouldSaveContactFile());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    void execute_nullModel_rejectsRequest() {
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
