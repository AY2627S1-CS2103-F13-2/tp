package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.FieldOrder;

/**
 * Parses a read-only query or a complete case-insensitive field permutation.
 */
public class OrderCommandParser implements Parser<OrderCommand> {
    @Override
    public OrderCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new OrderCommand();
        }
        try {
            return new OrderCommand(new FieldOrder(List.of(args.strip().split("\\s+"))));
        } catch (IllegalArgumentException e) {
            throw new ParseException(FieldOrder.MESSAGE_CONSTRAINTS + "\n" + OrderCommand.MESSAGE_USAGE, e);
        }
    }
}
