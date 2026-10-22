package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.NAME_ONLY;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.OrderCommand;
import seedu.address.model.FieldOrder;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

class FieldOrderFormattingTest {
    @Test
    void format_completeContact_obeysOrderIncludingActionsAndTags() {
        Person person = ALICE.appendFollowUp(new FollowUp(1, "Send portfolio", FollowUp.Status.PENDING));
        FieldOrder order = new FieldOrder(
                List.of("followups", "phone", "email", "linkedin", "role", "company", "tags", "name"));
        String text = Messages.format(person, order);
        List<String> labels = List.of("Follow-ups:", "Phone:", "Email:", "LinkedIn:", "Role:", "Company:",
                "Tags:", ALICE.getName().fullName);
        for (int i = 1; i < labels.size(); i++) {
            assertTrue(text.indexOf(labels.get(i - 1)) < text.indexOf(labels.get(i)), text);
        }
        assertTrue(text.contains("#1 [PENDING] Send portfolio"));
        assertEquals(Messages.format(person, FieldOrder.DEFAULT), Messages.format(person));
    }

    @Test
    void format_absentFields_omitsSeparatorsAndEmptyRows() {
        FieldOrder reverse = new FieldOrder(
                List.of("followups", "linkedin", "email", "phone", "role", "company", "tags", "name"));
        assertEquals(NAME_ONLY.getName().fullName, Messages.format(NAME_ONLY, reverse));
        assertEquals(NAME_ONLY.getName().fullName, Messages.format(NAME_ONLY));
    }

    @Test
    void result_detailMetadata_preservesValueSemantics() {
        CommandResult details = CommandResult.createPersonDetails("details", ALICE);
        CommandResult copy = CommandResult.createPersonDetails("details", ALICE);
        assertEquals(ALICE, details.getPersonForDetails().orElseThrow());
        assertEquals(details, copy);
        assertEquals(details.hashCode(), copy.hashCode());
        assertNotEquals(details, new CommandResult("details"));
        assertNotEquals(details, CommandResult.createPersonDetails("details", BENSON));
        assertThrows(NullPointerException.class, () -> CommandResult.createPersonDetails("details", null));
    }

    @Test
    void command_queryAndUpdate_haveDistinctValueSemantics() {
        OrderCommand query = new OrderCommand();
        assertEquals(query, new OrderCommand());
        assertEquals(query.hashCode(), new OrderCommand().hashCode());
        assertNotEquals(query, new OrderCommand(FieldOrder.DEFAULT));
        assertNotEquals(query, null);
        assertNotEquals(query, "order");
        assertThrows(NullPointerException.class, () -> new OrderCommand(null));
        assertThrows(NullPointerException.class, () -> query.execute(null));
    }
}
