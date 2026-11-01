package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.FollowUpEntry;
import seedu.address.model.person.FollowUp;

public class CommandResultTest {
    @Test
    public void followUpListing_isImmutableAndSurvivesResultCopies() {
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING);
        FollowUpEntry entry = new FollowUpEntry(ALICE, action);
        List<FollowUpEntry> entries = new ArrayList<>(List.of(entry));
        FollowUpListing listing = new FollowUpListing("Pending follow-ups", entries);
        CommandResult original = CommandResult.createPersonDetails("feedback", ALICE).withFollowUpListing(listing);
        entries.clear();
        assertEquals(List.of(entry), original.getFollowUpListing().orElseThrow().entries());
        assertEquals(listing, original.withFeedbackPrefix("Note: ").getFollowUpListing().orElseThrow());
        assertEquals(listing, original.withSelectedFollowUp(action).getFollowUpListing().orElseThrow());
        assertEquals(original, CommandResult.createPersonDetails("feedback", ALICE).withFollowUpListing(listing));
        assertNotEquals(original, CommandResult.createPersonDetails("feedback", ALICE));
        assertThrows(UnsupportedOperationException.class, () -> listing.entries().clear());
        assertThrows(NullPointerException.class, () -> original.withFollowUpListing(null));
        assertThrows(NullPointerException.class, () -> new FollowUpListing(null, List.of()));
    }

    @Test
    public void withSelectedFollowUp_retainsViewAndAddsActionContext() {
        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING);
        CommandResult viewed = CommandResult.createPersonDetails("feedback", ALICE);
        CommandResult result = viewed.withSelectedFollowUp(action);
        CommandResult same = viewed.withSelectedFollowUp(action);
        assertEquals(ALICE, result.getPersonForDetails().orElseThrow());
        assertEquals(action, result.getFollowUpForDetails().orElseThrow());
        assertEquals(viewed.getFeedbackToUser(), result.getFeedbackToUser());
        assertEquals(viewed.shouldSaveContactFile(), result.shouldSaveContactFile());
        assertTrue(viewed.getFollowUpForDetails().isEmpty());
        assertEquals(result, same);
        assertEquals(result.hashCode(), same.hashCode());
        assertNotEquals(result, viewed);
        assertNotEquals(result, CommandResult.createPersonDetails("feedback", BENSON).withSelectedFollowUp(action));
        assertNotEquals(result, viewed.withSelectedFollowUp(new FollowUp(2, "Other", FollowUp.Status.PENDING)));
        assertEquals(action, result.withFeedbackPrefix("Note: ").getFollowUpForDetails().orElseThrow());
        assertThrows(NullPointerException.class, () -> viewed.withSelectedFollowUp(null));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback").withSelectedFollowUp(action));
    }

    @Test
    public void equals_comparesPendingRequestAndFollowUpListing() {
        PendingRequest request = new ConfirmationRequest("preview", new OrderCommand());
        CommandResult pending = new CommandResult("feedback", request);
        assertEquals(pending, new CommandResult("feedback", request));
        assertNotEquals(pending, new CommandResult("feedback"));

        FollowUp action = new FollowUp(1, "Call", FollowUp.Status.PENDING);
        FollowUpEntry entry = new FollowUpEntry(ALICE, action);
        FollowUpListing listing = new FollowUpListing("Pending", List.of(entry));
        CommandResult withListing = new CommandResult("feedback").withFollowUpListing(listing);
        assertEquals(withListing, new CommandResult("feedback").withFollowUpListing(listing));
        assertNotEquals(withListing, new CommandResult("feedback")
                .withFollowUpListing(new FollowUpListing("Other", List.of(entry))));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));
        assertFalse(commandResult.equals(new CommandResult("feedback", false, false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, false, false).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", saveContactFile=true}";
        assertEquals(expected, commandResult.toString());
    }

    @Test
    public void withFeedbackPrefix_keepsEveryOtherField() {
        CommandResult result = new CommandResult("Previewed.", false, true, false);

        CommandResult prefixed = result.withFeedbackPrefix("Ran: undo\n");

        assertEquals("Ran: undo\nPreviewed.", prefixed.getFeedbackToUser());
        assertEquals(new CommandResult("Ran: undo\nPreviewed.", false, true, false), prefixed);
    }
}
