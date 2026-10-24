package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.model.FieldOrder;
import seedu.address.model.Model;

/**
 * Queries or changes the global contact field order without changing contacts.
 */
public class OrderCommand extends Command {
    public static final String COMMAND_WORD = "order";
    public static final String MESSAGE_USAGE = "order: Shows or changes the global contact field order.\n"
            + "Parameters: no arguments, or every field exactly once.\n" + FieldOrder.MESSAGE_CONSTRAINTS + "\n"
            + "Example: order name phone email company role tags linkedin followups interactions";

    private final FieldOrder requestedOrder;

    /**
     * Creates a read-only query for the current order.
     */
    public OrderCommand() {
        requestedOrder = null;
    }

    /**
     * Creates a request to apply a complete validated order.
     */
    public OrderCommand(FieldOrder requestedOrder) {
        this.requestedOrder = requireNonNull(requestedOrder);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (requestedOrder == null) {
            return new CommandResult("Current field order: " + model.getFieldOrder());
        }
        if (requestedOrder.equals(model.getFieldOrder())) {
            return new CommandResult("Field order is unchanged. No changes needed.");
        }
        model.setFieldOrder(requestedOrder);
        return new CommandResult("Field order updated: " + requestedOrder);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OrderCommand command && Objects.equals(requestedOrder, command.requestedOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedOrder);
    }
}
