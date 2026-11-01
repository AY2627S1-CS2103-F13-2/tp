package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

class EditFollowUpCommandTest {

    @Test
    void execute_duplicatePendingDescription_rejectsEdit() {
        Person person = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Call client", FollowUp.Status.PENDING),
                new FollowUp(2, "Send proposal", FollowUp.Status.PENDING))
                .build();
        ModelManager model = new ModelManager(
                new ContactFileBuilder().withPerson(person).build(),
                new UserPrefs());
        EditFollowUpCommand command = new EditFollowUpCommand(
                Index.fromOneBased(1), 1, "Send proposal", null, false);

        assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(person, model.getFilteredPersonList().getFirst());
    }

    @Test
    void execute_distinctPendingDescription_allowsEdit() throws Exception {
        Person person = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Call client", FollowUp.Status.PENDING),
                new FollowUp(2, "Send proposal", FollowUp.Status.PENDING))
                .build();
        ModelManager model = new ModelManager(
                new ContactFileBuilder().withPerson(person).build(),
                new UserPrefs());
        EditFollowUpCommand command = new EditFollowUpCommand(
                Index.fromOneBased(1), 1, "Schedule meeting", null, false);

        command.execute(model);

        assertEquals(
                new FollowUp(1, "Schedule meeting", FollowUp.Status.PENDING),
                model.getFilteredPersonList().getFirst().getFollowUps().getFirst());
    }

    @Test
    void execute_completedFollowUpMayMatchPendingDescription() throws Exception {
        Person person = new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(1, "Old description", FollowUp.Status.COMPLETED),
                new FollowUp(2, "Send proposal", FollowUp.Status.PENDING))
                .build();
        ModelManager model = new ModelManager(
                new ContactFileBuilder().withPerson(person).build(),
                new UserPrefs());
        EditFollowUpCommand command = new EditFollowUpCommand(
                Index.fromOneBased(1), 1, "Send proposal", null, false);

        command.execute(model);

        assertEquals(
                new FollowUp(1, "Send proposal", FollowUp.Status.COMPLETED),
                model.getFilteredPersonList().getFirst().getFollowUps().getFirst());
    }
}
