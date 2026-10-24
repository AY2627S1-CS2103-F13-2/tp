package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalContactFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.JsonUtil;

class FieldOrderTest {
    private static final FieldOrder CUSTOM = new FieldOrder(
            List.of("followups", "linkedin", "email", "phone", "role", "company", "tags", "name"));

    @Test
    void constructor_validFields_copiesAndCanonicalizesNames() {
        List<String> input = new ArrayList<>(FieldOrder.DEFAULT.getFieldNames());
        input.replaceAll(name -> name.toUpperCase(Locale.ROOT));
        FieldOrder order = new FieldOrder(input);
        input.clear();
        assertEquals(FieldOrder.DEFAULT, order);
        assertEquals("name tags company role phone email linkedin followups interactions", order.toString());
        assertThrows(UnsupportedOperationException.class, () -> order.getFields().clear());
        assertThrows(UnsupportedOperationException.class, () -> order.getFieldNames().clear());
    }

    @Test
    void constructor_orderSavedBeforeInteractions_placesInteractionsLast() {
        FieldOrder order = new FieldOrder(
                List.of("followups", "linkedin", "email", "phone", "role", "company", "tags", "name"));
        assertEquals(List.of("followups", "linkedin", "email", "phone", "role", "company", "tags", "name",
                "interactions"), order.getFieldNames());
    }

    @Test
    void constructor_invalidPermutation_rejectsMissingRepeatedAndUnknownFields() {
        assertThrows(NullPointerException.class, () -> new FieldOrder(null));
        for (List<String> names : List.of(List.<String>of(), List.of("name"),
                List.of("name", "tags", "company", "role", "phone", "email", "linkedin", "name"),
                List.of("name", "tags", "company", "role", "phone", "email", "linkedin", "unknown"))) {
            assertThrows(IllegalArgumentException.class, () -> new FieldOrder(names));
        }
        List<String> missing = new ArrayList<>(FieldOrder.DEFAULT.getFieldNames());
        missing.set(7, null);
        assertThrows(IllegalArgumentException.class, () -> new FieldOrder(missing));
    }

    @Test
    void equals_orderAndNames_defineValueIdentity() {
        assertEquals(CUSTOM, CUSTOM);
        FieldOrder copy = new FieldOrder(CUSTOM.getFieldNames());
        assertEquals(CUSTOM, copy);
        assertEquals(CUSTOM.hashCode(), copy.hashCode());
        assertNotEquals(CUSTOM, FieldOrder.DEFAULT);
        assertNotEquals(CUSTOM, null);
        assertNotEquals(CUSTOM, CUSTOM.getFieldNames());
    }

    @Test
    void json_roundTrip_preservesArrayOrder() throws Exception {
        String json = JsonUtil.toJsonString(CUSTOM);
        assertTrue(json.strip().startsWith("["));
        assertEquals(CUSTOM, JsonUtil.fromJsonString(json, FieldOrder.class));
        UserPrefs prefs = new UserPrefs();
        prefs.setFieldOrder(CUSTOM);
        assertEquals(prefs, JsonUtil.fromJsonString(JsonUtil.toJsonString(prefs), UserPrefs.class));
    }

    @Test
    void preferences_copyAndEquality_includeFieldOrderAndGuiSettings() {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(900, 700, 12, 34));
        prefs.setFieldOrder(CUSTOM);
        UserPrefs copy = new UserPrefs(prefs);
        assertEquals(prefs, copy);
        assertEquals(prefs.hashCode(), copy.hashCode());
        assertTrue(copy.toString().contains(CUSTOM.toString()));
        prefs.setFieldOrder(FieldOrder.DEFAULT);
        assertNotEquals(prefs, copy);
        assertEquals(CUSTOM, copy.getFieldOrder());
        assertThrows(NullPointerException.class, () -> prefs.setFieldOrder(null));
    }

    @Test
    void model_orderChange_notifiesViewsAndSnapshotRestoresPreferencesAndFilter() {
        UserPrefs prefs = new UserPrefs();
        prefs.setFieldOrder(CUSTOM);
        ModelManager model = new ModelManager(getTypicalContactFile(), prefs);
        prefs.setFieldOrder(FieldOrder.DEFAULT);
        assertEquals(CUSTOM, model.getFieldOrder());
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        ModelSnapshot snapshot = new ModelSnapshot(model);
        List<FieldOrder> changes = new ArrayList<>();
        model.getFieldOrderProperty().addListener((observable, previous, current) -> changes.add(current));
        model.setFieldOrder(FieldOrder.DEFAULT);
        model.setGuiSettings(new GuiSettings(800, 600, 1, 2));
        assertTrue(snapshot.hasPreferencesChanged(model));
        snapshot.restore(model);
        assertEquals(CUSTOM, model.getFieldOrder());
        assertEquals(CUSTOM, model.getUserPrefs().getFieldOrder());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(List.of(FieldOrder.DEFAULT, CUSTOM), changes);
        assertEquals(new GuiSettings(), model.getGuiSettings());
        assertThrows(NullPointerException.class, () -> model.setFieldOrder(null));
    }
}
