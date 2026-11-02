package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.FollowUp;
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

    /** Whether the contact file should be saved after this command. */
    private final boolean saveContactFile;
    private final PendingRequest pendingRequest;
    private final Person personForDetails;
    private final FollowUp followUpForDetails;
    private final FollowUpListing followUpListing;
    private final boolean preserveCurrentView;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, true);
    }

    /**
     * Constructs a {@code CommandResult} with the specified fields and save behavior.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveContactFile) {
        this(feedbackToUser, showHelp, exit, saveContactFile, null, null, null);
    }

    /**
     * Constructs a result that asks Logic to retain a request without saving contact data.
     */
    public CommandResult(String feedbackToUser, PendingRequest pendingRequest) {
        this(feedbackToUser, false, false, false, requireNonNull(pendingRequest), null, null);
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
        this(feedbackToUser, showHelp, exit, true, pendingRequest, personForDetails, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveContactFile,
            PendingRequest pendingRequest, Person personForDetails, FollowUp followUpForDetails) {
        this(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest,
                personForDetails, followUpForDetails, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveContactFile,
            PendingRequest pendingRequest, Person personForDetails, FollowUp followUpForDetails,
            FollowUpListing followUpListing) {
        this(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest, personForDetails, followUpForDetails,
                followUpListing, false);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, boolean saveContactFile,
            PendingRequest pendingRequest, Person personForDetails, FollowUp followUpForDetails,
            FollowUpListing followUpListing, boolean preserveCurrentView) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.saveContactFile = saveContactFile;
        this.pendingRequest = pendingRequest;
        this.personForDetails = personForDetails;
        this.followUpForDetails = followUpForDetails;
        this.followUpListing = followUpListing;
        this.preserveCurrentView = preserveCurrentView;
    }

    /**
     * Creates a detail result retaining the contact for presentation refresh without another view command.
     */
    public static CommandResult createPersonDetails(String feedbackToUser, Person person) {
        return new CommandResult(feedbackToUser, false, false, null, requireNonNull(person));
    }

    /**
     * Returns a copy of this result whose feedback starts with {@code prefix}, keeping every other field.
     */
    public CommandResult withFeedbackPrefix(String prefix) {
        requireNonNull(prefix);
        return new CommandResult(prefix + feedbackToUser, showHelp, exit, saveContactFile, pendingRequest,
                personForDetails, followUpForDetails, followUpListing, preserveCurrentView);
    }

    /**
     * Adds selected-action context to a contact view without changing its feedback or presentation request.
     */
    public CommandResult withSelectedFollowUp(FollowUp action) {
        requireNonNull(personForDetails);
        return new CommandResult(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest,
                personForDetails, requireNonNull(action), followUpListing, preserveCurrentView);
    }

    /**
     * Attaches structured results without changing command feedback or pending selection semantics.
     */
    public CommandResult withFollowUpListing(FollowUpListing listing) {
        return new CommandResult(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest,
                personForDetails, followUpForDetails, requireNonNull(listing), preserveCurrentView);
    }

    /**
     * Marks a result whose display should not replace the currently viewed contact or action.
     */
    public CommandResult withPreserveCurrentView() {
        return new CommandResult(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest,
                personForDetails, followUpForDetails, followUpListing, true);
    }

    public boolean shouldPreserveCurrentView() {
        return preserveCurrentView;
    }

    public Optional<FollowUpListing> getFollowUpListing() {
        return Optional.ofNullable(followUpListing);
    }

    public Optional<FollowUp> getFollowUpForDetails() {
        return Optional.ofNullable(followUpForDetails);
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

    public boolean shouldSaveContactFile() {
        return saveContactFile;
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
                && saveContactFile == otherCommandResult.saveContactFile
                && Objects.equals(pendingRequest, otherCommandResult.pendingRequest)
                && Objects.equals(personForDetails, otherCommandResult.personForDetails)
                && Objects.equals(followUpForDetails, otherCommandResult.followUpForDetails)
                && Objects.equals(followUpListing, otherCommandResult.followUpListing)
                && preserveCurrentView == otherCommandResult.preserveCurrentView;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, saveContactFile, pendingRequest, personForDetails,
                followUpForDetails, followUpListing, preserveCurrentView);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("saveContactFile", saveContactFile)
                .toString();
    }

}
