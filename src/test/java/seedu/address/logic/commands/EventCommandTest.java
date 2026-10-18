package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class EventCommandTest {

    private final Model model =
            new ModelManager(new seedu.address.model.ContactFile(), new UserPrefs());

    @BeforeEach
    public void setUp() {
        model.addPerson(new PersonBuilder().build());
    }

    @Test
    public void execute_validEvent_addsEventToPerson() throws Exception {
        Event event = new Event("NUS Career Fair", "2026-10-20");
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        EventCommand command = new EventCommand(INDEX_FIRST_PERSON, event);
        command.execute(model);

        Person updatedPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        assertTrue(updatedPerson.hasEvent(event));
        assertEquals(originalPerson.getName(), updatedPerson.getName());
    }

    @Test
    public void execute_duplicateEvent_throwsCommandException() throws Exception {
        Event event = new Event("NUS Career Fair", "2026-10-20");
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        model.setPerson(originalPerson, originalPerson.withEvent(event));

        EventCommand command = new EventCommand(INDEX_FIRST_PERSON, event);

        assertThrows(CommandException.class, () -> command.execute(model));
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Event event = new Event("NUS Career Fair", "2026-10-20");
        Index invalidIndex = Index.fromOneBased(999);
        EventCommand command = new EventCommand(invalidIndex, event);

        assertThrows(CommandException.class, () -> command.execute(model));
    }

    @Test
    public void equals() {
        Event firstEvent = new Event("NUS Career Fair", "2026-10-20");
        Event secondEvent = new Event("Tech Conference", "2026-11-15");

        EventCommand firstCommand =
                new EventCommand(INDEX_FIRST_PERSON, firstEvent);
        EventCommand secondCommand =
                new EventCommand(INDEX_FIRST_PERSON, secondEvent);

        assertTrue(firstCommand.equals(firstCommand));
        assertFalse(firstCommand.equals(secondCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(1));
    }

    @Test
    public void toStringMethod() {
        Event event = new Event("NUS Career Fair", "2026-10-20");
        EventCommand command = new EventCommand(INDEX_FIRST_PERSON, event);

        String expected = EventCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", event=" + event + "}";

        assertEquals(expected, command.toString());
    }
}
