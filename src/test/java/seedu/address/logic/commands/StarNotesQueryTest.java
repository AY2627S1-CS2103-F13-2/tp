package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FindCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * A failed query must never star or unstar contacts retained from an earlier search.
 */
public class StarNotesQueryTest {
    @Test
    public void execute_noMatchingNotes_keepsSingleDisplayedContactAndStarState() throws Exception {
        for (boolean starred : new boolean[] {false, true}) {
            Person alice = new PersonBuilder().withName("Alice Tan").build().withStarred(starred);
            Model model = new ModelManager(new ContactFileBuilder().withPerson(alice).build(), new UserPrefs());
            FindCommand query = new FindCommandParser().parse(" /notes noSuchNote987");
            Command command = starred ? new UnstarCommand(query) : new StarCommand(query);

            CommandResult result = command.execute(model);

            assertEquals(FindNotesCommand.MESSAGE_NO_MATCHING_NOTES, result.getFeedbackToUser());
            assertEquals(List.of(alice), model.getContactFile().getPersonList());
            assertEquals(List.of(alice), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_noMatchingNotes_doesNotReportOldListAsAmbiguous() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Tan").build();
        Person bob = new PersonBuilder().withName("Bob Lee").withEmail("bob@example.com").build();
        Model model = new ModelManager(new ContactFileBuilder().withPerson(alice).withPerson(bob).build(),
                new UserPrefs());
        FindCommand query = new FindCommandParser().parse(" /notes noSuchNote987");

        assertEquals(FindNotesCommand.MESSAGE_NO_MATCHING_NOTES,
                new StarCommand(query).execute(model).getFeedbackToUser());
        assertEquals(FindNotesCommand.MESSAGE_NO_MATCHING_NOTES,
                new UnstarCommand(query).execute(model).getFeedbackToUser());
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
    }

    @Test
    public void execute_matchingNotes_targetsMatchingContactOutsideOldView() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Tan").build();
        Person bob = new PersonBuilder().withName("Bob Lee").withEmail("bob@example.com").build()
                .withInteraction(new Interaction(LocalDate.of(2026, 10, 1), "portfolio"));
        Model model = new ModelManager(new ContactFileBuilder().withPerson(alice).withPerson(bob).build(),
                new UserPrefs());
        model.updateFilteredPersonList(alice::equals);
        FindCommand query = new FindCommandParser().parse(" /notes portfolio");

        new StarCommand(query).execute(model);

        assertFalse(model.getContactFile().getPersonList().get(0).isStarred());
        assertTrue(model.getContactFile().getPersonList().get(1).isStarred());

        new UnstarCommand(query).execute(model);

        assertFalse(model.getContactFile().getPersonList().get(1).isStarred());
    }
}
