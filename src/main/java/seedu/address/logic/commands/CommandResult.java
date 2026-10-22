package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /**
     * Indicates whether help information should be shown.
     */
    private final boolean showHelp;

    /**
     * Indicates whether the application should exit.
     */
    private final boolean exit;

    private final PendingRequest pendingRequest;
    private final Person personForDetails;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null);
    }

    /**
     * Constructs a result that asks Logic to retain a request without saving contact data.
     */
    public CommandResult(String feedbackToUser, PendingRequest pendingRequest) {
        this(feedbackToUser, false, false, requireNonNull(pendingRequest), null);
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, PendingRequest pendingRequest,
            Person personForDetails) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.pendingRequest = pendingRequest;
        this.personForDetails = personForDetails;
    }

    /**
     * Creates a detail result retaining the contact for presentation refresh without another view command.
     */
    public static CommandResult createPersonDetails(String feedbackToUser, Person person) {
        return new CommandResult(feedbackToUser, false, false, null, requireNonNull(person));
    }

    public Optional<Person> getPersonForDetails() {
        return Optional.ofNullable(personForDetails);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public Optional<PendingRequest> getPendingRequest() {
        return Optional.ofNullable(pendingRequest);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(pendingRequest, otherCommandResult.pendingRequest)
                && Objects.equals(personForDetails, otherCommandResult.personForDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, pendingRequest, personForDetails);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .toString();
    }

}
