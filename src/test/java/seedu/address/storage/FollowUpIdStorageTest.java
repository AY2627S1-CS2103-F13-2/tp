package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.IntFunction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ContactFile;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

class FollowUpIdStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    void toModelType_legacyMetadata_infersGreatestExistingId() throws Exception {
        for (String counter : List.of("", ",\"lastFollowUpId\":null")) {
            Person person = readPerson("{\"name\":\"Alice\",\"followUps\":["
                    + "{\"id\":9,\"description\":\"Call\",\"status\":\"PENDING\"}]" + counter + "}");
            assertEquals(9, person.getLastFollowUpId());
        }
        assertEquals(0, readPerson("{\"name\":\"Alice\"}").getLastFollowUpId());
    }

    @Test
    void toModelType_invalidCounter_rejectsCoercionOverflowAndLostHistory() {
        for (String counter : List.of("-1", "8", "1.5", "\"9\"", "true", "[]", "{}", "2147483648")) {
            String json = "{\"name\":\"Alice\",\"lastFollowUpId\":" + counter + ",\"followUps\":["
                    + "{\"id\":9,\"description\":\"Call\",\"status\":\"PENDING\"}]}";
            assertThrows(IllegalValueException.class, () -> readPerson(json), counter);
        }
        assertThrows(IllegalValueException.class, () -> readPerson("{\"name\":\"Alice\",\"lastFollowUpId\":-1}"));
    }

    @Test
    void storage_deletedHistory_preservesCounterThroughRecoveryAndRestart() throws Exception {
        Person retired = ALICE.appendFollowUp(new FollowUp(9, "Call", FollowUp.Status.PENDING)).removeFollowUp(9);
        ContactFile contacts = new ContactFile();
        contacts.addPerson(retired);
        Path path = testFolder.resolve("contacts.json");
        JsonContactFileStorage storage = new JsonContactFileStorage(path);
        storage.saveContactFile(contacts);
        assertTrue(Files.readString(path).contains("\"lastFollowUpId\" : 9"));
        assertEquals(retired, storage.readContactFile().orElseThrow().getPersonList().getFirst());
        String valid = JsonUtil.toJsonString(new JsonAdaptedPerson(retired));
        Files.writeString(path, "{\"persons\":[" + valid + ",{\"name\":\"Invalid\",\"lastFollowUpId\":-1}]}");
        StartupLoadResult recovered = storage.loadForStartup();
        assertEquals(List.of(retired), recovered.contacts());
        assertEquals(List.of(retired), storage.readContactFile().orElseThrow().getPersonList());
    }

    @Test
    void person_retiredIds_affectEqualityAndCannotBeReused() {
        Person retired = ALICE.appendFollowUp(new FollowUp(9, "Call", FollowUp.Status.PENDING)).removeFollowUp(9);
        assertNotEquals(ALICE, retired);
        assertEquals(ALICE.getFollowUps(), retired.getFollowUps());
        assertEquals(ALICE.isSamePerson(retired), retired.isSamePerson(ALICE));
        assertTrue(ALICE.isSamePerson(retired));
        assertThrows(IllegalArgumentException.class, () ->
                retired.appendFollowUp(new FollowUp(9, "Again", FollowUp.Status.PENDING)));
        assertThrows(IllegalArgumentException.class, () -> retired.removeFollowUp(9));
        Person withAction = retired.appendFollowUp(new FollowUp(10, "Next", FollowUp.Status.PENDING));
        assertThrows(IllegalArgumentException.class, () ->
                withAction.replaceFollowUp(10, new FollowUp(11, "Replacement", FollowUp.Status.PENDING)));
    }

    @Test
    void constructor_invalidCounter_rejectsInconsistentAllocatedIds() {
        FollowUp action = new FollowUp(9, "Call", FollowUp.Status.PENDING);
        IntFunction<Person> construct = counter -> new Person(ALICE.getName(),
                ALICE.getCompany().orElse(null), ALICE.getRole().orElse(null), ALICE.getPhone().orElse(null),
                ALICE.getEmail().orElse(null), ALICE.getLinkedin().orElse(null), null, ALICE.getTags(),
                ALICE.getEvents(), List.of(action), false, ALICE.getInteractions(), counter);
        assertThrows(IllegalArgumentException.class, () -> construct.apply(-1));
        assertThrows(IllegalArgumentException.class, () -> construct.apply(8));
        assertEquals(9, construct.apply(9).getLastFollowUpId());
    }

    private Person readPerson(String json) throws Exception {
        return JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
    }
}
