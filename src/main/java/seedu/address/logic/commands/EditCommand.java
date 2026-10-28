package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINKEDIN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Previews and confirms edits to an existing person in the contact file.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits the details of the person identified by the index number "
            + "used in the displayed person list, or by name. "
            + "Preview changes, then reply y to apply or n/cancel to stop.\n"
            + "Parameters: INDEX or NAME "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_COMPANY + "COMPANY] "
            + "[" + PREFIX_ROLE + "ROLE] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_LINKEDIN + "LINKEDIN] "
            + "[" + PREFIX_EVENT_NAME + "EVENT] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EVENT_NAME + "NUS Career Fair";

    public static final String MESSAGE_FIELD_UPDATED =
            "Updated %1$s's %2$s to %3$s.";
    public static final String MESSAGE_NO_FIELDS_UPDATED =
            "No details of %1$s were changed.";
    public static final String MESSAGE_NOT_EDITED =
            "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGES =
            "The new values are the same as the current ones, so there is nothing to change.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "This person already exists in the contact file.";
    public static final String MESSAGE_DUPLICATE_EVENT =
            "This person is already associated with event %1$s.";
    public static final String MESSAGE_ALREADY_SAVED_FOR_OTHER =
            "%1$s is already saved for %2$s. No changes were applied.";

    private final Index index;
    private final String targetName;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * Creates an edit command using an index.
     *
     * @param index Index of the person to edit.
     * @param editPersonDescriptor Details to edit the person with.
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.targetName = null;
        this.editPersonDescriptor =
                new EditPersonDescriptor(editPersonDescriptor);
    }

    /**
     * Creates an edit command using a name.
     *
     * @param targetName Name of the person to edit.
     * @param editPersonDescriptor Details to edit the person with.
     */
    public EditCommand(
            String targetName,
            EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(targetName);
        requireNonNull(editPersonDescriptor);

        this.index = null;
        this.targetName = targetName;
        this.editPersonDescriptor =
                new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit;
        switch (PersonNameResolver.resolveTarget(
                model, index, targetName, COMMAND_WORD)) {
            case PersonNameResolver.Outcome.Unique unique -> {
                personToEdit = unique.person();
            }
            case PersonNameResolver.Outcome.Several several -> {
                return new CommandResult(several.message());
            }
        }

        if (editPersonDescriptor.getEvent().isPresent()
                && personToEdit.hasEvent(
                        editPersonDescriptor.getEvent().orElseThrow())) {
            throw new CommandException(
                    String.format(
                            MESSAGE_DUPLICATE_EVENT,
                            editPersonDescriptor.getEvent().orElseThrow()));
        }

        Person editedPerson = createEditedPerson(
                personToEdit,
                editPersonDescriptor);

        if (editedPerson.getIntroducer()
                .filter(name -> name.fullName.equalsIgnoreCase(editedPerson.getName().fullName)).isPresent()) {
            throw new CommandException(IntroduceCommand.MESSAGE_SELF_INTRODUCTION);
        }

        if (editedPerson.equals(personToEdit)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }

        if (!personToEdit.isSamePerson(editedPerson)
                && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        checkPhoneAndEmailNotTaken(model, personToEdit, editedPerson);

        PendingRequest request = new ConfirmationRequest("Preview contact edit:\n"
                + formatUpdatedFields(personToEdit, editedPerson) + "\nResulting contact:\n"
                + Messages.format(editedPerson, model.getFieldOrder()), new Command() {
                    @Override
                    public CommandResult execute(Model confirmedModel) throws CommandException {
                        if (!confirmedModel.getContactFile().getPersonList().contains(personToEdit)) {
                            throw new CommandException("Contact changed. Start the edit request again.");
                        }
                        if (!personToEdit.isSamePerson(editedPerson) && confirmedModel.hasPerson(editedPerson)) {
                            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
                        }
                        checkPhoneAndEmailNotTaken(confirmedModel, personToEdit, editedPerson);
                        confirmedModel.setPerson(personToEdit, editedPerson);
                        confirmedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
                        assert editedPerson.getLastFollowUpId() == personToEdit.getLastFollowUpId()
                                : "Contact edits must retain allocated follow-up IDs";
                        return new CommandResult(formatUpdatedFields(personToEdit, editedPerson));
                    }
                });
        return new CommandResult(request.getPrompt(), request);
    }

    /**
     * Formats the fields changed by an edit.
     */
    static String formatUpdatedFields(Person original, Person edited) {
        String name = original.getName().toString();
        List<String> lines = new ArrayList<>();

        addLineIfChanged(
                lines, name, "name", original.getName(), edited.getName());
        addLineIfChanged(
                lines, name, "company", original.getCompany(), edited.getCompany());
        addLineIfChanged(
                lines, name, "role", original.getRole(), edited.getRole());
        addLineIfChanged(
                lines, name, "phone", original.getPhone(), edited.getPhone());
        addLineIfChanged(
                lines, name, "email", original.getEmail(), edited.getEmail());
        addLineIfChanged(
                lines,
                name,
                "LinkedIn",
                original.getLinkedin(),
                edited.getLinkedin());

        if (!original.getTags().equals(edited.getTags())) {
            lines.add(String.format(
                    MESSAGE_FIELD_UPDATED,
                    name,
                    "tags",
                    formatTags(edited.getTags())));
        }

        if (!original.getEvents().equals(edited.getEvents())) {
            lines.add(String.format(
                    MESSAGE_FIELD_UPDATED,
                    name,
                    "events",
                    formatEvents(edited.getEvents())));
        }

        if (lines.isEmpty()) {
            return String.format(MESSAGE_NO_FIELDS_UPDATED, name);
        }

        return String.join("\n", lines);
    }

    private static void addLineIfChanged(
            List<String> lines,
            String name,
            String fieldName,
            Object originalValue,
            Object editedValue) {
        if (!originalValue.equals(editedValue)) {
            lines.add(String.format(
                    MESSAGE_FIELD_UPDATED,
                    name,
                    fieldName,
                    editedValue));
        }
    }

    private static void addLineIfChanged(
            List<String> lines,
            String name,
            String fieldName,
            Optional<?> originalValue,
            Optional<?> editedValue) {
        if (originalValue.equals(editedValue)) {
            return;
        }

        lines.add(String.format(
                MESSAGE_FIELD_UPDATED,
                name,
                fieldName,
                editedValue.map(Object::toString).orElse("none")));
    }

    private static String formatTags(Set<Tag> tags) {
        if (tags.isEmpty()) {
            return "none";
        }

        return tags.stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private static String formatEvents(Set<Event> events) {
        if (events.isEmpty()) {
            return "none";
        }

        return events.stream()
                .map(event -> event.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    private void checkPhoneAndEmailNotTaken(
            Model model,
            Person personToEdit,
            Person editedPerson) throws CommandException {
        boolean isPhoneEdited =
                editPersonDescriptor.getPhone().isPresent();
        boolean isEmailEdited =
                editPersonDescriptor.getEmail().isPresent();

        for (Person otherPerson
                : model.getContactFile().getPersonList()) {
            if (otherPerson.equals(personToEdit)) {
                continue;
            }

            if (isPhoneEdited
                    && hasSamePhone(otherPerson, editedPerson)) {
                throw new CommandException(
                        String.format(
                                MESSAGE_ALREADY_SAVED_FOR_OTHER,
                                editedPerson.getPhone().orElseThrow(),
                                otherPerson.getName()));
            }

            if (isEmailEdited
                    && hasSameEmail(otherPerson, editedPerson)) {
                throw new CommandException(
                        String.format(
                                MESSAGE_ALREADY_SAVED_FOR_OTHER,
                                editedPerson.getEmail().orElseThrow(),
                                otherPerson.getName()));
            }
        }
    }

    private static boolean hasSamePhone(
            Person first,
            Person second) {
        return first.getPhone().isPresent()
                && second.getPhone().isPresent()
                && first.getPhone().get().equals(second.getPhone().get());
    }

    private static boolean hasSameEmail(
            Person first,
            Person second) {
        return first.getEmail().isPresent()
                && second.getEmail().isPresent()
                && first.getEmail().get().value
                        .equalsIgnoreCase(second.getEmail().get().value);
    }

    private static Person createEditedPerson(
            Person personToEdit,
            EditPersonDescriptor editPersonDescriptor) {
        Name updatedName =
                editPersonDescriptor.getName().orElse(personToEdit.getName());

        Company updatedCompany = updatedValue(
                editPersonDescriptor,
                RemovableField.COMPANY,
                editPersonDescriptor.getCompany(),
                personToEdit.getCompany());

        Role updatedRole = updatedValue(
                editPersonDescriptor,
                RemovableField.ROLE,
                editPersonDescriptor.getRole(),
                personToEdit.getRole());

        Phone updatedPhone = updatedValue(
                editPersonDescriptor,
                RemovableField.PHONE,
                editPersonDescriptor.getPhone(),
                personToEdit.getPhone());

        Email updatedEmail = updatedValue(
                editPersonDescriptor,
                RemovableField.EMAIL,
                editPersonDescriptor.getEmail(),
                personToEdit.getEmail());

        Linkedin updatedLinkedin = updatedValue(
                editPersonDescriptor,
                RemovableField.LINKEDIN,
                editPersonDescriptor.getLinkedin(),
                personToEdit.getLinkedin());

        Set<Tag> updatedTags =
                editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        Set<Event> updatedEvents =
                new HashSet<>(personToEdit.getEvents());

        editPersonDescriptor.getEvent()
                .ifPresent(updatedEvents::add);

        return new Person(
                updatedName,
                updatedCompany,
                updatedRole,
                updatedPhone,
                updatedEmail,
                updatedLinkedin,
                personToEdit.getIntroducer().orElse(null),
                updatedTags,
                updatedEvents,
                personToEdit.getFollowUps(),
                personToEdit.isStarred(),
                personToEdit.getInteractions(), personToEdit.getLastFollowUpId());
    }

    private static <T> T updatedValue(
            EditPersonDescriptor editPersonDescriptor,
            RemovableField field,
            Optional<T> newValue,
            Optional<T> currentValue) {
        if (editPersonDescriptor.isRemoved(field)) {
            return null;
        }

        return newValue.or(() -> currentValue).orElse(null);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return Objects.equals(index, otherEditCommand.index)
                && Objects.equals(
                        targetName,
                        otherEditCommand.targetName)
                && editPersonDescriptor.equals(
                        otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("targetName", targetName)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Optional person fields that an edit can remove.
     */
    public enum RemovableField {
        COMPANY, ROLE, PHONE, EMAIL, LINKEDIN
    }

    /**
     * Stores the details to edit a person with.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Company company;
        private Role role;
        private Phone phone;
        private Email email;
        private Linkedin linkedin;
        private Event event;
        private Set<Tag> tags;
        private final Set<RemovableField> fieldsToRemove =
                EnumSet.noneOf(RemovableField.class);

        /**
         * Creates an empty edit descriptor.
         */
        public EditPersonDescriptor() {}

        /**
         * Creates a copy of an existing edit descriptor.
         *
         * @param toCopy descriptor to copy
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setCompany(toCopy.company);
            setRole(toCopy.role);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setLinkedin(toCopy.linkedin);
            setEvent(toCopy.event);
            setTags(toCopy.tags);
            fieldsToRemove.addAll(toCopy.fieldsToRemove);
        }

        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(
                    name,
                    company,
                    role,
                    phone,
                    email,
                    linkedin,
                    event,
                    tags)
                    || !fieldsToRemove.isEmpty();
        }

        public void removeField(RemovableField field) {
            fieldsToRemove.add(field);
        }

        public boolean isRemoved(RemovableField field) {
            return fieldsToRemove.contains(field);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setCompany(Company company) {
            this.company = company;
        }

        public Optional<Company> getCompany() {
            return Optional.ofNullable(company);
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public Optional<Role> getRole() {
            return Optional.ofNullable(role);
        }

        public void setLinkedin(Linkedin linkedin) {
            this.linkedin = linkedin;
        }

        public Optional<Linkedin> getLinkedin() {
            return Optional.ofNullable(linkedin);
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public Optional<Event> getEvent() {
            return Optional.ofNullable(event);
        }

        public void setTags(Set<Tag> tags) {
            this.tags = tags != null
                    ? new HashSet<>(tags)
                    : null;
        }

        public Optional<Set<Tag>> getTags() {
            return tags != null
                    ? Optional.of(Collections.unmodifiableSet(tags))
                    : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (!(other instanceof EditPersonDescriptor otherDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherDescriptor.name)
                    && Objects.equals(company, otherDescriptor.company)
                    && Objects.equals(role, otherDescriptor.role)
                    && Objects.equals(phone, otherDescriptor.phone)
                    && Objects.equals(email, otherDescriptor.email)
                    && Objects.equals(linkedin, otherDescriptor.linkedin)
                    && Objects.equals(event, otherDescriptor.event)
                    && Objects.equals(tags, otherDescriptor.tags)
                    && fieldsToRemove.equals(otherDescriptor.fieldsToRemove);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("company", company)
                    .add("role", role)
                    .add("phone", phone)
                    .add("email", email)
                    .add("linkedin", linkedin)
                    .add("event", event)
                    .add("tags", tags)
                    .add("fieldsToRemove", fieldsToRemove)
                    .toString();
        }
    }
}
