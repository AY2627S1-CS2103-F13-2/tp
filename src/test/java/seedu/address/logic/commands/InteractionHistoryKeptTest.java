package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;

/**
 * Contains tests showing that other commands keep or show a contact's interaction history.
 */
public class InteractionHistoryKeptTest {

    private static final Interaction OCT_1 = new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat");

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void edit_keepsInteractionHistory() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, OCT_1).execute(model);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withCompany("Apple").build();

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person editedAlice = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("Apple", editedAlice.getCompany().orElseThrow().toString());
        assertEquals(List.of(OCT_1), editedAlice.getInteractions());
    }

    @Test
    public void view_showsInteractionHistory() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, OCT_1).execute(model);

        String feedback = new ViewCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();

        assertTrue(feedback.startsWith(ALICE.getName().toString()));
        assertTrue(feedback.endsWith("; Interaction history:\n1. 2026-10-01: Career fair chat"));
    }

    @Test
    public void view_noInteractions_showsNoHistory() throws Exception {
        String feedback = new ViewCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();

        assertFalse(feedback.contains("Interaction history"));
    }
}
