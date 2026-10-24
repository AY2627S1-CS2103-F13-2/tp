package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.Model;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Removes explicitly selected detail values from a contact.
 */
public class DropCommand extends Command {

    public static final String COMMAND_WORD = "drop";
    public static final String CONFIRM_OPTION = "--confirm";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes selected detail values from a contact after confirmation. Add " + CONFIRM_OPTION
            + " to skip the preview.\n"
            + "Parameters: INDEX or NAME [FIELD/VALUE]... [" + CONFIRM_OPTION + "]\n"
            + "Fields: " + CliSyntax.PREFIX_COMPANY + " " + CliSyntax.PREFIX_ROLE + " "
            + CliSyntax.PREFIX_PHONE + " " + CliSyntax.PREFIX_EMAIL + " "
            + CliSyntax.PREFIX_LINKEDIN + " " + CliSyntax.PREFIX_TAG + " "
            + CliSyntax.PREFIX_INTRODUCER + "\n"
            + "Example: " + COMMAND_WORD + " 1 " + CliSyntax.PREFIX_TAG + "oldtag\n"
            + "Example: " + COMMAND_WORD + " Jane Doe " + CliSyntax.PREFIX_EMAIL
            + "jane@example.com";

    public static final String MESSAGE_EMPTY_CONTACT_FILE = "There are no contacts to update.";
    public static final String MESSAGE_NO_VALUES = "At least one detail value to remove must be provided.";
    public static final String MESSAGE_VALUE_NOT_FOUND =
            "The selected %1$s value \"%2$s\" is not saved for %3$s.";
    public static final String MESSAGE_CONFIRM_REMOVAL = "Remove the following detail(s) from %1$s?\n%2$s";
    public static final String MESSAGE_REMOVED = "Removed the selected detail(s) from contact:\n%1$s";

    /**
     * A contact detail that can be explicitly selected for removal.
     */
    public enum Field {
        COMPANY("company", CliSyntax.PREFIX_COMPANY),
        ROLE("role", CliSyntax.PREFIX_ROLE),
        PHONE("phone", CliSyntax.PREFIX_PHONE),
        EMAIL("email", CliSyntax.PREFIX_EMAIL),
        LINKEDIN("LinkedIn", CliSyntax.PREFIX_LINKEDIN),
        TAG("tag", CliSyntax.PREFIX_TAG),
        INTRODUCER("introducer", CliSyntax.PREFIX_INTRODUCER);

        private final String displayName;
        private final Prefix prefix;

        Field(String displayName, Prefix prefix) {
            this.displayName = displayName;
            this.prefix = prefix;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Prefix getPrefix() {
            return prefix;
        }
    }

    /**
     * A value is stored as its validated model type so execution can compare it to the selected contact detail.
     */
    public record FieldValue(Field field, Object value) {
        /**
         * Creates a selected field value.
         */
        public FieldValue {
            requireNonNull(field);
            requireNonNull(value);
        }

        public String displayValue() {
            return value instanceof Tag tag ? tag.tagName : value.toString();
        }
    }

    private final Index targetIndex;
    private final String targetName;
    private final List<FieldValue> values;
    private final boolean confirmed;

    /**
     * Creates a command targeting a displayed contact index.
     */
    public DropCommand(Index targetIndex, List<FieldValue> values, boolean confirmed) {
        requireNonNull(targetIndex);
        requireNonNull(values);
        this.targetIndex = targetIndex;
        this.targetName = null;
        this.values = List.copyOf(values);
        this.confirmed = confirmed;
    }

    /**
     * Creates a command targeting a contact by name.
     */
    public DropCommand(String targetName, List<FieldValue> values, boolean confirmed) {
        requireNonNull(targetName);
        requireNonNull(values);
        this.targetIndex = null;
        this.targetName = targetName;
        this.values = List.copyOf(values);
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (values.isEmpty()) {
            throw new CommandException(MESSAGE_NO_VALUES);
        }
        if (model.getContactFile().getPersonList().isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_CONTACT_FILE);
        }

        Person person;
        if (targetName == null) {
            List<Person> lastShownList = model.getFilteredPersonList();
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            person = lastShownList.get(targetIndex.getZeroBased());
        } else {
            switch (PersonNameResolver.resolve(model, targetName, COMMAND_WORD)) {
                case PersonNameResolver.Outcome.Unique unique -> person = unique.person();
                case PersonNameResolver.Outcome.Several several -> {
                    List<Person> matches = List.copyOf(model.getFilteredPersonList());
                    PendingRequest request = new DropSelectionRequest(matches);
                    return new CommandResult(request.getPrompt(), request);
                }
            }
        }

        return removeSelectedValues(model, person);
    }

    private CommandResult removeSelectedValues(Model model, Person person) throws CommandException {
        for (FieldValue selected : values) {
            if (!containsValue(person, selected)) {
                throw new CommandException(String.format(MESSAGE_VALUE_NOT_FOUND,
                        selected.field().getDisplayName(), selected.displayValue(), person.getName()));
            }
        }

        if (!confirmed) {
            String preview = String.format(MESSAGE_CONFIRM_REMOVAL, person.getName(), formatValues(values));
            PendingRequest request = new ConfirmationRequest(preview,
                    new ConfirmedDropCommand(person, targetName != null));
            return new CommandResult(request.getPrompt(), request);
        }

        return applyRemoval(model, person, targetName != null);
    }

    private CommandResult applyRemoval(Model model, Person person, boolean restoreFilteredList) {
        Person updatedPerson = removeValues(person, values);
        model.setPerson(person, updatedPerson);
        if (restoreFilteredList) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        }
        return new CommandResult(String.format(MESSAGE_REMOVED, Messages.format(updatedPerson)));
    }

    private final class ConfirmedDropCommand extends Command {
        private final Person target;
        private final boolean restoreFilteredList;

        private ConfirmedDropCommand(Person target, boolean restoreFilteredList) {
            this.target = target;
            this.restoreFilteredList = restoreFilteredList;
        }

        @Override
        public CommandResult execute(Model model) throws CommandException {
            if (!model.getContactFile().getPersonList().contains(target)) {
                throw new CommandException("Contacts changed. Start the drop request again.");
            }
            for (FieldValue selected : values) {
                if (!containsValue(target, selected)) {
                    throw new CommandException(String.format(MESSAGE_VALUE_NOT_FOUND,
                            selected.field().getDisplayName(), selected.displayValue(), target.getName()));
                }
            }
            return applyRemoval(model, target, restoreFilteredList);
        }
    }

    private final class DropSelectionRequest implements PendingRequest {
        private final List<Person> candidates;

        private DropSelectionRequest(List<Person> candidates) {
            this.candidates = List.copyOf(candidates);
        }

        @Override
        public String getPrompt() {
            return candidates.size() + " contacts found:\n" + PersonNameResolver.formatNumberedList(candidates)
                    + "\nEnter a contact number, or n/cancel to stop.";
        }

        @Override
        public CommandResult respond(String response, Model model) throws CommandException {
            if (StringUtil.isNonZeroUnsignedInteger(response)) {
                int index = Integer.parseInt(response) - 1;
                if (index < candidates.size()) {
                    Person selected = candidates.get(index);
                    if (!model.getContactFile().getPersonList().contains(selected)) {
                        throw new CommandException("Contacts changed. Start the drop request again.");
                    }
                    return removeSelectedValues(model, selected);
                }
            }
            return new CommandResult("Enter a number from the displayed candidates.\n" + getPrompt(), this);
        }
    }

    private boolean containsValue(Person person, FieldValue selected) {
        return switch (selected.field()) {
            case COMPANY -> selected.value() instanceof Company value && person.getCompany().filter(value::equals)
                    .isPresent();
            case ROLE -> selected.value() instanceof Role value
                    && person.getRole().filter(value::equals).isPresent();
            case PHONE -> selected.value() instanceof Phone value
                    && person.getPhone().filter(value::equals).isPresent();
            case EMAIL -> selected.value() instanceof Email value
                    && person.getEmail().filter(value::equals).isPresent();
            case LINKEDIN -> selected.value() instanceof Linkedin value
                    && person.getLinkedin().filter(value::equals).isPresent();
            case TAG -> selected.value() instanceof Tag value && person.getTags().contains(value);
            case INTRODUCER -> selected.value() instanceof Name value
                    && person.getIntroducer().filter(value::equals).isPresent();
        };
    }

    private static Person removeValues(Person person, List<FieldValue> selections) {
        Company company = person.getCompany().filter(value -> !contains(selections, Field.COMPANY, value))
                .orElse(null);
        Role role = person.getRole().filter(value -> !contains(selections, Field.ROLE, value)).orElse(null);
        Phone phone = person.getPhone().filter(value -> !contains(selections, Field.PHONE, value)).orElse(null);
        Email email = person.getEmail().filter(value -> !contains(selections, Field.EMAIL, value)).orElse(null);
        Linkedin linkedin = person.getLinkedin().filter(value -> !contains(selections, Field.LINKEDIN, value))
                .orElse(null);
        Set<Tag> tags = person.getTags().stream()
                .filter(tag -> !contains(selections, Field.TAG, tag))
                .collect(Collectors.toSet());
        Name introducer = person.getIntroducer()
                .filter(value -> !contains(selections, Field.INTRODUCER, value))
                .orElse(null);
        return new Person(person.getName(), company, role, phone, email, linkedin,
                introducer, tags);
    }

    private static boolean contains(List<FieldValue> selections, Field field, Object value) {
        return selections.stream().anyMatch(selection -> selection.field() == field
                && selection.value().equals(value));
    }

    private static String formatValues(List<FieldValue> values) {
        return values.stream()
                .map(value -> "- " + value.field().getDisplayName() + ": " + value.displayValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DropCommand otherDropCommand)) {
            return false;
        }
        return Objects.equals(targetIndex, otherDropCommand.targetIndex)
                && Objects.equals(targetName, otherDropCommand.targetName)
                && values.equals(otherDropCommand.values)
                && confirmed == otherDropCommand.confirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetName", targetName)
                .add("values", values)
                .add("confirmed", confirmed)
                .toString();
    }
}
