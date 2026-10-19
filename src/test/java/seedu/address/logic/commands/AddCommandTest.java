package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactFile;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_personAcceptedWithUnicodeInName_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().withName("小明").build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_personAcceptedWithApostrophiesInName_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().withName("Steven's").build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_personAcceptedWithHyphens_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().withName("John-Doe").build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_personAcceptedWithDots_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().withName("John.Doe").build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person validPerson = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(validPerson);
        ModelStub modelStub = new ModelStubWithPerson(validPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_duplicateEmail_throwsCommandException() {
        Person existing = new PersonBuilder().withName("Alice").withPhone("91111111")
                .withEmail("shared@example.com").build();
        Person toAdd = new PersonBuilder().withName("Bob").withPhone("92222222")
                .withEmail("shared@example.com").build();

        AddCommand addCommand = new AddCommand(toAdd);
        ModelStub modelStub = new ModelStubWithPerson(existing);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_EMAIL, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_personWithoutEmailAndExistingPersonWithoutEmail_addSuccessful() throws Exception {
        Model model = new ModelManager();
        Person existing = new PersonBuilder().withName("Alice").withPhone("81111111").withEmail(null).build();
        model.addPerson(existing);
        Person toAdd = new PersonBuilder().withName("YC").withPhone("82222222").withEmail(null).build();

        CommandResult commandResult = new AddCommand(toAdd).execute(model);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(toAdd)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(existing, toAdd), model.getFilteredPersonList());
    }

    @Test
    public void execute_duplicatePhoneWithoutParenthesis_throwsCommandException() {
        Person existing = new PersonBuilder().withName("Alice").withPhone("9191111111")
                .withEmail("alice@example.com").build();
        Person toAdd = new PersonBuilder().withName("Bob").withPhone("9191111111")
                .withEmail("bob@example.com").build();

        AddCommand addCommand = new AddCommand(toAdd);
        ModelStub modelStub = new ModelStubWithPerson(existing);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PHONE, () -> addCommand.execute(modelStub));
    }

    @Test
    public void execute_duplicatePhoneWithParenthesis_throwsCommandException() {
        Person existing = new PersonBuilder().withName("Alice").withPhone("9191111111")
                .withEmail("alice@example.com").build();
        Person toAdd = new PersonBuilder().withName("Bob").withPhone("(91)91111111")
                .withEmail("bob@example.com").build();

        AddCommand addCommand = new AddCommand(toAdd);
        ModelStub modelStub = new ModelStubWithPerson(existing);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PHONE, () -> addCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        AddCommand addCommand = new AddCommand(ALICE);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setContactFile(ReadOnlyContactFile newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyContactFile getContactFile() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasEmail(Person person) {
            throw new AssertionError("This method should not be calles");
        }

        @Override
        public boolean hasPhone(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Predicate<Person> getPersonFilter() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }

        @Override
        public boolean hasEmail(Person person) {
            requireNonNull(person);
            return this.person.hasSameEmail(person);
        }

        @Override
        public boolean hasPhone(Person person) {
            requireNonNull(person);
            return this.person.hasSamePhone(person);
        }
    }

    /**
     * A Model stub that always accepts the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::isSamePerson);
        }

        @Override
        public boolean hasEmail(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::hasSameEmail);
        }

        @Override
        public boolean hasPhone(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::hasSamePhone);
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public ReadOnlyContactFile getContactFile() {
            return new ContactFile();
        }
    }

}
