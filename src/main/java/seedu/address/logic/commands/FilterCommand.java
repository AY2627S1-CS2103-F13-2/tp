package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.TagFilterPredicate;

/**
 * Replaces the displayed results with contacts matching an AND or OR tag filter.
 */
public class FilterCommand extends Command {
    public static final String COMMAND_WORD = "filter";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists contacts matching all requested tags (AND) or any requested tag (OR).\n"
            + "Parameters: [mode/and|mode/or] t/TAG [t/TAG]...\n"
            + "Default: AND. Tags are case-sensitive; mode values ignore case.\n"
            + "Examples: filter t/work t/friend; filter mode/or t/work t/friend";
    public static final String MESSAGE_NO_MATCHES = "No contacts match the requested tags.";
    public static final String MESSAGE_MATCHES = "%1$d contact(s) match the %2$s tag filter.";

    private final TagFilterPredicate predicate;

    /**
     * Creates a read-only command using the validated tag predicate.
     */
    public FilterCommand(TagFilterPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        assert model.getFilteredPersonList().stream().allMatch(predicate)
                : "Every displayed contact must satisfy the requested tag filter";
        int count = model.getFilteredPersonList().size();
        return new CommandResult(count == 0 ? MESSAGE_NO_MATCHES
                : String.format(MESSAGE_MATCHES, count, predicate.getMode()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FilterCommand command && predicate.equals(command.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
