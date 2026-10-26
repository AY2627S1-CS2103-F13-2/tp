package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.person.FollowUp;

public class ChangeFollowUpStatusCommandTest {

    @Test
    public void constructor_nonPositiveId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 0, FollowUp.Status.COMPLETED));
    }

    @Test
    public void equals() {
        ChangeFollowUpStatusCommand complete =
                new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 2, FollowUp.Status.COMPLETED);
        assertEquals(complete, new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 2, FollowUp.Status.COMPLETED));
        assertNotEquals(complete, new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 2, FollowUp.Status.PENDING));
        assertNotEquals(complete, new ChangeFollowUpStatusCommand(Index.fromOneBased(2), 2, FollowUp.Status.COMPLETED));
        assertNotEquals(complete, new ChangeFollowUpStatusCommand(Index.fromOneBased(1), 3, FollowUp.Status.COMPLETED));
        assertNotEquals(complete, null);
    }
}
