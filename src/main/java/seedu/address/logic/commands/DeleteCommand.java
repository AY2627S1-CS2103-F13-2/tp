package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ContactFieldPredicate;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/**
 * Resolves contacts by displayed index, name, phone, or email batch and requests confirmation.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Previews contacts and asks for y/n confirmation before deleting.\n"
            + "Parameters: INDEX (positive integer), NAME, /contact PHONE, or /email EMAIL [EMAIL]...\n"
            + "Examples: delete 1; delete John Doe; delete /contact 98765432; "
            + "delete /email john@example.com jane@example.com";
    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_EMPTY_CONTACT_FILE = "There are no contacts to delete.";
    public static final String MESSAGE_FIELD_NOT_FOUND = "No contact with %1$s %2$s found. Nothing deleted.";
    public static final String MESSAGE_FIELD_AMBIGUOUS =
            "More than one contact has %1$s %2$s. Use an index or name. Nothing deleted.";

    private final Index targetIndex;
    private final String targetName;
    private final Phone targetPhone;
    private final List<Email> targetEmails;

    /**
     * Creates a deletion request for a displayed contact index.
     */
    public DeleteCommand(Index targetIndex) {
        this(requireNonNull(targetIndex), null, null, List.of());
    }

    /**
     * Creates a deletion request using the shared partial-name matching rule.
     */
    public DeleteCommand(String targetName) {
        this(null, requireNonNull(targetName), null, List.of());
    }

    /**
     * Creates a deletion request for a complete normalized phone number.
     */
    public DeleteCommand(Phone targetPhone) {
        this(null, null, requireNonNull(targetPhone), List.of());
    }

    /**
     * Creates a deletion request for a non-empty batch of validated email addresses.
     */
    public DeleteCommand(List<Email> targetEmails) {
        this(null, null, null, List.copyOf(targetEmails));
        if (targetEmails.isEmpty()) {
            throw new IllegalArgumentException("At least one email is required.");
        }
    }

    private DeleteCommand(Index targetIndex, String targetName, Phone targetPhone, List<Email> targetEmails) {
        this.targetIndex = targetIndex;
        this.targetName = targetName;
        this.targetPhone = targetPhone;
        this.targetEmails = targetEmails;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.getContactFile().getPersonList().isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_CONTACT_FILE);
        }

        PendingRequest request;
        if (targetIndex != null) {
            if (targetIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            request = createConfirmation(List.of(model.getFilteredPersonList().get(targetIndex.getZeroBased())), model);
        } else if (targetName != null) {
            List<Person> matches = PersonNameResolver.findMatches(model, targetName);
            if (matches.isEmpty()) {
                throw new CommandException(String.format(PersonNameResolver.MESSAGE_NAME_NOT_FOUND, targetName));
            }
            if (matches.size() > 1) {
                model.updateFilteredPersonList(matches::contains);
                request = new PersonSelectionRequest(matches, person -> {
                    PendingRequest confirmation = createConfirmation(List.of(person), model);
                    return new CommandResult(confirmation.getPrompt(), confirmation);
                });
            } else {
                request = createConfirmation(matches, model);
            }
        } else if (targetPhone != null) {
            request = createConfirmation(List.of(resolveField(model, ContactFieldPredicate.Field.PHONE,
                    targetPhone.value)), model);
        } else {
            Set<Person> targets = new LinkedHashSet<>();
            for (Email email : targetEmails) {
                targets.add(resolveField(model, ContactFieldPredicate.Field.EMAIL, email.value));
            }
            request = createConfirmation(List.copyOf(targets), model);
        }
        return new CommandResult(request.getPrompt(), request);
    }

    private Person resolveField(Model model, ContactFieldPredicate.Field field, String value) throws CommandException {
        List<Person> matches = model.getContactFile().getPersonList().stream()
                .filter(new ContactFieldPredicate(field, value)).toList();
        String fieldName = field == ContactFieldPredicate.Field.PHONE ? "phone" : "email";
        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_FIELD_NOT_FOUND, fieldName, value));
        }
        if (matches.size() != 1) {
            throw new CommandException(String.format(MESSAGE_FIELD_AMBIGUOUS, fieldName, value));
        }
        return matches.get(0);
    }

    private static PendingRequest createConfirmation(List<Person> targets, Model model) {
        String preview = "Delete " + targets.size() + " contact(s):\n"
                + targets.stream().map(person -> Messages.format(person, model.getFieldOrder()))
                        .collect(Collectors.joining("\n"));
        return new ConfirmationRequest(preview, new ConfirmedDeleteCommand(targets));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DeleteCommand otherCommand)) {
            return false;
        }
        return Objects.equals(targetIndex, otherCommand.targetIndex)
                && Objects.equals(targetName, otherCommand.targetName)
                && Objects.equals(targetPhone, otherCommand.targetPhone)
                && targetEmails.equals(otherCommand.targetEmails);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetName", targetName)
                .add("targetPhone", targetPhone)
                .add("targetEmails", targetEmails)
                .toString();
    }

    /**
     * Removes a previously resolved set of contacts after confirmation.
     */
    private static final class ConfirmedDeleteCommand extends Command {
        private final List<Person> targets;

        private ConfirmedDeleteCommand(List<Person> targets) {
            this.targets = List.copyOf(targets);
        }

        @Override
        public CommandResult execute(Model model) throws CommandException {
            if (!model.getContactFile().getPersonList().containsAll(targets)) {
                throw new CommandException("Contacts changed. Start the delete request again.");
            }
            int originalCount = model.getContactFile().getPersonList().size();
            targets.forEach(model::deletePerson);
            assert model.getContactFile().getPersonList().size() == originalCount - targets.size()
                    : "Every resolved contact must be deleted exactly once";
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            String deleted = targets.stream()
                    .map(person -> String.format(MESSAGE_DELETE_PERSON_SUCCESS,
                            Messages.format(person, model.getFieldOrder())))
                    .collect(Collectors.joining("\n"));
            return new CommandResult(deleted + "\n" + model.getContactFile().getPersonList().size()
                    + " contact(s) remaining.");
        }
    }
}
