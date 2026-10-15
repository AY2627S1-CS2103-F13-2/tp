package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ContactFile;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableContactFileTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableContactFileTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsContactFile.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonContactFile.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonContactFile.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableContactFile dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableContactFile.class).get();
        ContactFile contactFileFromFile = dataFromFile.toModelType();
        ContactFile typicalPersonsContactFile = TypicalPersons.getTypicalContactFile();
        assertEquals(contactFileFromFile, typicalPersonsContactFile);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableContactFile dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableContactFile.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableContactFile dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableContactFile.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableContactFile.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

}
