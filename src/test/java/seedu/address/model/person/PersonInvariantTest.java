package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

class PersonInvariantTest {
    @Test
    void removeFollowUp_corruptedDuplicateIds_throwsAssertionError() throws Exception {
        FollowUp action = new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING);
        Person person = ALICE.appendFollowUp(action);

        // Constructors reject duplicate IDs, so simulate corruption to exercise the deletion invariant.
        Field followUpsField = Person.class.getDeclaredField("followUps");
        followUpsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<FollowUp> internalFollowUps = (List<FollowUp>) followUpsField.get(person);
        internalFollowUps.add(action);

        AssertionError error = assertThrows(AssertionError.class, () -> person.removeFollowUp(1));

        assertEquals("Exactly one follow-up must be removed", error.getMessage());
        assertEquals(List.of(action, action), person.getFollowUps());
        assertEquals(1, person.getLastFollowUpId());
    }
}
