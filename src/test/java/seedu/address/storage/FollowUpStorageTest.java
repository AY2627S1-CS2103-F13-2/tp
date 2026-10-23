package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ContactFile;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class FollowUpStorageTest {
    private static final String VALID_ACTION =
            "{\"id\": 7, \"description\": \"Send portfolio\", \"status\": \"PENDING\"}";

    @TempDir
    public Path testFolder;

    @Test
    void storage_actionHistory_preservesIdsStatusOrderAndOptionalFields() throws Exception {
        Person person = new PersonBuilder(ALICE).withCompany(null).withFollowUps(
                new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(2026, 10, 25)),
                new FollowUp(2, "Call", FollowUp.Status.COMPLETED, LocalDate.of(2026, 10, 20)),
                new FollowUp(9, "Check in", FollowUp.Status.PENDING)).build();
        ContactFile contacts = new ContactFile();
        contacts.addPerson(person);
        Path path = testFolder.resolve("contacts.json");
        JsonContactFileStorage storage = new JsonContactFileStorage(path);
        storage.saveContactFile(contacts);
        assertEquals(contacts, storage.readContactFile().orElseThrow());
        byte[] saved = Files.readAllBytes(path);
        StartupLoadResult startup = storage.loadForStartup();
        assertEquals(List.of(person), startup.contacts());
        assertEquals(StartupLoadResult.Source.STORED, startup.source());
        assertArrayEquals(saved, Files.readAllBytes(path));
        assertTrue(Files.readString(path).contains("COMPLETED"));
        assertTrue(Files.readString(path).contains("2026-10-25"));
        assertTrue(Files.readString(path).contains("2026-10-20"));
        assertFalse(Files.readString(path).contains("isOverdue"));
    }

    @Test
    void toModelType_missingOrNullDueDate_loadsWithoutDueDate() throws Exception {
        for (String action : List.of(VALID_ACTION, withDueDate("null"))) {
            FollowUp loaded = JsonUtil.fromJsonString(action, JsonAdaptedFollowUp.class).toModelType();
            assertEquals(new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING), loaded);
            assertTrue(loaded.getDueDate().isEmpty());
            assertFalse(loaded.isOverdue());
        }
    }

    @Test
    void toModelType_nullConstructorFields_validatesRequiredFieldsAndAllowsAbsentDate() throws Exception {
        IntNode id = IntNode.valueOf(7);
        TextNode description = TextNode.valueOf("Send portfolio");
        TextNode status = TextNode.valueOf("PENDING");
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedFollowUp(null, description, status, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedFollowUp(id, null, status, null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                new JsonAdaptedFollowUp(id, description, null, null).toModelType());
        assertTrue(new JsonAdaptedFollowUp(id, description, status, null).toModelType().getDueDate().isEmpty());
    }

    @Test
    void storage_replacedDueDate_preservesNewDateAcrossRestart() throws Exception {
        Path path = testFolder.resolve("replaced-date.json");
        ContactFile contacts = new ContactFile();
        contacts.addPerson(new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(2026, 10, 25))).build());
        JsonContactFileStorage storage = new JsonContactFileStorage(path);
        storage.saveContactFile(contacts);
        contacts.setPerson(contacts.getPersonList().getFirst(), new PersonBuilder(ALICE).withFollowUps(
                new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(2026, 11, 1))).build());
        storage.saveContactFile(contacts);
        assertEquals(List.copyOf(contacts.getPersonList()),
                new JsonContactFileStorage(path).loadForStartup().contacts());
        assertFalse(Files.readString(path).contains("2026-10-25"));
    }

    @Test
    void storage_legacyActionWithoutDueDate_loadsAndSavesSuccessfully() throws Exception {
        Path path = testFolder.resolve("legacy.json");
        Files.writeString(path, "{\"persons\":[{\"name\":\"Alice\",\"followUps\":[" + VALID_ACTION + "]}]}");
        JsonContactFileStorage storage = new JsonContactFileStorage(path);
        ContactFile contacts = new ContactFile(storage.readContactFile().orElseThrow());
        assertTrue(contacts.getPersonList().getFirst().getFollowUps().getFirst().getDueDate().isEmpty());
        storage.saveContactFile(contacts);
        assertEquals(contacts, new JsonContactFileStorage(path).readContactFile().orElseThrow());
    }

    @Test
    void storage_invalidDueDate_strictReadFailsWithoutChangingFile() throws Exception {
        Path path = testFolder.resolve("invalid-date.json");
        String json = "{\"persons\":[{\"name\":\"Alice\",\"followUps\":["
                + withDueDate("\"2026-02-30\"") + "]}]}";
        Files.writeString(path, json);
        assertThrows(DataLoadingException.class, () -> new JsonContactFileStorage(path).readContactFile());
        assertEquals(json, Files.readString(path));
    }

    @Test
    void toModelType_validDueDate_preservesDateAndRecomputesOverdue() throws Exception {
        FollowUp pending = JsonUtil.fromJsonString(withDueDate("\"2000-02-29\""),
                JsonAdaptedFollowUp.class).toModelType();
        assertEquals(LocalDate.of(2000, 2, 29), pending.getDueDate().orElseThrow());
        assertTrue(pending.isOverdue());
        FollowUp completed = JsonUtil.fromJsonString(
                withDueDate("\"2000-02-29\"").replace("PENDING", "COMPLETED"),
                JsonAdaptedFollowUp.class).toModelType();
        assertFalse(completed.isOverdue());
    }

    @Test
    void toModelType_invalidDueDates_rejectsInsteadOfCoercing() throws Exception {
        for (String date : List.of("123", "true", "[]", "{}", "\"\"", "\"2026-02-29\"",
                "\"2026-04-31\"", "\"2026-13-01\"", "\"25/10/2026\"", "\"25 Oct 2026\"",
                "\"2026-10-25T12:00:00\"", "\" 2026-10-25\"", "\"2026-10-25 \"",
                "\"2026-00-01\"", "\"2026-01-00\"", "\"2026-1-1\"")) {
            JsonAdaptedFollowUp adapted = JsonUtil.fromJsonString(withDueDate(date), JsonAdaptedFollowUp.class);
            assertThrows(IllegalValueException.class, adapted::toModelType, date);
        }
    }

    @Test
    void recovery_invalidDueDate_skipsContactAndPreservesValidDatedAction() throws Exception {
        String json = "{\"persons\":[{\"name\":\"Broken\",\"followUps\":[" + withDueDate("\"2026-02-30\"")
                + "]},{\"name\":\"Valid\",\"followUps\":[" + withDueDate("\"2026-10-25\"") + "]}]}";
        ContactRecoveryResult result = new ContactRecoveryParser().parse(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(List.of(1), result.skippedContacts().stream().map(SkippedContact::recordNumber).toList());
        assertEquals(List.of(new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING, LocalDate.of(2026, 10, 25))),
                result.recoveredContacts().getFirst().getFollowUps());
    }

    private static String withDueDate(String jsonValue) {
        return VALID_ACTION.substring(0, VALID_ACTION.length() - 1) + ",\"dueDate\":" + jsonValue + "}";
    }

    @Test
    void toModelType_legacyOrNullHistory_defaultsToEmptyList() throws Exception {
        for (String json : List.of("{\"name\":\"Alice\"}",
                "{\"name\":\"Alice\",\"followUps\":null}",
                "{\"name\":\"Alice\",\"followUps\":[]}")) {
            assertTrue(JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType().getFollowUps().isEmpty());
        }
    }

    @Test
    void toModelType_invalidActionFields_rejectsInsteadOfCoercing() throws Exception {
        for (String action : List.of("{}", "{\"id\":null}",
                "{\"id\":0,\"description\":\"Call\",\"status\":\"PENDING\"}",
                "{\"id\":-1,\"description\":\"Call\",\"status\":\"PENDING\"}",
                "{\"id\":2147483648,\"description\":\"Call\",\"status\":\"PENDING\"}",
                "{\"id\":1.5,\"description\":\"Call\",\"status\":\"PENDING\"}",
                "{\"id\":\"1\",\"description\":\"Call\",\"status\":\"PENDING\"}",
                "{\"id\":1,\"status\":\"PENDING\"}",
                "{\"id\":1,\"description\":null,\"status\":\"PENDING\"}",
                "{\"id\":1,\"description\":123,\"status\":\"PENDING\"}",
                "{\"id\":1,\"description\":\"  \",\"status\":\"PENDING\"}",
                "{\"id\":1,\"description\":\"Call\"}",
                "{\"id\":1,\"description\":\"Call\",\"status\":null}",
                "{\"id\":1,\"description\":\"Call\",\"status\":true}",
                "{\"id\":1,\"description\":\"Call\",\"status\":\"UNKNOWN\"}")) {
            JsonAdaptedFollowUp adapted = JsonUtil.fromJsonString(action, JsonAdaptedFollowUp.class);
            assertThrows(IllegalValueException.class, adapted::toModelType, action);
        }
    }

    @Test
    void toModelType_nullOrDuplicateActionIds_rejectsWholeContact() throws Exception {
        for (String actions : List.of("[null]", "[" + VALID_ACTION + "," + VALID_ACTION + "]")) {
            JsonAdaptedPerson person = JsonUtil.fromJsonString(
                    "{\"name\":\"Alice\",\"followUps\":" + actions + "}", JsonAdaptedPerson.class);
            assertThrows(IllegalValueException.class, person::toModelType, actions);
        }
    }

    @Test
    void recovery_invalidHistory_skipsWholeRecordAndPreservesValidHistory() throws Exception {
        for (String actions : List.of("{}", "5", "\"invalid\"", "[null]", "[{}]", "[false]",
                "[" + VALID_ACTION + "," + VALID_ACTION + "]")) {
            String json = "{\"persons\":[{\"name\":\"Broken\",\"followUps\":" + actions
                    + "},{\"name\":\"Valid\",\"followUps\":[" + VALID_ACTION + "]}]}";
            ContactRecoveryResult result = new ContactRecoveryParser().parse(json.getBytes(StandardCharsets.UTF_8));
            assertEquals(List.of(1), result.skippedContacts().stream().map(SkippedContact::recordNumber).toList());
            assertFalse(result.skippedContacts().getFirst().reason().isBlank());
            assertEquals(List.of(new FollowUp(7, "Send portfolio", FollowUp.Status.PENDING)),
                    result.recoveredContacts().getFirst().getFollowUps());
        }
    }

    @Test
    void startup_recovery_archivesOriginalAndRetainsValidActions() throws Exception {
        String json = "{\"persons\":[{\"name\":\"Broken\",\"followUps\":["
                + withDueDate("\"2026-02-30\"") + "]},"
                + "{\"name\":\"Valid\",\"followUps\":[" + withDueDate("\"2026-10-25\"") + "]}]}";
        Path file = testFolder.resolve("contacts.json");
        Files.writeString(file, json);
        StartupLoadResult result = new JsonContactFileStorage(file).loadForStartup();
        assertEquals(StartupLoadResult.Source.RECOVERED, result.source());
        assertEquals(json, Files.readString(result.recoveryArchive().orElseThrow().backupFile()));
        assertEquals(7, result.contacts().getFirst().getFollowUps().getFirst().getId());
        assertEquals(LocalDate.of(2026, 10, 25),
                result.contacts().getFirst().getFollowUps().getFirst().getDueDate().orElseThrow());
        assertEquals(result.contacts(),
                new JsonContactFileStorage(file).readContactFile().orElseThrow().getPersonList());
    }
}
