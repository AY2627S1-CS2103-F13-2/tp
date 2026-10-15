package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Unit tests for {@code PersonNameResolver}, the one place that decides what naming a contact means.
 */
public class PersonNameResolverTest {

    private final Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());

    @Test
    public void resolve_fullName_returnsUniqueMatch() throws Exception {
        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "Alice Pauline", "delete");

        assertEquals(new PersonNameResolver.Outcome.Unique(ALICE), outcome);
    }

    @Test
    public void resolve_partialNameMatchingOnePerson_returnsUniqueMatch() throws Exception {
        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "Pauline", "delete");

        assertEquals(new PersonNameResolver.Outcome.Unique(ALICE), outcome);
    }

    @Test
    public void resolve_differentCaseAndPadding_returnsUniqueMatch() throws Exception {
        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "  aLiCe   pAuLiNe ", "delete");

        assertEquals(new PersonNameResolver.Outcome.Unique(ALICE), outcome);
    }

    @Test
    public void resolve_uniqueMatch_narrowsDisplayedListToThatPerson() throws Exception {
        PersonNameResolver.resolve(model, "Alice", "delete");

        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void resolve_severalMatches_returnsNumberedCandidateList() throws Exception {
        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "Meier", "delete");

        String expected = "2 contacts found:\n"
                + "1. Benson Meier \u2014 GovTech \u2014 johnd@example.com\n"
                + "2. Daniel Meier \u2014 DBS \u2014 cornelia@example.com\n"
                + "To delete one of them, enter the command again with its index in place of the name.";
        assertEquals(new PersonNameResolver.Outcome.Several(expected), outcome);
    }

    @Test
    public void resolve_severalMatches_namesTheCommandTheUserTyped() throws Exception {
        PersonNameResolver.Outcome editOutcome = PersonNameResolver.resolve(model, "Meier", "edit");

        PersonNameResolver.Outcome.Several several =
                assertInstanceOf(PersonNameResolver.Outcome.Several.class, editOutcome);
        assertEquals("To edit one of them, enter the command again with its index in place of the name.",
                several.message().lines().toList().get(3));
    }

    @Test
    public void resolve_severalMatches_showsTheMatchesAndNothingElse() throws Exception {
        PersonNameResolver.resolve(model, "Meier", "delete");

        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void resolve_matchWithoutCompanyOrEmail_listsNameOnly() throws Exception {
        Person firstNameOnly = new PersonBuilder().withName("Nadia One")
                .withCompany(null).withRole(null).withPhone(null).withEmail(null).withLinkedin(null).build();
        Person secondNameOnly = new PersonBuilder().withName("Nadia Two")
                .withCompany(null).withRole(null).withPhone(null).withEmail(null).withLinkedin(null).build();
        Model nameOnlyModel = new ModelManager(new ContactFileBuilder()
                .withPerson(firstNameOnly).withPerson(secondNameOnly).build(), new UserPrefs());

        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(nameOnlyModel, "Nadia", "delete");

        String expected = "2 contacts found:\n"
                + "1. Nadia One\n"
                + "2. Nadia Two\n"
                + "To delete one of them, enter the command again with its index in place of the name.";
        assertEquals(new PersonNameResolver.Outcome.Several(expected), outcome);
    }

    @Test
    public void resolve_noMatch_throwsCommandException() {
        String expectedMessage = String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Zed");

        assertThrows(CommandException.class, expectedMessage, () ->
                PersonNameResolver.resolve(model, "Zed", "delete"));
    }

    @Test
    public void resolve_noMatch_showsEveryContactAgain() {
        assertThrows(CommandException.class, () -> PersonNameResolver.resolve(model, "Zed", "delete"));

        assertEquals(getTypicalContactFile().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void resolve_whileAnotherFilterIsApplied_searchesEveryContact() throws Exception {
        // Only Alice is on display, but naming Carl still reaches him.
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));

        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "Carl Kurz", "delete");

        assertEquals(new PersonNameResolver.Outcome.Unique(CARL), outcome);
    }

    @Test
    public void resolve_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PersonNameResolver.resolve(null, "Alice", "delete"));
        assertThrows(NullPointerException.class, () -> PersonNameResolver.resolve(model, null, "delete"));
        assertThrows(NullPointerException.class, () -> PersonNameResolver.resolve(model, "Alice", null));
    }

    @Test
    public void resolve_doesNotLeakLiveListIntoOutcome() throws Exception {
        // The resolved person must survive the displayed list being filtered away underneath it,
        // since callers go on to change the contact file while holding onto the outcome.
        PersonNameResolver.Outcome outcome = PersonNameResolver.resolve(model, "Alice Pauline", "delete");
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        assertEquals(new PersonNameResolver.Outcome.Unique(ALICE), outcome);
    }
}
