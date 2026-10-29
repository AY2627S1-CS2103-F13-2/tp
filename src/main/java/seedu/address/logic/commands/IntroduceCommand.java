package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;

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
public class IntroduceCommand extends Command {

    public static final String COMMAND_WORD = "introduce";
    public static final String MESSAGE_NAME_REQUIRED = "An introducer's name is required.";
    public static final String MESSAGE_RECORDED = "Recorded introducer for contact:\n%1$s";
    public static final String MESSAGE_ALREADY_RECORDED = "%1$s is already recorded as the introducer for %2$s.";
    public static final String MESSAGE_CONFIRM_REPLACEMENT =
            "The existing introducer for %1$s is %2$s. Replace it with %3$s?";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records who introduced a contact.\n"
            + "Parameters: INDEX or NAME " + PREFIX_INTRODUCER + "INTRODUCER\n"
            + "INDEX is the contact's number in the displayed list.\n"
            + "Example: " + COMMAND_WORD + " John Doe " + PREFIX_INTRODUCER + "Jane Smith\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_INTRODUCER + "Jane Smith";

    private final Index targetIndex;
    private final String targetName;
    private final Name introducer;
    private final boolean confirmed;

    /**
     * Creates an IntroduceCommand.
     *
     * @param targetIndex index of the contact in the filtered person list
     * @param introducer name of the person who introduced the contact
     * @param confirmed whether replacement of an existing introducer is confirmed
     */
    public IntroduceCommand(Index targetIndex, Name introducer, boolean confirmed) {
        requireNonNull(targetIndex);
        requireNonNull(introducer);

        this.targetIndex = targetIndex;
        this.targetName = null;
        this.introducer = introducer;
        this.confirmed = confirmed;
    }

    /**
     * Creates an IntroduceCommand that finds the contact by name.
     *
     * @param targetName full or partial name of the contact to update
     * @param introducer name of the person who introduced the contact
     * @param confirmed whether replacement of an existing introducer is confirmed
     */
    public IntroduceCommand(String targetName, Name introducer, boolean confirmed) {
        requireNonNull(targetName);
        requireNonNull(introducer);

        this.targetIndex = null;
        this.targetName = targetName;
        this.introducer = introducer;
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToUpdate;
        if (targetName == null) {
            List<Person> lastShownList = model.getFilteredPersonList();
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            personToUpdate = lastShownList.get(targetIndex.getZeroBased());
        } else {
            switch (PersonNameResolver.resolve(model, targetName, COMMAND_WORD)) {
                case PersonNameResolver.Outcome.Unique unique -> personToUpdate = unique.person();
                case PersonNameResolver.Outcome.Several several -> {
                    List<Person> matches = List.copyOf(model.getFilteredPersonList());
                    PendingRequest request = new PersonSelectionRequest(matches,
                            person -> recordIntroducer(model, person));
                    return new CommandResult(request.getPrompt(), request);
                }
            }
        }

        return recordIntroducer(model, personToUpdate);
    }

    private CommandResult recordIntroducer(Model model, Person personToUpdate) {
        if (personToUpdate.getIntroducer().filter(introducer::equals).isPresent()) {
            return new CommandResult(String.format(MESSAGE_ALREADY_RECORDED, introducer, personToUpdate.getName()));
        }
        if (personToUpdate.getIntroducer().isPresent() && !confirmed) {
            String preview = String.format(MESSAGE_CONFIRM_REPLACEMENT,
                    personToUpdate.getName(), personToUpdate.getIntroducer().orElseThrow(), introducer);
            PendingRequest request = new ConfirmationRequest(preview,
                    new ConfirmedIntroduceCommand(personToUpdate));
            return new CommandResult(request.getPrompt(), request);
        }

        return applyIntroducer(model, personToUpdate);
    }

    private CommandResult applyIntroducer(Model model, Person personToUpdate) {
        Person updatedPerson = new Person(personToUpdate.getName(),
                personToUpdate.getCompany().orElse(null),
                personToUpdate.getRole().orElse(null),
                personToUpdate.getPhone().orElse(null),
                personToUpdate.getEmail().orElse(null),
                personToUpdate.getLinkedin().orElse(null),
                introducer,
                personToUpdate.getTags(),
                personToUpdate.getEvents(),
                personToUpdate.getFollowUps(),
                personToUpdate.isStarred(),
                personToUpdate.getInteractions(), personToUpdate.getLastFollowUpId());
        model.setPerson(personToUpdate, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_RECORDED, Messages.format(updatedPerson)));
    }

    private final class ConfirmedIntroduceCommand extends Command {
        private final Person target;

        private ConfirmedIntroduceCommand(Person target) {
            this.target = target;
        }

        @Override
        public CommandResult execute(Model model) throws CommandException {
            if (!model.getContactFile().getPersonList().contains(target)) {
                throw new CommandException("Contacts changed. Start the introduce request again.");
            }
            return applyIntroducer(model, target);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof IntroduceCommand otherIntroduceCommand)) {
            return false;
        }
        return Objects.equals(targetIndex, otherIntroduceCommand.targetIndex)
                && Objects.equals(targetName, otherIntroduceCommand.targetName)
                && introducer.equals(otherIntroduceCommand.introducer)
                && confirmed == otherIntroduceCommand.confirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetName", targetName)
                .add("introducer", introducer)
                .add("confirmed", confirmed)
                .toString();
    }
}
