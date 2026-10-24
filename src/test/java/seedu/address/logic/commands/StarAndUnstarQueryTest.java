package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.ContactFileParser;
import seedu.address.logic.parser.FindCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Event;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class StarAndUnstarQueryTest {
    private final ContactFileParser parser = new ContactFileParser();

    @Test
    public void execute_uniqueMatch_updatesStatusAndPreservesDetails() throws Exception {
        for (boolean starred : List.of(true, false)) {
            for (String query : List.of("aLiCe", " Alice   Pauline ", "/email ALICE@example.com",
                    "/contact 94351253")) {
                Model model = new ModelManager();
                Person original = new PersonBuilder(ALICE).withStarred(!starred).build();
                model.addPerson(original);
                model.addPerson(BENSON);
                model.updateFilteredPersonList(person -> person.equals(BENSON));

                CommandResult result = parser.parseCommand(commandWord(starred) + " " + query).execute(model);

                Person expected = new PersonBuilder(ALICE).withStarred(starred).build();
                assertEquals(List.of(expected), model.getFilteredPersonList());
                assertEquals(List.of(expected, BENSON), model.getContactFile().getPersonList());
                assertEquals(String.format(starred ? StarCommand.MESSAGE_SUCCESS : UnstarCommand.MESSAGE_SUCCESS,
                        ALICE.getName()), result.getFeedbackToUser());
            }
        }
    }

    @Test
    public void execute_upstreamFindQueries_preservesEventsAndDatedFollowUps() throws Exception {
        Event event = new Event("Tech Meetup", "2026-10-20");
        FollowUp action = new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING,
                LocalDate.of(2026, 10, 25));
        for (boolean starred : List.of(true, false)) {
            for (String query : List.of("/company Acme", "/role Analyst", "/company Acme /role Analyst",
                    "/event Tech Meetup", "/fuzzy Alice Pauline")) {
                Model model = new ModelManager();
                Person original = new PersonBuilder(ALICE).withCompany("Acme").withRole("Analyst")
                        .withFollowUps(action).withStarred(!starred).build().withEvent(event);
                model.addPerson(original);
                model.addPerson(BENSON);
                model.updateFilteredPersonList(person -> person.equals(BENSON));

                parser.parseCommand(commandWord(starred) + " " + query).execute(model);

                Person updated = model.getFilteredPersonList().getFirst();
                assertEquals(List.of(original.withStarred(starred)), model.getFilteredPersonList());
                assertEquals(original.getEvents(), updated.getEvents());
                assertEquals(List.of(action), updated.getFollowUps());
                assertSame(BENSON, model.getContactFile().getPersonList().get(1));
            }
        }
    }

    @Test
    public void execute_alreadySet_doesNotReplaceContact() throws Exception {
        for (boolean starred : List.of(true, false)) {
            Model model = new ModelManager();
            Person original = new PersonBuilder(ALICE).withStarred(starred).build();
            model.addPerson(original);
            CommandResult result = parser.parseCommand(commandWord(starred) + " Alice").execute(model);
            assertSame(original, model.getContactFile().getPersonList().getFirst());
            assertEquals(String.format(
                    starred ? StarCommand.MESSAGE_ALREADY_STARRED : UnstarCommand.MESSAGE_NOT_STARRED,
                    ALICE.getName()), result.getFeedbackToUser());
        }
    }

    @Test
    public void execute_multipleMatches_displaysResultsAndAllowsIndexRetry() throws Exception {
        for (boolean starred : List.of(true, false)) {
            Model model = new ModelManager();
            Person first = new PersonBuilder(ALICE).withStarred(!starred).build();
            Person second = new PersonBuilder(BENSON).withName("Alice Tan").withStarred(!starred).build();
            model.addPerson(first);
            model.addPerson(second);
            model.updateFilteredPersonList(person -> false);
            String word = commandWord(starred);

            CommandResult result = parser.parseCommand(word + " Alice").execute(model);

            assertEquals(List.of(first, second), model.getFilteredPersonList());
            assertSame(first, model.getContactFile().getPersonList().get(0));
            assertSame(second, model.getContactFile().getPersonList().get(1));
            assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 2) + "\n"
                    + (starred ? StarCommand.MESSAGE_MULTIPLE_MATCHES : UnstarCommand.MESSAGE_MULTIPLE_MATCHES),
                    result.getFeedbackToUser());
            parser.parseCommand(word + " 2").execute(model);
            assertSame(first, model.getFilteredPersonList().get(0));
            assertEquals(new PersonBuilder(second).withStarred(starred).build(),
                    model.getFilteredPersonList().get(1));
        }
    }

    @Test
    public void execute_noMatches_returnsFindFeedbackWithoutChangingContacts() throws Exception {
        for (String word : List.of("star", "unstar")) {
            for (boolean empty : List.of(true, false)) {
                Model model = new ModelManager();
                if (!empty) {
                    model.addPerson(ALICE);
                }
                CommandResult result = parser.parseCommand(word + " Nobody").execute(model);
                assertEquals(FindCommand.MESSAGE_CONTACT_NOT_FOUND, result.getFeedbackToUser());
                assertEquals(List.of(), model.getFilteredPersonList());
                assertEquals(empty ? List.of() : List.of(ALICE), model.getContactFile().getPersonList());
            }
        }
    }

    @Test
    public void equals_comparesQueryAndCommandType() throws Exception {
        FindCommand find = new FindCommandParser().parse("Alice");
        for (boolean starred : List.of(true, false)) {
            Command command = queryCommand(find, starred);
            assertEquals(command, command);
            assertEquals(command, queryCommand(new FindCommandParser().parse("Alice"), starred));
            assertNotEquals(command, queryCommand(find, !starred));
            assertNotEquals(command, queryCommand(new FindCommandParser().parse("Benson"), starred));
            assertNotEquals(command, parser.parseCommand(commandWord(starred) + " 1"));
            assertNotEquals(parser.parseCommand(commandWord(starred) + " 1"), command);
            assertNotEquals(command, find);
            assertNotEquals(command, null);
            assertThrows(NullPointerException.class, () -> queryCommand(null, starred));
            assertThrows(NullPointerException.class, () -> command.execute(null));
        }
    }

    @Test
    public void toString_queryCommand_identifiesSearchCriteria() throws Exception {
        FindCommand find = new FindCommandParser().parse("Alice");
        assertEquals(StarCommand.class.getCanonicalName() + "{findCommand=" + find + "}",
                new StarCommand(find).toString());
        assertEquals(UnstarCommand.class.getCanonicalName() + "{findCommand=" + find + "}",
                new UnstarCommand(find).toString());
    }

    private Command queryCommand(FindCommand find, boolean starred) {
        return starred ? new StarCommand(find) : new UnstarCommand(find);
    }

    private String commandWord(boolean starred) {
        return starred ? StarCommand.COMMAND_WORD : UnstarCommand.COMMAND_WORD;
    }
}
