package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
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
    void execute_nullModel_rejectsRequest() {
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
