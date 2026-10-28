package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

class DeleteFollowUpCommandTest {
    @Test
    void constructor_invalidTarget_rejectsBeforeExecution() {
        Index index = Index.fromOneBased(1);
        assertThrows(NullPointerException.class, () -> new DeleteFollowUpCommand(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new DeleteFollowUpCommand(index, 0));
        assertThrows(IllegalArgumentException.class, () -> new DeleteFollowUpCommand(index, -1));
        assertThrows(NullPointerException.class, () -> new DeleteFollowUpCommand(index, 1).execute(null));
    }

    @Test
    void command_distinctTargets_haveDistinctValueSemanticsAndDiagnostics() {
        DeleteFollowUpCommand command = new DeleteFollowUpCommand(Index.fromOneBased(1), 9);
        assertEquals(command, command);
        assertEquals(command, new DeleteFollowUpCommand(Index.fromOneBased(1), 9));
        assertNotEquals(command, new DeleteFollowUpCommand(Index.fromOneBased(2), 9));
        assertNotEquals(command, new DeleteFollowUpCommand(Index.fromOneBased(1), 2));
        assertNotEquals(command, null);
        assertNotEquals(command, "deletefollowup");
        assertTrue(command.toString().contains("targetIndex="));
        assertTrue(command.toString().contains("followUpId=9"));
    }
}
