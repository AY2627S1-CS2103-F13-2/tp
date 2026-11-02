package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ConfirmationRequest;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyContactFile;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonContactFileStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class NumericContactSelectionWorkflowTest {
    private static final List<String> NAMES = List.of("Alpha", "Beta", "Carol", "David", "Eve", "Frank", "Zara");
    private static final List<String> COMMANDS = List.of("view 7", "edit 7 c/Changed", "delete 7", "drop 7 t/keep",
            "introduce 7 i/New Introducer", "log 7 d/2020-01-02 nt/New note", "editlog 7 i/1 nt/Changed note",
            "unlog 7 i/1", "star 7", "unstar 7");

    @TempDir
    public Path folder;

    @Test
    void execute_numericSelector_allCommandsOfferBothTargetsAndKeepTheirConfirmationSteps() throws Exception {
        for (String command : COMMANDS) {
            for (String choice : List.of("1", "2")) {
                Session session = session(7, 0, command.startsWith("unstar"));
                List<Person> original = List.copyOf(session.model.getContactFile().getPersonList());
                byte[] bytes = Files.readAllBytes(session.file);
                CommandResult result = session.logic.execute(command);
                assertTrue(result.getFeedbackToUser().contains("2 contacts found:"), command);
                assertFalse(result.getPendingRequest().orElseThrow() instanceof ConfirmationRequest, command);
                assertEquals(List.of(original.getFirst(), original.getLast()), session.model.getFilteredPersonList());
                assertTrue(session.logic.execute("3").getPendingRequest().isPresent(), command);
                assertEquals(original, session.model.getContactFile().getPersonList());
                assertArrayEquals(bytes, Files.readAllBytes(session.file));
                assertEquals(0, session.storage.saves);

                Person selected = choice.equals("1") ? original.getFirst() : original.getLast();
                Person other = choice.equals("1") ? original.getLast() : original.getFirst();
                result = session.logic.execute(choice);
                assertEquals(List.of("edit", "delete", "drop", "introduce", "unlog")
                        .contains(command.split(" ")[0]), result.getPendingRequest().isPresent(), command);
                if (result.getPendingRequest().isPresent()) {
                    assertTrue(result.getPendingRequest().orElseThrow() instanceof ConfirmationRequest, command);
                    assertEquals(original, session.model.getContactFile().getPersonList());
                    assertEquals(0, session.storage.saves);
                    session.logic.execute("y");
                }
                assertTrue(session.model.getContactFile().getPersonList().contains(other), command);
                if (command.equals("view 7")) {
                    assertEquals(selected, result.getPersonForDetails().orElseThrow());
                    assertEquals(original, session.model.getContactFile().getPersonList());
                    assertEquals(0, session.storage.saves);
                } else if (command.equals("delete 7")) {
                    assertFalse(session.model.getContactFile().getPersonList().contains(selected), command);
                    assertEquals(6, session.model.getContactFile().getPersonList().size());
                    assertEquals(1, session.storage.saves);
                } else {
                    Person updated = session.model.getContactFile().getPersonList().stream()
                            .filter(person -> person.getName().equals(selected.getName())).findFirst().orElseThrow();
                    assertNotEquals(selected, updated, command);
                    assertTrue(session.model.getFilteredPersonList().contains(updated), command);
                    assertEquals(1, session.storage.saves, command);
                }
                assertEquals(session.model.getContactFile(), session.storage.readContactFile().orElseThrow());
            }
        }
    }

    @Test
    void execute_selectedEmailRemoval_keepsTheContactVisibleAfterItsIdentityChanges() throws Exception {
        Session session = session(7, 0, false);
        session.logic.execute("drop 7 e/person6@example.com");
        session.logic.execute("2");
        session.logic.execute("y");
        assertEquals(1, session.model.getFilteredPersonList().size());
        Person selected = session.model.getFilteredPersonList().getFirst();
        assertEquals("Zara", selected.getName().fullName);
        assertTrue(selected.getEmail().isEmpty());
        assertEquals(1, session.storage.saves);
    }

    @Test
    void execute_onlyNumericNameMatches_allCommandsReachItWithoutAnIndex() throws Exception {
        for (String command : COMMANDS) {
            Session session = session(6, 0, command.startsWith("unstar"));
            CommandResult result = session.logic.execute(command);
            if (result.getPendingRequest().isPresent()) {
                assertTrue(result.getPendingRequest().orElseThrow() instanceof ConfirmationRequest, command);
                session.logic.execute("cancel");
            } else if (command.equals("view 7")) {
                assertEquals("7 Eleven", result.getPersonForDetails().orElseThrow().getName().fullName);
            } else {
                assertEquals(1, session.storage.saves, command);
            }
        }
    }

    @Test
    void execute_severalNamesContainTheNumber_listsEveryMatchOnce() throws Exception {
        Session session = session(7, 0, false);
        session.logic.execute("add n/7 Ada");
        session.storage.saves = 0;
        byte[] bytes = Files.readAllBytes(session.file);
        assertTrue(session.logic.execute("view 7").getFeedbackToUser().contains("3 contacts found:"));
        assertEquals("7 Ada", session.logic.execute("3").getPersonForDetails().orElseThrow().getName().fullName);
        assertArrayEquals(bytes, Files.readAllBytes(session.file));
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_contactChangesBeforeSelection_rejectsItWithoutSavingAnotherContact() throws Exception {
        Session session = session(7, 0, false);
        byte[] bytes = Files.readAllBytes(session.file);
        session.logic.execute("delete 7");
        session.model.deletePerson(session.model.getContactFile().getPersonList().getFirst());
        CommandException failure = assertThrows(CommandException.class, () -> session.logic.execute("1"));
        assertEquals("Contact changed. Start the command again.", failure.getMessage());
        assertArrayEquals(bytes, Files.readAllBytes(session.file));
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_selectedLastContactIsRemoved_remainingUpdatesKeepTheViewEmpty() throws Exception {
        Session session = session(7, 0, false);
        session.logic.execute("view 7");
        Person selected = session.logic.execute("2").getPersonForDetails().orElseThrow();
        session.model.deletePerson(selected);
        Person remaining = session.model.getContactFile().getPersonList().getFirst();
        session.model.setPerson(remaining, new PersonBuilder(remaining).withCompany("Changed").build());
        assertTrue(session.model.getFilteredPersonList().isEmpty());
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_filteredIndexAndHiddenNumericName_useTheOriginalDisplayedIndex() throws Exception {
        Session session = session(7, 0, false);
        session.logic.execute("add n/Hector");
        session.storage.saves = 0;
        session.model.updateFilteredPersonList(person -> !person.getName().fullName.equals("7 Eleven"));
        List<Person> before = List.copyOf(session.model.getFilteredPersonList());
        byte[] bytes = Files.readAllBytes(session.file);
        session.logic.execute("view 7");
        assertEquals(List.of("7 Eleven", "Hector"), session.model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        session.logic.execute("cancel");
        assertEquals(before, session.model.getFilteredPersonList());
        session.logic.execute("view 7");
        assertEquals("Hector", session.logic.execute("2").getPersonForDetails().orElseThrow().getName().fullName);
        assertArrayEquals(bytes, Files.readAllBytes(session.file));
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_candidateNumber_doesNotStartAnotherNumericNameSearch() throws Exception {
        Session session = session(7, 0, false);
        Person first = session.model.getContactFile().getPersonList().getFirst();
        Person second = session.model.getContactFile().getPersonList().get(1);
        Person target = new PersonBuilder(first).withName("17 Eleven").build();
        session.model.setPerson(first, target);
        session.model.setPerson(second, new PersonBuilder(second).withName("1 Ada").build());
        CommandResult choices = session.logic.execute("view 7");
        assertTrue(choices.getFeedbackToUser().contains("2 contacts found:"));
        CommandResult chosen = session.logic.execute("1");
        assertTrue(chosen.getPendingRequest().isEmpty());
        assertEquals(target, chosen.getPersonForDetails().orElseThrow());
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_sameContactMatchesIndexAndName_doesNotAskForSelection() throws Exception {
        Session session = session(7, 6, false);
        CommandResult result = session.logic.execute("view 7");
        assertTrue(result.getPendingRequest().isEmpty());
        assertEquals("7 Eleven", result.getPersonForDetails().orElseThrow().getName().fullName);
        assertEquals(7, session.model.getFilteredPersonList().size());
        assertEquals(0, session.storage.saves);
    }

    @Test
    void execute_cancelSelectionOrConfirmation_restoresContactsFilterAndFile() throws Exception {
        for (String reply : List.of("n", "cancel")) {
            Session session = session(7, 0, false);
            List<Person> before = List.copyOf(session.model.getFilteredPersonList());
            byte[] bytes = Files.readAllBytes(session.file);
            session.logic.execute("delete 7");
            session.logic.execute(reply);
            assertEquals(before, session.model.getFilteredPersonList());
            session.logic.execute("delete 7");
            session.logic.execute("2");
            session.logic.execute(reply);
            assertEquals(before, session.model.getFilteredPersonList());
            assertEquals(before, session.model.getContactFile().getPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(session.file));
            assertEquals(0, session.storage.saves);
        }
    }

    @Test
    void execute_selectedEditAndDelete_preserveUndoAndHistory() throws Exception {
        for (String command : List.of("edit 7 c/Changed", "delete 7")) {
            Session session = session(7, 0, false);
            List<Person> before = List.copyOf(session.model.getContactFile().getPersonList());
            session.logic.execute(command);
            session.logic.execute("2");
            session.logic.execute("y");
            assertTrue(session.logic.execute("undo").getPendingRequest().isPresent());
            session.logic.execute("y");
            assertEquals(before, session.model.getContactFile().getPersonList());
            assertTrue(session.logic.execute("history").getFeedbackToUser().contains("(undone)"));
            assertEquals(2, session.storage.saves);
        }
    }

    @Test
    void execute_selectedSaveFails_restoresOriginalContactsAndFile() throws Exception {
        Session session = session(7, 0, false);
        List<Person> before = List.copyOf(session.model.getContactFile().getPersonList());
        byte[] bytes = Files.readAllBytes(session.file);
        session.logic.execute("edit 7 c/Changed");
        session.logic.execute("1");
        session.storage.fail = true;
        assertThrows(CommandException.class, () -> session.logic.execute("y"));
        assertEquals(before, session.model.getContactFile().getPersonList());
        assertEquals(before, session.model.getFilteredPersonList());
        assertArrayEquals(bytes, Files.readAllBytes(session.file));
    }

    @Test
    void execute_shortcut_expandsBeforeMatchingBothTargets() throws Exception {
        Session session = session(7, 0, false);
        session.logic.execute("alias v view");
        assertTrue(session.logic.execute("v 7").getFeedbackToUser().startsWith("Ran: view 7\n"));
        assertEquals("Zara", session.logic.execute("2").getPersonForDetails().orElseThrow().getName().fullName);
        assertEquals(0, session.storage.saves);
    }

    private Session session(int count, int numericNamePosition, boolean starred) throws Exception {
        ModelManager model = new ModelManager();
        for (int index = 0; index < count; index++) {
            String name = index == numericNamePosition ? "7 Eleven" : NAMES.get(index);
            Person person = new PersonBuilder().withName(name).withPhone("9000000" + (index + 1))
                    .withEmail("person" + index + "@example.com").withTags("keep")
                    .withIntroducer("Old Introducer").withStarred(starred).build()
                    .withInteractions(List.of(new Interaction(LocalDate.of(2020, 1, 1), "Original note")));
            model.addPerson(person);
        }
        Path path = Files.createTempDirectory(folder, "selection-");
        Path file = path.resolve("contacts.json");
        CountingStorage storage = new CountingStorage(file);
        storage.saveContactFile(model.getContactFile());
        storage.saves = 0;
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(path.resolve("preferences.json"))));
        return new Session(model, storage, logic, file);
    }

    private record Session(ModelManager model, CountingStorage storage, LogicManager logic, Path file) { }

    private static class CountingStorage extends JsonContactFileStorage {
        private int saves;
        private boolean fail;

        CountingStorage(Path file) {
            super(file);
        }

        @Override
        public void saveContactFile(ReadOnlyContactFile contacts) throws IOException {
            saves++;
            if (fail) {
                throw new IOException("Simulated save failure");
            }
            super.saveContactFile(contacts);
        }
    }
}
