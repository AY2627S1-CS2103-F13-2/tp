package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FieldOrder;

class OrderCommandParserTest {
    private final OrderCommandParser parser = new OrderCommandParser();

    @Test
    void parse_noArguments_returnsReadOnlyQuery() throws Exception {
        assertEquals(new OrderCommand(), parser.parse(""));
        assertEquals(new OrderCommand(), parser.parse(" \t\n "));
        assertEquals(new OrderCommand(), new ContactFileParser().parseCommand("order"));
    }

    @Test
    void parse_completeOrder_acceptsCaseAndWhitespace() throws Exception {
        OrderCommand expected = new OrderCommand(FieldOrder.DEFAULT);
        assertEquals(expected, parser.parse(" NAME tags COMPANY role phone email linkedin followups "));
        assertEquals(expected, parser.parse("name\ttags company role phone email linkedin followups"));
        assertEquals(expected, new ContactFileParser().parseCommand("order " + FieldOrder.DEFAULT));
    }

    @Test
    void parse_invalidOrder_showsAcceptedFields() {
        for (String input : List.of("name", "name tags company role phone email linkedin name",
                "name tags company role phone email linkedin unknown",
                "name tags company role phone email linkedin followups extra", "field/name")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertTrue(error.getMessage().contains(FieldOrder.MESSAGE_CONSTRAINTS));
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
