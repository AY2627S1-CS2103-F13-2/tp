package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ContactFileBuilder;

/**
 * Contains integration tests for {@code ViewCommand}.
 */
public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void execute_validIndex_success() {
        Index firstIndex = Index.fromOneBased(1);
        Person personToView = model.getFilteredPersonList().get(firstIndex.getZeroBased());

        ViewCommand command = new ViewCommand(firstIndex);

        assertCommandSuccess(command, model,
                CommandResult.createPersonDetails(Messages.format(personToView), personToView), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ViewCommand command = new ViewCommand(invalidIndex);

        assertCommandFailure(command, model, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_partialNameWithDifferentCaseAndWhitespace_showsFullDetails() throws Exception {
        assertEquals("Alice Pauline; Tags: [friends]; Company: Shopee; Role: Software Engineer; Phone: 94351253; "
                + "Email: alice@example.com; LinkedIn: linkedin.com/in/alicepauline",
                new ViewCommand("  aLiCe   pAuLiNe ").execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(getTypicalContactFile(), model.getContactFile());
    }

    @Test
    public void execute_nameOnlyContact_omitsAbsentFields() throws Exception {
        Model sparseModel = new ModelManager(new ContactFileBuilder().withPerson(NAME_ONLY).build(), new UserPrefs());
        assertEquals("Nadia Only", new ViewCommand("Nadia").execute(sparseModel).getFeedbackToUser());
    }

    @Test
    public void execute_tagFilteredIndex_showsDisplayedContact() throws Exception {
        model.updateFilteredPersonList(person -> person.getTags().contains(new Tag("owesMoney")));
        assertEquals(Messages.format(BENSON),
                new ViewCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertCommandFailure(new ViewCommand(Index.fromOneBased(2)), model, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nameHiddenByFilter_searchesAllContacts() throws Exception {
        model.updateFilteredPersonList(person -> person.getTags().contains(new Tag("owesMoney")));
        assertEquals(Messages.format(ALICE), new ViewCommand("Pauline").execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_ambiguousName_listsCandidatesForIndexRetry() throws Exception {
        assertEquals("2 contacts found:\n"
                + "1. Benson Meier \u2014 GovTech \u2014 johnd@example.com\n"
                + "2. Daniel Meier \u2014 DBS \u2014 cornelia@example.com\n"
                + "To view one of them, enter the command again with its index in place of the name.",
                new ViewCommand("Meier").execute(model).getFeedbackToUser());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
        assertEquals(Messages.format(DANIEL),
                new ViewCommand(Index.fromOneBased(2)).execute(model).getFeedbackToUser());
        assertEquals(getTypicalContactFile(), model.getContactFile());
    }

    @Test
    public void execute_unknownName_throwsCommandException() {
        assertCommandFailure(new ViewCommand("Zed"), model,
                String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed"));
    }

    @Test
    public void execute_emptyContactFile_reportsMissingContactOrInvalidIndex() {
        Model emptyModel = new ModelManager();
        assertCommandFailure(new ViewCommand("Zed"), emptyModel,
                String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed"));
        assertCommandFailure(new ViewCommand(Index.fromOneBased(1)), emptyModel,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand("Alice").execute(null));
    }

    @Test
    public void constructor_nullSelector_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand((Index) null));
        assertThrows(NullPointerException.class, () -> new ViewCommand((String) null));
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        Index secondIndex = Index.fromOneBased(2);

        ViewCommand firstCommand = new ViewCommand(firstIndex);
        ViewCommand secondCommand = new ViewCommand(secondIndex);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(new ViewCommand(firstIndex)));
        assertFalse(firstCommand.equals(secondCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(1));
        ViewCommand nameCommand = new ViewCommand("Alice");
        assertTrue(nameCommand.equals(new ViewCommand("Alice")));
        assertFalse(nameCommand.equals(new ViewCommand("Benson")));
        assertFalse(nameCommand.equals(firstCommand));
        assertFalse(firstCommand.equals(nameCommand));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        ViewCommand command = new ViewCommand(index);

        String expected = ViewCommand.class.getCanonicalName()
                + "{targetIndex=" + index + ", targetName=null}";
        assertEquals(expected, command.toString());
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=null, targetName=Alice}",
                new ViewCommand("Alice").toString());
    }
}
