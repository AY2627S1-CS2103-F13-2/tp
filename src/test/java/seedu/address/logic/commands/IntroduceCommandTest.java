package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.testutil.ContactFileBuilder;
import seedu.address.testutil.PersonBuilder;

public class IntroduceCommandTest {

    @Test
    public void execute_validIntroducerNotInContactFile_recordsAndDisplaysContact() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        Person target = model.getFilteredPersonList().get(0);
        Person updated = new PersonBuilder(target).withIntroducer("Someone Outside Astra").build();
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        IntroduceCommand command = new IntroduceCommand(Index.fromOneBased(1),
                new Name("Someone Outside Astra"), false);

        assertCommandSuccess(command, model, String.format(IntroduceCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_existingIntroducer_confirmsWithYesWithoutChangingContactBeforeReply() throws Exception {
        Person original = new PersonBuilder().withIntroducer("Old Introducer").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();
        Model model = new ModelManager(contactFile, new UserPrefs());

        IntroduceCommand command = new IntroduceCommand(Index.fromOneBased(1), new Name("New Introducer"), false);

        String expectedMessage = String.format(IntroduceCommand.MESSAGE_CONFIRM_REPLACEMENT,
                original.getName(), original.getIntroducer().orElseThrow(), "New Introducer");
        CommandResult prompt = command.execute(model);
        assertEquals(expectedMessage + "\nConfirm? (y/n; cancel to stop)", prompt.getFeedbackToUser());
        assertTrue(prompt.getPendingRequest().isPresent());
        assertEquals(original, model.getContactFile().getPersonList().getFirst());

        CommandResult confirmed = prompt.getPendingRequest().orElseThrow().respond("y", model);
        assertTrue(confirmed.getFeedbackToUser().contains("New Introducer"));
        assertEquals(new Name("New Introducer"),
                model.getContactFile().getPersonList().getFirst().getIntroducer().orElseThrow());
    }

    @Test
    public void execute_deletedTarget_rejectsStaleRequest() throws Exception {
        Person original = new PersonBuilder().withIntroducer("Old Introducer").build();
        Model model = new ModelManager(new ContactFileBuilder().withPerson(original).build(), new UserPrefs());
        CommandResult prompt = new IntroduceCommand(Index.fromOneBased(1),
                new Name("New Introducer"), false).execute(model);
        model.deletePerson(original);

        CommandException exception = assertThrows(CommandException.class, () ->
                prompt.getPendingRequest().orElseThrow().respond("y", model));

        assertEquals("Contacts changed. Start the introduce request again.", exception.getMessage());
        assertTrue(model.getContactFile().getPersonList().isEmpty());
    }

    @Test
    public void execute_confirmedReplacement_replacesAndDisplaysContact() {
        Person original = new PersonBuilder().withIntroducer("Old Introducer").build();
        Person updated = new PersonBuilder(original).withIntroducer("New Introducer").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        Model expectedModel = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        expectedModel.setPerson(original, updated);

        IntroduceCommand command = new IntroduceCommand(Index.fromOneBased(1), new Name("New Introducer"), true);

        assertCommandSuccess(command, model,
                String.format(IntroduceCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        IntroduceCommand command = new IntroduceCommand(Index.fromOneBased(model.getFilteredPersonList().size() + 1),
                new Name("Jane Doe"), false);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_uniquePartialName_recordsIntroducerAndRestoresFullList() throws Exception {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        Person target = model.getContactFile().getPersonList().get(0);
        Person updated = new PersonBuilder(target).withIntroducer("Jane Smith").build();
        Model expectedModel = new ModelManager(new ContactFile(model.getContactFile()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        IntroduceCommand command = new IntroduceCommand(target.getName().fullName.substring(0, 4),
                new Name("Jane Smith"), false);

        assertCommandSuccess(command, model, String.format(IntroduceCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_ambiguousName_selectingIndexUpdatesOnlySelectedContact() throws Exception {
        Person first = new PersonBuilder().withName("Alex Chen").withEmail("alex.chen@example.com").build();
        Person second = new PersonBuilder().withName("Alex Chan").withEmail("alex.chan@example.com").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(first).withPerson(second).build();
        Model model = new ModelManager(contactFile, new UserPrefs());

        CommandResult result = new IntroduceCommand("Alex", new Name("Jane Smith"), false).execute(model);

        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(first, model.getFilteredPersonList().get(0));
        assertEquals(second, model.getFilteredPersonList().get(1));
        assertTrue(result.getFeedbackToUser().contains("2 contacts found:"));
        assertTrue(result.getFeedbackToUser().contains("Enter a contact number"));
        assertTrue(result.getPendingRequest().isPresent());
        assertFalse(model.getContactFile().getPersonList().get(0).getIntroducer().isPresent());

        CommandResult invalid = result.getPendingRequest().orElseThrow().respond("3", model);
        assertTrue(invalid.getPendingRequest().isPresent());
        assertFalse(model.getContactFile().getPersonList().get(1).getIntroducer().isPresent());

        CommandResult selected = result.getPendingRequest().orElseThrow().respond("2", model);
        assertTrue(selected.getFeedbackToUser().contains("Alex Chan"));
        assertFalse(selected.getPendingRequest().isPresent());
        assertFalse(model.getContactFile().getPersonList().get(0).getIntroducer().isPresent());
        assertEquals(new Name("Jane Smith"),
                model.getContactFile().getPersonList().get(1).getIntroducer().orElseThrow());
    }

    @Test
    public void execute_ambiguousName_replacementRequestsConfirmation() throws Exception {
        Person first = new PersonBuilder().withName("Alex Chen").withEmail("alex.chen@example.com").build();
        Person second = new PersonBuilder().withName("Alex Chan").withEmail("alex.chan@example.com")
                .withIntroducer("Old Introducer").build();
        Model model = new ModelManager(new ContactFileBuilder().withPerson(first).withPerson(second).build(),
                new UserPrefs());

        CommandResult selection = new IntroduceCommand("Alex", new Name("New Introducer"), false).execute(model);
        CommandResult chosen = selection.getPendingRequest().orElseThrow().respond("2", model);

        assertEquals(String.format(IntroduceCommand.MESSAGE_CONFIRM_REPLACEMENT,
                second.getName(), "Old Introducer", "New Introducer")
                + "\nConfirm? (y/n; cancel to stop)", chosen.getFeedbackToUser());
        assertTrue(chosen.getPendingRequest().isPresent());
        assertEquals(new Name("Old Introducer"),
                model.getContactFile().getPersonList().get(1).getIntroducer().orElseThrow());

        chosen.getPendingRequest().orElseThrow().respond("y", model);
        assertEquals(new Name("New Introducer"),
                model.getContactFile().getPersonList().get(1).getIntroducer().orElseThrow());
    }

    @Test
    public void execute_nameNotFound_throwsExceptionAndRestoresFullList() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        String expectedMessage = String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, "Unknown");

        assertCommandFailure(new IntroduceCommand("Unknown", new Name("Jane Smith"), false),
                model, expectedMessage);
        assertEquals(getTypicalContactFile().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameTargetWhileFiltered_updatesNamedContactAndRestoresList() {
        Model model = new ModelManager(getTypicalContactFile(), new UserPrefs());
        Person target = model.getContactFile().getPersonList().stream()
                .filter(person -> person.getName().equals(new Name("Carl Kurz")))
                .findFirst().orElseThrow();
        Person updated = new PersonBuilder(target).withIntroducer("Jane Smith").build();
        model.updateFilteredPersonList(person -> person.getName().equals(new Name("Alice Pauline")));
        Model expectedModel = new ModelManager(new ContactFile(getTypicalContactFile()), new UserPrefs());
        expectedModel.setPerson(target, updated);

        IntroduceCommand command = new IntroduceCommand("Carl", new Name("Jane Smith"), false);

        assertCommandSuccess(command, model, String.format(IntroduceCommand.MESSAGE_RECORDED, Messages.format(updated)),
                expectedModel);
    }

    @Test
    public void execute_nameTargetWithExistingIntroducer_replacesAfterYesResponse() throws Exception {
        Person original = new PersonBuilder().withName("Jane Contact").withIntroducer("Old Introducer").build();
        ContactFile contactFile = new ContactFileBuilder().withPerson(original).build();
        Model model = new ModelManager(new ContactFile(contactFile), new UserPrefs());
        Person updated = new PersonBuilder(original).withIntroducer("New Introducer").build();

        IntroduceCommand requestConfirmation =
                new IntroduceCommand("Jane", new Name("New Introducer"), false);
        String confirmationMessage = String.format(IntroduceCommand.MESSAGE_CONFIRM_REPLACEMENT,
                original.getName(), original.getIntroducer().orElseThrow(), "New Introducer")
                + "\nConfirm? (y/n; cancel to stop)";
        CommandResult prompt = requestConfirmation.execute(model);
        assertEquals(confirmationMessage, prompt.getFeedbackToUser());
        assertTrue(prompt.getPendingRequest().isPresent());
        CommandResult confirmed = prompt.getPendingRequest().orElseThrow().respond("y", model);
        assertEquals(String.format(IntroduceCommand.MESSAGE_RECORDED, Messages.format(updated)),
                confirmed.getFeedbackToUser());
        assertEquals(updated, model.getContactFile().getPersonList().getFirst());
    }

    @Test
    public void equals() {
        Index firstIndex = Index.fromOneBased(1);
        Index secondIndex = Index.fromOneBased(2);
        Name janeDoe = new Name("Jane Doe");
        Name johnDoe = new Name("John Doe");

        IntroduceCommand command = new IntroduceCommand(firstIndex, janeDoe, false);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new IntroduceCommand(firstIndex, janeDoe, false)));
        assertFalse(command.equals(new IntroduceCommand(secondIndex, janeDoe, false)));
        assertFalse(command.equals(new IntroduceCommand(firstIndex, johnDoe, false)));
        assertFalse(command.equals(new IntroduceCommand(firstIndex, janeDoe, true)));
        assertFalse(command.equals(new IntroduceCommand("Jane", janeDoe, false)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertTrue(new IntroduceCommand("Jane", janeDoe, false)
                .equals(new IntroduceCommand("Jane", janeDoe, false)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        Name introducer = new Name("Jane Doe");
        IntroduceCommand command = new IntroduceCommand(index, introducer, true);

        String expected = IntroduceCommand.class.getCanonicalName()
                + "{targetIndex=" + index
                + ", targetName=null"
                + ", introducer=" + introducer
                + ", confirmed=true}";
        assertEquals(expected, command.toString());
    }
}
