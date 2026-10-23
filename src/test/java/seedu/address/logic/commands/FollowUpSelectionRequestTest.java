package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.FollowUpEntry;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class FollowUpSelectionRequestTest {
    private ModelManager model;
    private Person alice;
    private Person benson;
    private FollowUpSelectionRequest request;

    @Test
    void shouldRunAsCommand_nonNumbersLeaveSelection_numericInputStays() {
        for (String input : List.of("list", "view 1", "followups", "help", "", "y", "1 extra")) {
            assertTrue(request.shouldRunAsCommand(input));
        }
        for (String input : List.of("1", "0", "-1", "+1", "1.5", "2147483648")) {
            assertFalse(request.shouldRunAsCommand(input));
        }
    }

    @BeforeEach
    void setUp() {
        alice = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(7, "Portfolio", FollowUp.Status.PENDING, LocalDate.of(9999, 12, 31)),
                new FollowUp(2, "Undated", FollowUp.Status.PENDING)).build();
        benson = new PersonBuilder(BENSON).withFollowUps(
                new FollowUp(7, "Call", FollowUp.Status.PENDING, LocalDate.of(2000, 1, 1))).build();
        model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(benson);
        request = new FollowUpSelectionRequest(model.getPendingFollowUps());
    }

    @Test
    void respond_validNumbers_usesDisplayedOrderAndReturnsAssociatedContactDetails() throws Exception {
        model.updateFilteredPersonList(person -> person.equals(alice));
        CommandResult first = request.respond("1", model);
        assertEquals(benson, first.getPersonForDetails().orElseThrow());
        assertTrue(first.getFeedbackToUser().startsWith(
                "Selected follow-up:\n#7 [PENDING] Call | Due: 2000-01-01 [OVERDUE]\nContact:\n"));
        assertTrue(first.getFeedbackToUser().contains(benson.getName().fullName));
        assertFalse(first.getPendingRequest().isPresent());
        CommandResult middle = request.respond("2", model);
        assertEquals(alice.getFollowUps().getFirst(), middle.getFollowUpForDetails().orElseThrow());
        CommandResult last = request.respond("3", model);
        assertEquals(alice, last.getPersonForDetails().orElseThrow());
        assertTrue(last.getFeedbackToUser().startsWith("Selected follow-up:\n#2 [PENDING] Undated | No due date"));
        assertEquals(List.of(alice), model.getFilteredPersonList());
    }

    @Test
    void respond_invalidNumbers_repeatsSameRequest() throws Exception {
        for (String response : List.of("0", "-1", "+1", "1.5", "4", "2147483648", "9999999999999999999")) {
            CommandResult result = request.respond(response, model);
            assertSame(request, result.getPendingRequest().orElseThrow());
            assertEquals(FollowUpSelectionRequest.MESSAGE_INVALID + "\n" + request.getPrompt(),
                    result.getFeedbackToUser());
        }
    }

    @Test
    void respond_addedEarlierAction_preservesOriginalSelectionUntilNewList() throws Exception {
        Person updated = alice.appendFollowUp(
                new FollowUp(9, "Earlier", FollowUp.Status.PENDING, LocalDate.of(1900, 1, 1)));
        model.setPerson(alice, updated);
        assertEquals(benson, request.respond("1", model).getPersonForDetails().orElseThrow());
        FollowUpSelectionRequest refreshed = new FollowUpSelectionRequest(model.getPendingFollowUps());
        assertEquals(updated, refreshed.respond("1", model).getPersonForDetails().orElseThrow());
    }

    @Test
    void respond_selectedContactChangedOrDeleted_rejectsStaleSelection() {
        model.setPerson(benson, new PersonBuilder(benson).withName("Changed").build());
        CommandException changed = assertThrows(CommandException.class, () -> request.respond("1", model));
        assertEquals(FollowUpSelectionRequest.MESSAGE_CHANGED, changed.getMessage());
        model.deletePerson(alice);
        assertThrows(CommandException.class, () -> request.respond("2", model));
    }

    @Test
    void respond_selectedActionCompleted_rejectsStaleSelection() {
        model.setPerson(benson, new PersonBuilder(benson).withFollowUps(
                new FollowUp(7, "Call", FollowUp.Status.COMPLETED, LocalDate.of(2000, 1, 1))).build());
        assertThrows(CommandException.class, () -> request.respond("1", model));
    }

    @Test
    void respond_selectedActionRemovedOrDateChanged_rejectsStaleSelection() {
        model.setPerson(benson, new PersonBuilder(benson).withFollowUps().build());
        assertThrows(CommandException.class, () -> request.respond("1", model));
        model.setPerson(model.getContactFile().getPersonList().getLast(),
                new PersonBuilder(benson).withFollowUps(
                        new FollowUp(7, "Call", FollowUp.Status.PENDING, LocalDate.of(2001, 1, 1))).build());
        assertThrows(CommandException.class, () -> request.respond("1", model));
    }

    @Test
    void constructor_copiesEntriesAndRejectsInvalidArguments() {
        List<FollowUpEntry> entries = new ArrayList<>(model.getPendingFollowUps());
        FollowUpSelectionRequest copied = new FollowUpSelectionRequest(entries);
        String prompt = copied.getPrompt();
        entries.clear();
        assertEquals(prompt, copied.getPrompt());
        assertThrows(NullPointerException.class, () -> new FollowUpSelectionRequest(null));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpSelectionRequest(List.of()));
        assertThrows(NullPointerException.class, () -> request.respond(null, model));
        assertThrows(NullPointerException.class, () -> request.respond("1", null));
    }
}
