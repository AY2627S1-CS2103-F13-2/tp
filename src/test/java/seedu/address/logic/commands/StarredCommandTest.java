package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class StarredCommandTest {

    private final Person starredAlice = new PersonBuilder(ALICE).withStarred(true).build();
    private final Person starredNameOnly = new PersonBuilder(NAME_ONLY).withStarred(true).build();
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(starredAlice);
        model.addPerson(BENSON);
        model.addPerson(starredNameOnly);
    }

    @Test
    public void execute_mixedContacts_showsStarredInStoredOrderWithoutChangingData() {
        ContactFile original = new ContactFile(model.getContactFile());

        CommandResult result = new StarredCommand().execute(model);

        assertEquals(String.format(StarredCommand.MESSAGE_SUCCESS, 2), result.getFeedbackToUser());
        assertEquals(List.of(starredAlice, starredNameOnly), model.getFilteredPersonList());
        assertEquals(original, model.getContactFile());
        assertSame(starredAlice, model.getFilteredPersonList().getFirst());
        assertSame(starredNameOnly, model.getFilteredPersonList().getLast());
    }

    @Test
    public void execute_emptyContactFile_reportsNoStarredContacts() {
        Model emptyModel = new ModelManager();

        CommandResult result = new StarredCommand().execute(emptyModel);

        assertEquals(StarredCommand.MESSAGE_NO_STARRED_CONTACTS, result.getFeedbackToUser());
        assertEquals(List.of(), emptyModel.getFilteredPersonList());
        assertEquals(new ContactFile(), emptyModel.getContactFile());
    }

    @Test
    public void execute_noStarredContacts_clearsDisplayedListWithoutChangingData() {
        Model unstarredModel = new ModelManager(getTypicalContactFile(), new UserPrefs());
        ContactFile original = new ContactFile(unstarredModel.getContactFile());

        CommandResult result = new StarredCommand().execute(unstarredModel);

        assertEquals(StarredCommand.MESSAGE_NO_STARRED_CONTACTS, result.getFeedbackToUser());
        assertEquals(List.of(), unstarredModel.getFilteredPersonList());
        assertEquals(original, unstarredModel.getContactFile());
    }

    @Test
    public void execute_previousFilterExcludesStarredContacts_showsAllStarredContacts() {
        model.updateFilteredPersonList(person -> person.equals(BENSON));

        new StarredCommand().execute(model);

        assertEquals(List.of(starredAlice, starredNameOnly), model.getFilteredPersonList());
    }

    @Test
    public void execute_previousFilterHasNoMatches_showsAllStarredContacts() {
        model.updateFilteredPersonList(person -> false);

        new StarredCommand().execute(model);

        assertEquals(List.of(starredAlice, starredNameOnly), model.getFilteredPersonList());
    }

    @Test
    public void execute_unstarInStarredView_removesDisplayedContactAndUpdatesEmptyResult() throws Exception {
        new StarredCommand().execute(model);

        new UnstarCommand(INDEX_SECOND_PERSON).execute(model);

        assertEquals(List.of(starredAlice), model.getFilteredPersonList());
        assertEquals(List.of(starredAlice, BENSON, NAME_ONLY), model.getContactFile().getPersonList());
        assertEquals(String.format(StarredCommand.MESSAGE_SUCCESS, 1),
                new StarredCommand().execute(model).getFeedbackToUser());

        new UnstarCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(StarredCommand.MESSAGE_NO_STARRED_CONTACTS,
                new StarredCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICE, BENSON, NAME_ONLY), model.getContactFile().getPersonList());
    }

    @Test
    public void execute_listAfterStarred_restoresAllContacts() {
        new StarredCommand().execute(model);

        new ListCommand().execute(model);

        assertEquals(List.of(starredAlice, BENSON, starredNameOnly), model.getFilteredPersonList());
    }

    @Test
    public void execute_starThenRetrieve_includesNewlyStarredContact() throws Exception {
        new StarCommand(INDEX_SECOND_PERSON).execute(model);
        Person starredBenson = new PersonBuilder(BENSON).withStarred(true).build();

        CommandResult result = new StarredCommand().execute(model);

        assertEquals(String.format(StarredCommand.MESSAGE_SUCCESS, 3), result.getFeedbackToUser());
        assertEquals(List.of(starredAlice, starredBenson, starredNameOnly), model.getFilteredPersonList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StarredCommand().execute(null));
    }
}
