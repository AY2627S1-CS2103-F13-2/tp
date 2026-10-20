package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Records the name of the person who introduced a contact.
 */
public class RecordCommand extends Command {

    public static final String COMMAND_WORD = "record";
    public static final String CONFIRM_OPTION = "--confirm";
    public static final String MESSAGE_NAME_REQUIRED = "An introducer's name is required.";
    public static final String MESSAGE_RECORDED = "Recorded introducer for contact:\n%1$s";
    public static final String MESSAGE_CONFIRM_REPLACEMENT =
            "The existing introducer for %1$s is %2$s. To replace it with %3$s, run: "
                    + COMMAND_WORD + " %4$d " + PREFIX_NAME + "%3$s " + CONFIRM_OPTION;
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records who introduced a contact.\n"
            + "Parameters: INDEX " + PREFIX_NAME + "NAME [" + CONFIRM_OPTION + "]\n"
            + "Use " + CONFIRM_OPTION + " to confirm replacing an existing introducer.\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_NAME + "Jane Doe";

    private final Index targetIndex;
    private final Name introducer;
    private final boolean confirmed;

    /**
     * Creates a RecordCommand.
     *
     * @param targetIndex index of the contact in the filtered person list
     * @param introducer name of the person who introduced the contact
     * @param confirmed whether replacement of an existing introducer is confirmed
     */
    public RecordCommand(Index targetIndex, Name introducer, boolean confirmed) {
        requireNonNull(targetIndex);
        requireNonNull(introducer);

        this.targetIndex = targetIndex;
        this.introducer = introducer;
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(targetIndex.getZeroBased());
        if (personToUpdate.getIntroducer().isPresent() && !confirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRM_REPLACEMENT,
                    personToUpdate.getName(), personToUpdate.getIntroducer().orElseThrow(), introducer,
                    targetIndex.getOneBased()));
        }

        Person updatedPerson = new Person(personToUpdate.getName(),
                personToUpdate.getCompany().orElse(null),
                personToUpdate.getRole().orElse(null),
                personToUpdate.getPhone().orElse(null),
                personToUpdate.getEmail().orElse(null),
                personToUpdate.getLinkedin().orElse(null),
                introducer,
                personToUpdate.getTags());
        model.setPerson(personToUpdate, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_RECORDED, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RecordCommand otherRecordCommand)) {
            return false;
        }
        return targetIndex.equals(otherRecordCommand.targetIndex)
                && introducer.equals(otherRecordCommand.introducer)
                && confirmed == otherRecordCommand.confirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("introducer", introducer)
                .add("confirmed", confirmed)
                .toString();
    }
}
