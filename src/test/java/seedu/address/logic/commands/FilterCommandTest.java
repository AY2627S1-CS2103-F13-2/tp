package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.TagFilterPredicate;
import seedu.address.model.person.TagFilterPredicate.Mode;
import seedu.address.model.tag.Tag;

class FilterCommandTest {
    @Test
    void equals_predicateValue_definesCommandIdentity() {
        TagFilterPredicate predicate = new TagFilterPredicate(Set.of(new Tag("work")), Mode.AND);
        FilterCommand command = new FilterCommand(predicate);
        assertEquals(command, command);
        assertEquals(command, new FilterCommand(new TagFilterPredicate(Set.of(new Tag("work")), Mode.AND)));
        assertNotEquals(command, new FilterCommand(new TagFilterPredicate(Set.of(new Tag("work")), Mode.OR)));
        assertNotEquals(command, null);
        assertNotEquals(command, predicate);
        assertEquals(FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}", command.toString());
    }

    @Test
    void execute_nullInputs_rejectsInvalidUsage() {
        assertThrows(NullPointerException.class, () -> new FilterCommand(null));
        FilterCommand command = new FilterCommand(new TagFilterPredicate(Set.of(new Tag("work")), Mode.OR));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
