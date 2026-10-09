package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINKEDIN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.Collections;
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
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_COMPANY + "COMPANY] "
            + "[" + PREFIX_ROLE + "ROLE] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_LINKEDIN + "LINKEDIN] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_FIELD_UPDATED = "Updated %1$s's %2$s to %3$s.";
    public static final String MESSAGE_NO_FIELDS_UPDATED = "No details of %1$s were changed.";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGES =
            "The new values are the same as the current ones, so there is nothing to change.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    public static final String MESSAGE_ALREADY_SAVED_FOR_OTHER =
            "%1$s is already saved for %2$s. No changes were applied.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (editedPerson.equals(personToEdit)) {
            throw new CommandException(MESSAGE_NO_CHANGES);
        }

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        checkPhoneAndEmailNotTaken(model, personToEdit, editedPerson);

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(formatUpdatedFields(personToEdit, editedPerson));
    }

    /**
     * Returns one line for each field whose value differs between {@code original} and {@code edited},
     * in the form "Updated NAME's FIELD to VALUE.", where NAME is the name before the edit.
     */
    static String formatUpdatedFields(Person original, Person edited) {
        String name = original.getName().toString();
        List<String> lines = new ArrayList<>();
        addLineIfChanged(lines, name, "name", original.getName(), edited.getName());
        addLineIfChanged(lines, name, "company", original.getCompany(), edited.getCompany());
        addLineIfChanged(lines, name, "role", original.getRole(), edited.getRole());
        addLineIfChanged(lines, name, "phone", original.getPhone(), edited.getPhone());
        addLineIfChanged(lines, name, "email", original.getEmail(), edited.getEmail());
        addLineIfChanged(lines, name, "LinkedIn", original.getLinkedin(), edited.getLinkedin());
        if (!original.getTags().equals(edited.getTags())) {
            lines.add(String.format(MESSAGE_FIELD_UPDATED, name, "tags", formatTags(edited.getTags())));
        }

        if (lines.isEmpty()) {
            return String.format(MESSAGE_NO_FIELDS_UPDATED, name);
        }
        return String.join("\n", lines);
    }

    private static void addLineIfChanged(List<String> lines, String name, String fieldName,
            Object originalValue, Object editedValue) {
        if (!originalValue.equals(editedValue)) {
            lines.add(String.format(MESSAGE_FIELD_UPDATED, name, fieldName, editedValue));
        }
    }

    /**
     * Adds a line for an optional field. The value is unwrapped before it is shown, so that a
     * changed phone number reads "to 91234567" rather than "to Optional[91234567]".
     */
    private static void addLineIfChanged(List<String> lines, String name, String fieldName,
            Optional<?> originalValue, Optional<?> editedValue) {
        if (originalValue.equals(editedValue)) {
            return;
        }
        lines.add(String.format(MESSAGE_FIELD_UPDATED, name, fieldName,
                editedValue.map(Object::toString).orElse("none")));
    }

    /**
     * Returns the tag names in alphabetical order, separated by commas, or "none" if there are no tags.
     */
    private static String formatTags(Set<Tag> tags) {
        if (tags.isEmpty()) {
            return "none";
        }
        return tags.stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Throws a {@code CommandException} if the new phone number or email in this edit already
     * belongs to a person other than {@code personToEdit}. Emails are compared case-insensitively.
     */
    private void checkPhoneAndEmailNotTaken(Model model, Person personToEdit, Person editedPerson)
            throws CommandException {
        boolean isPhoneEdited = editPersonDescriptor.getPhone().isPresent();
        boolean isEmailEdited = editPersonDescriptor.getEmail().isPresent();

        for (Person otherPerson : model.getAddressBook().getPersonList()) {
            if (otherPerson.equals(personToEdit)) {
                continue;
            }
            if (isPhoneEdited && hasSamePhone(otherPerson, editedPerson)) {
                throw new CommandException(String.format(MESSAGE_ALREADY_SAVED_FOR_OTHER,
                        editedPerson.getPhone().orElseThrow(), otherPerson.getName()));
            }
            if (isEmailEdited && hasSameEmail(otherPerson, editedPerson)) {
                throw new CommandException(String.format(MESSAGE_ALREADY_SAVED_FOR_OTHER,
                        editedPerson.getEmail().orElseThrow(), otherPerson.getName()));
            }
        }
    }

    /**
     * Returns true if both persons have a phone number and the two are the same. A person without a
     * phone number never clashes, since there is no number of theirs to reuse.
     */
    private static boolean hasSamePhone(Person first, Person second) {
        return first.getPhone().isPresent() && second.getPhone().isPresent()
                && first.getPhone().get().equals(second.getPhone().get());
    }

    /**
     * Returns true if both persons have an email and the two are the same, ignoring case.
     * A person without an email never clashes, since there is no address of theirs to reuse.
     */
    private static boolean hasSameEmail(Person first, Person second) {
        return first.getEmail().isPresent() && second.getEmail().isPresent()
                && first.getEmail().get().value.equalsIgnoreCase(second.getEmail().get().value);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Company updatedCompany = editPersonDescriptor.getCompany().or(personToEdit::getCompany).orElse(null);
        Role updatedRole = editPersonDescriptor.getRole().or(personToEdit::getRole).orElse(null);
        Phone updatedPhone = editPersonDescriptor.getPhone().or(personToEdit::getPhone).orElse(null);
        Email updatedEmail = editPersonDescriptor.getEmail().or(personToEdit::getEmail).orElse(null);
        Linkedin updatedLinkedin = editPersonDescriptor.getLinkedin().or(personToEdit::getLinkedin).orElse(null);
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        return new Person(updatedName, updatedCompany, updatedRole, updatedPhone, updatedEmail, updatedLinkedin,
                updatedTags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Company company;
        private Role role;
        private Phone phone;
        private Email email;
        private Linkedin linkedin;
        private Set<Tag> tags;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setCompany(toCopy.company);
            setRole(toCopy.role);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setLinkedin(toCopy.linkedin);
            setTags(toCopy.tags);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, company, role, phone, email, linkedin, tags);
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

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(company, otherEditPersonDescriptor.company)
                    && Objects.equals(role, otherEditPersonDescriptor.role)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(linkedin, otherEditPersonDescriptor.linkedin)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags);
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
                    .add("tags", tags)
                    .toString();
        }
    }
}
