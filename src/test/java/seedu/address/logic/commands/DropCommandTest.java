package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DropCommand.Field;
import seedu.address.logic.commands.DropCommand.FieldValue;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

public class DropCommandTest {
    private final Person original = new PersonBuilder().withName("Jane Doe")
            .withEmail("jane@example.com").withTags("old", "keep").build();
    private final ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();

    @Test
    public void execute_mss_promptsThenRemovesOnlySelectedValueAfterConfirmation() throws Exception {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        List<FieldValue> selections = List.of(new FieldValue(Field.TAG, new Tag("old")));

        CommandResult prompt = new DropCommand(Index.fromOneBased(1), selections, false).execute(model);

        assertTrue(prompt.getFeedbackToUser().contains("Remove the following detail(s) from Jane Doe?"));
        assertTrue(prompt.getFeedbackToUser().contains("Confirm? (y/n; cancel to stop)"));
        assertTrue(prompt.getPendingRequest().isPresent());
        assertFalse(prompt.shouldSaveContactFile());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());

        Person expectedPerson = new PersonBuilder(original).withTags("keep").build();
        CommandResult expectedResult = new CommandResult(
                String.format(DropCommand.MESSAGE_REMOVED, seedu.address.logic.Messages.format(expectedPerson)));

        CommandResult confirmed = prompt.getPendingRequest().orElseThrow().respond("y", model);
        assertEquals(expectedResult.getFeedbackToUser(), confirmed.getFeedbackToUser());
        assertEquals(expectedPerson, model.getContactFile().getPersonList().getFirst());
        assertEquals(original.getEmail(), expectedPerson.getEmail());
    }

    @Test
    public void execute_confirmed_keepsFollowUpsStarAndInteractions() throws Exception {
        Person rich = original.appendFollowUp(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING))
                .withStarred(true)
                .withInteraction(new Interaction(LocalDate.of(2026, 10, 1), "Career fair chat"));
        Model model = new ModelManager(new ContactFileBuilder().withPerson(rich).build(), new UserPrefs());

        new DropCommand(Index.fromOneBased(1), List.of(new FieldValue(Field.TAG, new Tag("old"))), false)
                .execute(model).getPendingRequest().orElseThrow().respond("y", model);

        Person dropped = model.getContactFile().getPersonList().getFirst();
        assertEquals(rich.getFollowUps(), dropped.getFollowUps());
        assertTrue(dropped.isStarred());
        assertEquals(rich.getInteractions(), dropped.getInteractions());
    }

    @Test
    public void execute_deletedTarget_rejectsStaleRequest() throws Exception {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        CommandResult prompt = new DropCommand(Index.fromOneBased(1),
                List.of(new FieldValue(Field.TAG, new Tag("old"))), false).execute(model);
        model.deletePerson(original);

        CommandException exception = assertThrows(CommandException.class, () ->
                prompt.getPendingRequest().orElseThrow().respond("y", model));

        assertEquals("Contacts changed. Start the drop request again.", exception.getMessage());
        assertTrue(model.getContactFile().getPersonList().isEmpty());
    }

    @Test
    public void execute_removedValue_rejectsStaleRequest() throws Exception {
        MutableTagsPerson target = new MutableTagsPerson(original);
        Model model = new ModelManager(new ContactFileBuilder().withPerson(target).build(), new UserPrefs());
        CommandResult prompt = new DropCommand(Index.fromOneBased(1),
                List.of(new FieldValue(Field.TAG, new Tag("old"))), false).execute(model);
        target.hideTags();

        CommandException exception = assertThrows(CommandException.class, () ->
                prompt.getPendingRequest().orElseThrow().respond("y", model));

        assertEquals(String.format(DropCommand.MESSAGE_VALUE_NOT_FOUND, "tag", "old", target.getName()),
                exception.getMessage());
        assertEquals(target, model.getContactFile().getPersonList().getFirst());
    }

    @Test
    public void execute_byName_removesSelectedScalarValueAndRetainsOtherFields() {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        Person expectedPerson = new PersonBuilder(original).withEmail(null).build();
        Model expectedModel = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        expectedModel.setPerson(original, expectedPerson);

        DropCommand command = new DropCommand("Jane Doe",
                List.of(new FieldValue(Field.EMAIL, new Email("jane@example.com"))), true);

        assertCommandSuccess(command, model,
                String.format(DropCommand.MESSAGE_REMOVED, seedu.address.logic.Messages.format(expectedPerson)),
                expectedModel);
        assertEquals(original.getTags(), model.getContactFile().getPersonList().getFirst().getTags());
    }

    @Test
    public void execute_removesIntroducerAndRetainsOtherDetails() {
        Person withIntroducer = new PersonBuilder(original).withIntroducer("Old Introducer").build();
        ContactFile file = new ContactFileBuilder().withPerson(withIntroducer).build();
        Model model = new ModelManager(new ContactFile(file), new UserPrefs());
        Person expectedPerson = new PersonBuilder(withIntroducer).withIntroducer(null).build();
        Model expectedModel = new ModelManager(new ContactFile(file), new UserPrefs());
        expectedModel.setPerson(withIntroducer, expectedPerson);

        DropCommand command = new DropCommand(Index.fromOneBased(1),
                List.of(new FieldValue(Field.INTRODUCER, new Name("Old Introducer"))), true);

        assertCommandSuccess(command, model,
                String.format(DropCommand.MESSAGE_REMOVED, seedu.address.logic.Messages.format(expectedPerson)),
                expectedModel);
        assertTrue(model.getContactFile().getPersonList().getFirst().getIntroducer().isEmpty());
        assertEquals(withIntroducer.getEmail(), expectedPerson.getEmail());
        assertEquals(withIntroducer.getTags(), expectedPerson.getTags());
    }

    @Test
    public void execute_removesEverySelectedFieldAndRetainsUnselectedValues() {
        Person withIntroducer = new PersonBuilder(original).withIntroducer("Old Introducer").build();
        ContactFile file = new ContactFileBuilder().withPerson(withIntroducer).build();
        Model model = new ModelManager(new ContactFile(file), new UserPrefs());
        Person expectedPerson = new PersonBuilder(withIntroducer)
                .withCompany(null)
                .withRole(null)
                .withPhone(null)
                .withEmail(null)
                .withLinkedin(null)
                .withIntroducer(null)
                .withTags("keep")
                .build();
        Model expectedModel = new ModelManager(new ContactFile(file), new UserPrefs());
        expectedModel.setPerson(withIntroducer, expectedPerson);
        List<FieldValue> selections = List.of(
                new FieldValue(Field.COMPANY, new Company(withIntroducer.getCompany().orElseThrow().value)),
                new FieldValue(Field.ROLE, new Role(withIntroducer.getRole().orElseThrow().value)),
                new FieldValue(Field.PHONE, new Phone(withIntroducer.getPhone().orElseThrow().value)),
                new FieldValue(Field.EMAIL, new Email("jane@example.com")),
                new FieldValue(Field.LINKEDIN, new Linkedin(withIntroducer.getLinkedin().orElseThrow().value)),
                new FieldValue(Field.TAG, new Tag("old")),
                new FieldValue(Field.INTRODUCER, new Name("Old Introducer")));

        assertCommandSuccess(new DropCommand(Index.fromOneBased(1), selections, true), model,
                String.format(DropCommand.MESSAGE_REMOVED, seedu.address.logic.Messages.format(expectedPerson)),
                expectedModel);
        Person actual = model.getContactFile().getPersonList().getFirst();
        assertEquals(withIntroducer.getName(), actual.getName());
        assertEquals(new Tag("keep"), actual.getTags().iterator().next());
        assertTrue(actual.getCompany().isEmpty());
        assertTrue(actual.getRole().isEmpty());
        assertTrue(actual.getPhone().isEmpty());
        assertTrue(actual.getEmail().isEmpty());
        assertTrue(actual.getLinkedin().isEmpty());
        assertTrue(actual.getIntroducer().isEmpty());
    }

    @Test
    public void execute_emptySelections_failsWithoutChangingContact() {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        assertCommandFailure(new DropCommand(Index.fromOneBased(1), List.of(), true),
                model, DropCommand.MESSAGE_NO_VALUES);
    }

    @Test
    public void execute_valueWithWrongModelType_isReportedAsNotFound() {
        List<FieldValue> selections = List.of(
                new FieldValue(Field.COMPANY, new Email("wrong@example.com")),
                new FieldValue(Field.ROLE, new Email("wrong@example.com")),
                new FieldValue(Field.PHONE, new Email("wrong@example.com")),
                new FieldValue(Field.EMAIL, new Tag("wrong")),
                new FieldValue(Field.LINKEDIN, new Email("wrong@example.com")),
                new FieldValue(Field.TAG, new Email("wrong@example.com")),
                new FieldValue(Field.INTRODUCER, new Email("wrong@example.com")));

        for (FieldValue selection : selections) {
            Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
            assertCommandFailure(new DropCommand(Index.fromOneBased(1), List.of(selection), true),
                    model, String.format(DropCommand.MESSAGE_VALUE_NOT_FOUND,
                            selection.field().getDisplayName(), selection.displayValue(), "Jane Doe"));
        }
    }

    @Test
    public void execute_unmatchedValueForEveryField_isReportedAsNotFound() {
        Person withIntroducer = new PersonBuilder(original).withIntroducer("Old Introducer").build();
        ContactFile file = new ContactFileBuilder().withPerson(withIntroducer).build();
        List<FieldValue> selections = List.of(
                new FieldValue(Field.COMPANY,
                        new Company(withIntroducer.getCompany().orElseThrow().value + " missing")),
                new FieldValue(Field.ROLE, new Role(withIntroducer.getRole().orElseThrow().value + " missing")),
                new FieldValue(Field.PHONE, new Phone("9999999999")),
                new FieldValue(Field.EMAIL, new Email(withIntroducer.getEmail().orElseThrow().value + ".missing")),
                new FieldValue(Field.LINKEDIN,
                        new Linkedin(withIntroducer.getLinkedin().orElseThrow().value + "missing")),
                new FieldValue(Field.TAG, new Tag("missing")),
                new FieldValue(Field.INTRODUCER, new Name("Missing Introducer")));

        for (FieldValue selection : selections) {
            Model model = new ModelManager(new ContactFile(file), new UserPrefs());
            assertCommandFailure(new DropCommand(Index.fromOneBased(1), List.of(selection), true),
                    model, String.format(DropCommand.MESSAGE_VALUE_NOT_FOUND,
                            selection.field().getDisplayName(), selection.displayValue(), withIntroducer.getName()));
        }
    }

    @Test
    public void execute_byNameRestoresFullListAfterRemoval() throws Exception {
        Person another = new PersonBuilder().withName("John Doe").build();
        ContactFile file = new ContactFileBuilder().withPerson(original).withPerson(another).build();
        Model model = new ModelManager(new ContactFile(file), new UserPrefs());

        new DropCommand("Jane Doe",
                List.of(new FieldValue(Field.EMAIL, new Email("jane@example.com"))), true).execute(model);

        assertEquals(file.getPersonList().size(), model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(original).withEmail(null).build(), model.getFilteredPersonList().getFirst());
        assertEquals(another, model.getFilteredPersonList().get(1));
    }

    @Test
    public void execute_missingValue_doesNotChangeContact() {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        assertCommandFailure(new DropCommand(Index.fromOneBased(1),
                        List.of(new FieldValue(Field.TAG, new Tag("missing"))), true),
                model, String.format(DropCommand.MESSAGE_VALUE_NOT_FOUND, "tag", "missing", "Jane Doe"));
    }

    @Test
    public void execute_invalidIndex_doesNotChangeContact() {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        assertCommandFailure(new DropCommand(Index.fromOneBased(2),
                        List.of(new FieldValue(Field.TAG, new Tag("old"))), true),
                model, seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyContactFile_fails() {
        Model model = new ModelManager(new ContactFile(), new UserPrefs());
        assertCommandFailure(new DropCommand(Index.fromOneBased(1),
                        List.of(new FieldValue(Field.TAG, new Tag("old"))), true),
                model, DropCommand.MESSAGE_EMPTY_CONTACT_FILE);
    }

    @Test
    public void execute_nameNotFound_failsWithoutChangingContact() {
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        assertCommandFailure(new DropCommand("Nobody",
                        List.of(new FieldValue(Field.TAG, new Tag("old"))), true),
                model, String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Nobody"));
    }

    @Test
    public void execute_ambiguousName_selectionRemovesValuesFromSelectedContact() throws CommandException {
        Person janeSmith = new PersonBuilder().withName("Jane Smith").withTags("old").build();
        ContactFile twoPeople = new ContactFileBuilder().withPerson(original).withPerson(janeSmith).build();
        Model model = new ModelManager(twoPeople, new UserPrefs());

        CommandResult selection = new DropCommand("Jane",
                List.of(new FieldValue(Field.TAG, new Tag("old"))), false).execute(model);

        assertTrue(selection.getFeedbackToUser().startsWith("2 contacts found:"));
        assertTrue(selection.getPendingRequest().isPresent());
        assertEquals(2, model.getFilteredPersonList().size());

        CommandResult result = selection.getPendingRequest().orElseThrow().respond("2", model);

        assertTrue(result.getFeedbackToUser().contains("Remove the following detail(s) from Jane Smith?"));
        assertTrue(result.getPendingRequest().isPresent());
        assertFalse(result.shouldSaveContactFile());
        assertEquals(2, model.getContactFile().getPersonList().size());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());
        assertEquals(janeSmith, model.getContactFile().getPersonList().get(1));

        result.getPendingRequest().orElseThrow().respond("y", model);

        assertEquals(original, model.getContactFile().getPersonList().getFirst());
        assertEquals(new PersonBuilder(janeSmith).withTags().build(),
                model.getContactFile().getPersonList().get(1));
    }

    @Test
    public void execute_ambiguousName_invalidSelectionKeepsRequestWithoutChangingContacts() throws Exception {
        Person janeSmith = new PersonBuilder().withName("Jane Smith").withTags("old").build();
        ContactFile twoPeople = new ContactFileBuilder().withPerson(original).withPerson(janeSmith).build();
        Model model = new ModelManager(twoPeople, new UserPrefs());

        CommandResult selection = new DropCommand("Jane",
                List.of(new FieldValue(Field.TAG, new Tag("old"))), true).execute(model);
        CommandResult invalid = selection.getPendingRequest().orElseThrow().respond("3", model);

        assertTrue(invalid.getFeedbackToUser().startsWith("Enter a number from the displayed candidates."));
        assertTrue(invalid.getPendingRequest().isPresent());
        assertEquals(twoPeople.getPersonList(), model.getContactFile().getPersonList());
    }

    @Test
    public void execute_deletedCandidate_rejectsStaleSelection() throws Exception {
        Person janeSmith = new PersonBuilder().withName("Jane Smith").withTags("old").build();
        ContactFile twoPeople = new ContactFileBuilder().withPerson(original).withPerson(janeSmith).build();
        Model model = new ModelManager(twoPeople, new UserPrefs());
        CommandResult selection = new DropCommand("Jane",
                List.of(new FieldValue(Field.TAG, new Tag("old"))), true).execute(model);
        model.deletePerson(janeSmith);

        CommandException exception = assertThrows(CommandException.class, () ->
                selection.getPendingRequest().orElseThrow().respond("2", model));

        assertEquals("Contacts changed. Start the drop request again.", exception.getMessage());
        assertEquals(List.of(original), model.getContactFile().getPersonList());
    }

    @Test
    public void equalsAndToString() {
        List<FieldValue> selections = List.of(new FieldValue(Field.TAG, new Tag("old")));
        DropCommand command = new DropCommand(Index.fromOneBased(1), selections, false);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new DropCommand(Index.fromOneBased(1), selections, false)));
        assertFalse(command.equals(new DropCommand(Index.fromOneBased(1), selections, true)));
        assertFalse(command.equals(new DropCommand("Jane Doe", selections, false)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("not a command"));
        assertTrue(command.toString().contains("targetIndex="));
        assertTrue(command.toString().contains("values="));
        assertTrue(command.toString().contains("confirmed=false"));
    }

    private static final class MutableTagsPerson extends Person {
        private boolean tagsVisible = true;

        private MutableTagsPerson(Person person) {
            super(person.getName(), person.getCompany().orElse(null), person.getRole().orElse(null),
                    person.getPhone().orElse(null), person.getEmail().orElse(null),
                    person.getLinkedin().orElse(null), person.getIntroducer().orElse(null), person.getTags());
        }

        private void hideTags() {
            tagsVisible = false;
        }

        @Override
        public Set<Tag> getTags() {
            return tagsVisible ? super.getTags() : Set.of();
        }
    }
}
