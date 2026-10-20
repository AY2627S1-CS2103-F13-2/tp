package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.List;

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
        assertTrue(prompt.getFeedbackToUser().contains("drop 1 t/old --confirm"));
        assertFalse(prompt.shouldSaveContactFile());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());

        Person expectedPerson = new PersonBuilder(original).withTags("keep").build();
        Model expectedModel = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        expectedModel.setPerson(original, expectedPerson);
        CommandResult expectedResult = new CommandResult(
                String.format(DropCommand.MESSAGE_REMOVED, seedu.address.logic.Messages.format(expectedPerson)));

        assertCommandSuccess(new DropCommand(Index.fromOneBased(1), selections, true), model,
                expectedResult, expectedModel);
        assertEquals(expectedPerson, model.getContactFile().getPersonList().getFirst());
        assertEquals(original.getEmail(), expectedPerson.getEmail());
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
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        assertCommandFailure(new DropCommand(Index.fromOneBased(1),
                        List.of(new FieldValue(Field.TAG, new Email("wrong@example.com"))), true),
                model, String.format(DropCommand.MESSAGE_VALUE_NOT_FOUND,
                        "tag", "wrong@example.com", "Jane Doe"));
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
    public void execute_ambiguousName_doesNotChangeContactsOrRequestSave() throws CommandException {
        Person janeSmith = new PersonBuilder().withName("Jane Smith").withTags("old").build();
        ContactFile twoPeople = new ContactFileBuilder().withPerson(original).withPerson(janeSmith).build();
        Model model = new ModelManager(twoPeople, new UserPrefs());

        CommandResult result = new DropCommand("Jane",
                List.of(new FieldValue(Field.TAG, new Tag("old"))), false).execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("2 contacts found:"));
        assertFalse(result.shouldSaveContactFile());
        assertEquals(2, model.getContactFile().getPersonList().size());
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
}
