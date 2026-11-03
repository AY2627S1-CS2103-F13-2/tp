---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Astra is built on [AddressBook-Level3](https://se-education.org/addressbook-level3/) (AB3) by the
  [SE-EDU initiative](https://se-education.org). The overall architecture, much of the `commons`, UI, storage
  and parsing code, the structure of this guide and the User Guide, and several diagrams are adapted from AB3.
* Libraries: [JavaFX](https://openjfx.io/) for the GUI, [Jackson](https://github.com/FasterXML/jackson) for
  JSON storage, [JUnit 5](https://junit.org/junit5/) for testing, and
  [OpenJFX Monocle](https://github.com/TestFX/Monocle) for running UI tests without a display.
* Fuzzy name search uses the standard
  [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance) algorithm.
* Notable uses of AI:
  * @Cookiemunchrr used Claude Code (Anthropic's Claude models) extensively as a pair programmer: to implement
    and test the contact model rework, deletion and confirmation flows, transactional saves, tag filtering,
    follow-ups and field order; to draft their User Guide and Developer Guide sections and UML diagrams; and to
    draft commit messages and pull request descriptions. They reviewed, edited and tested all generated output.
  * @Yx0905 used Claude Code to implement, test and document parts of their work, such as the interaction
    history, command shortcuts and several bug fixes.
  * @KeeYenCheng used GitHub Copilot to help implement and test parts of their work, such as the introducer,
    undo history, field order coverage and window position recovery, and to update Developer Guide diagrams.
  * @Kafka03 used OpenAI Codex for some v1.6 fixes, such as numeric contact selection.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other. It loads the saved contacts through `Storage` before creating `Model`, so that any recovery done at startup can be reported by `UI`.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

In the diagram, a solid arrow means the component holds a reference to the other one, and a dashed arrow means it only
depends on it. `UI` keeps a reference to `Logic` and shows data from `Model`, but never changes `Model` itself: every
user command goes through `Logic`, which updates `Model` and asks `Storage` to save the changes. `UI` depends on
`Storage` only for the startup result it displays (whether the contacts were loaded or recovered), and
`Storage` depends on `Model` for the data it reads and writes.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact when the user enters `delete 1` and then
confirms with `y`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

The first command only looks up the contact on display and returns a preview with a y/n prompt, so nothing is
changed or saved yet. The reply `y` makes `Logic` delete the contact from `Model` and save the contact file
through `Storage`. If saving fails, `Logic` restores the data that was in `Model` before the command.

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonDetailsPanel`, `FollowUpListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `PersonListPanel` observes the filtered contact list exposed by the `Model`. When a find command changes the filtered list, the panel refreshes the visible contact cards.

Each contact is rendered by `PersonCard`. The card displays the contact's name, tags, company, role, phone, email, LinkedIn profile, introducer, events, and follow-up information. Optional fields are hidden when they are absent.

The contact card layout is defined in `PersonListCard.fxml`. Common visual styling is defined in `DarkTheme.css`, and `AstraTheme.css` is applied on top of it.

`MainWindow` composes the main UI parts, including `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonDetailsPanel`, `FollowUpListPanel`, `StatusBarFooter`, `HelpWindow`, and `ConfirmationPanel`.

`PersonDetailsPanel` is the detail card beside the contact list. It shows every stored detail of the contact that a command such as `view` returned, in the saved field order. `FollowUpListPanel` takes the place of the contact list while a `followups` result is shown. It lists the follow-ups in the same numbered order as the command's feedback; selecting a row, by clicking or with the arrow keys, shows that contact and follow-up in `PersonDetailsPanel`.

`ConfirmationPanel` displays the active confirmation request and its preview. Users can confirm or cancel the request using the panel buttons, or enter `y`, `n`, or `cancel` in the command box. Button responses are passed back through `MainWindow` and use the same command execution path as typed responses.

The panel is shown only while a confirmation request is active. It is hidden after the request is completed, cancelled, or replaced. A revision number is used to ignore delayed button events from an older confirmation request.

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

`delete 1` does not delete the contact straight away: `Logic` first shows a preview and waits for the user to reply
`y`, and only then deletes the contact and saves. The sequence diagrams in
[Pending requests and transactional commands](#pending-requests-and-transactional-commands) show the interactions
inside `Logic` for `delete 1`.

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to a `ContactFileParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in those diagrams for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.
1. A result carrying a `PendingRequest` is retained by Logic until the user
   answers or cancels. Replies go to that request before normal command parsing.
   When a command changes data, Logic saves it, and restores the previous data if the save fails.
   A command that changes nothing is not saved.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `ContactFileParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `ContactFileParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<!-- @@author Cookiemunchrr -->

<img src="images/ModelClassDiagram.png" width="750" />

The diagram shows only the methods discussed in this guide. `ModelManager` implements every method of `Model`, so its methods are not repeated.

The `Model` component,

* stores the contact file data i.e., all `Person` objects, in a `UniquePersonList` inside a `ContactFile`. `ModelManager` builds its own copies of the `ContactFile` and `UserPrefs` it is given, so it owns them (composition).
* a `Person` always has a `Name`, and may additionally have a `Company`, `Role`, `Phone`, `Email`, `Linkedin` and an introducer (the `Name` of the person who introduced them). Every field other than the name is optional, because a contact met at a networking event is often captured with only the details that were exchanged. Optional fields are exposed as `Optional<...>` so callers must handle an absent value. These single-value classes, and `Tag`, are shown as attributes of `Person` rather than as separate classes.
* a `Person` also holds any number of tags, `Event`s, `FollowUp`s and `Interaction`s, a starred flag, and the last follow-up ID it allocated.
* `Person` is immutable. `withStarred`, `withEvent`, `appendFollowUp`, `removeFollowUp` and `withInteraction` each return a new `Person`, which `Model#setPerson` puts in place of the old one. The new `Person` shares its unchanged `Event`, `FollowUp` and `Interaction` objects with the old one, so these are drawn as associations rather than compositions.
* `Person#isSamePerson` treats two contacts as the same person when their names match ignoring case, and their
  emails are either both absent or equal ignoring case. Separately, `add` and `edit` reject a phone number (ignoring
  spaces, hyphens and parentheses) or an email (ignoring case) that another contact already uses. Follow-ups, the
  follow-up ID counter, events, interactions and the starred flag do not affect whether two contacts are the same.
  <!-- @@author -->
* a `FollowUp` has an ID, a description, an optional due date and a `Status` (`PENDING` or `COMPLETED`). `FollowUp#isOverdue` is true for a pending follow-up whose due date has passed. `Model#getFollowUps` creates a `FollowUpEntry` for each follow-up, pairing it with its contact; `Model#getPendingFollowUps` retains only pending actions from that snapshot.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list (`filteredPersons`). It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (GUI settings, command aliases and the global contact field order). This is exposed to the outside as a `ReadOnlyUserPrefs` object. The field order is a `FieldOrder`, which lists each of the ten `Field` values exactly once.
* provides `ModelSnapshot`, which copies the contact file and preferences and keeps the current filter. `Logic` takes one before a command so that it can `restore` the model if the command fails or is cancelled (see [Pending requests and transactional commands](#pending-requests-and-transactional-commands)).
* how a follow-up is added through an immutable `Person`, and why follow-up IDs are never reused, is shown in [Follow-up actions (UC16)](#follow-up-actions-uc16).
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F13-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both contact file data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonContactFileStorage` and `JsonUserPrefsStorage` (one class per data file).
  Both use `AtomicFileWriter` for candidate writing, verification, atomic replacement and cleanup.
  Callers retain data-specific validation. `ContactStartupLoader` also retains recovery archiving and
  the original-file change check.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAdaptedPerson` uses one optional-scalar helper for absent-value handling, validation and construction.
Required-name validation and collection-specific rules remain in separate conversion helpers.
`ContactTextField` holds the shared text-field constraints used by conversion and startup recovery.
Recovery still checks raw JSON types and collects field-specific errors before conversion; it owns
the decision to skip a record and retains the original record positions in its report.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Shared contact matching

`NameContainsKeywordsPredicate` supplies the existing contiguous, partial,
case-insensitive name matching used by find, edit, delete, and view.
`PersonNameResolver#resolve` keeps edit and view's numbered-retry behavior.
`PersonNameResolver#findMatches` performs the same full-file lookup without
changing the displayed list; delete uses it before creating a pending request.
Ambiguous delete names display candidates and require a numeric reply followed
by a separate y/n confirmation. A name never implicitly selects the first match.

### Finding contacts by different criteria

`FindCommandParser` determines the search mode from the user's input and
creates a `FindCommand` containing the corresponding predicate.

The following predicates support the implemented search modes:

* `NameContainsKeywordsPredicate` performs case-insensitive contiguous and
  partial name matching.
* `FuzzyNamePredicate` normalises the query and compares it, using Levenshtein
  distance, with the contact's full name and with each word of it. A contact
  matches when its full name contains the query, or when the full name or a
  word is within min(2, query length − 1) edits of the query. The second limit
  stops a one- or two-letter query from matching unrelated short words: any
  two-letter word is within two edits of `al`, but `yu` shares no letter with it.
* `ContactFieldPredicate` searches fields such as phone number, email, company,
  and role.
* `EventContainsKeywordsPredicate` searches the names of events associated with
  each contact.
* `NoteContainsKeywordsPredicate` performs case-insensitive partial matching
  against the notes in a contact's interaction history. `FindNotesCommand`
  executes the `/notes` search and displays contacts with matching notes.

When both `/company` and `/role` are provided, the parser combines the two
predicates. A contact must satisfy both criteria to be included in the results.

The resulting predicate is passed to the model to update its filtered contact
list. The UI observes this list and refreshes the displayed `PersonCard`
objects automatically. Searching does not modify stored contact data.

Invalid search values are rejected by the parser. If no contacts match the
search, Astra reports the result without modifying the stored contact file.

The following class diagram shows the relationship between the find command
and its search predicates:

![Find class diagram](images/FindClassDiagram.png)

The following sequence diagram shows how a find command is parsed, executed,
and reflected in the displayed contact list:

![Find sequence diagram](images/FindSequenceDiagram.png)

### Viewing contacts by name or displayed index

`ViewCommandParser` normalizes whitespace and uses `ParserUtil#isIndexInput` to classify the selector.
A numeric first token must form one valid positive index; otherwise the whole input must be a valid
name, so prefixes such as `n/` are rejected. `ViewCommand` finds the contact with
`PersonNameResolver#resolveTarget` and formats it with `Messages#format` in the saved field order, omitting
absent optional fields. The result also carries the contact, so the UI can redraw the details at once when
the field order changes. When a name matches several contacts, the command lists them and asks the user to
enter `view INDEX`; there is no y/n prompt or pending request.

An index keeps the current filter, while a name searches all contacts and narrows the displayed list to the
matches. If parsing or the lookup fails, `LogicManager` restores the previous filter from its snapshot.
`view` never changes contact data, so nothing is saved. `list` restores the full list.

The activity diagram below shows how `view` handles its selector.

![Viewing a contact](images/ViewActivityDiagram.png)

### Normalized contact keys

Phone matching uses `Phone#matches`, which removes spaces, hyphens, and
parentheses but preserves a leading plus. Equality and hashing use that same
canonical value. Email matching uses `Email#matches` and ignores letter case.
`ContactFieldPredicate`, duplicate checks, delete, and startup recovery share
these comparisons. Missing phone/email values are not shared keys.

### Pending requests and transactional commands

`ContactSelection` handles numeric selectors for commands that already accept a contact index or name.
It combines the displayed-index contact with stored name matches, removes duplicates, and uses the existing
numbered selection request when several contacts match. Choosing a contact then runs the original command's
validation and confirmation flow. Logic retains the original command type for history and undo bookkeeping.

`CommandResult` can carry a `PendingRequest`. `LogicManager` retains it together
with a `ModelSnapshot` captured before the initial command. `ConfirmationRequest`
runs its resolved command only when the reply is `y`. `PersonSelectionRequest` turns a
numbered candidate selection into the next request; it keeps references to the
candidate contacts rather than resolving a displayed index again later.

Logic handles `n` and `cancel` itself: it restores the snapshot, which also brings
back the pre-request filter, and discards the request. `exit` also discards the
request, and then closes Astra. While a confirmation or a contact selection is
pending, any other reply repeats the prompt and no other command runs. Only the
read-only selection shown by `followups` passes other input on as a normal command.
A confirmed request that completes or fails clears the pending state, so a stale `y`
cannot repeat the change. UC16 reuses `ConfirmationRequest` with an "Add another?"
question for a duplicate pending follow-up.

<!-- @@author Cookiemunchrr -->

`ModelSnapshot` is the Model's restore point for one request. It copies the
contact file and the user preferences, and keeps the active filter. Logic uses
it in three ways:

* **To decide whether to save.** After a command, `hasDataChanged` and
  `hasPreferencesChanged` compare the model with the snapshot, so only changed
  data is written. Queries, ambiguity results, prompts, selections,
  cancellations and no-ops perform no contact-file write.
* **To roll back a failure.** If a command or a save throws, `restore` puts the
  copied data and the original filter back, so the model never shows a change
  that was not saved.
* **To cancel a pending request.** The snapshot is kept with the request until
  the user replies. `n` or `cancel` restores it, which also brings back the
  list the user saw before, such as the full list after a name lookup narrowed
  it to the candidates.

`restore` copies the saved contacts back into the existing lists instead of
replacing them, so the observable lists bound to the UI stay connected.

The sequence diagram below shows `delete 1` followed by a reply. The first
input only takes the snapshot and creates the request; nothing is deleted yet.
The outer `alt` frame shows the two replies:

* `y` runs the resolved command, finds that the data changed, and saves. The
  inner `alt` frame shows the two outcomes of the save: if `saveContactFile`
  throws an `IOException`, Logic restores the snapshot and reports the failure
  as a `CommandException` instead of returning a result.
* `n` or `cancel` restores the snapshot straight away, without saving.

Either way, Logic then discards both the request and the snapshot. The details
of the two `ref` frames are shown in the two diagrams after it.

![Snapshot and rollback during a confirmed delete](images/TransactionSequenceDiagram.png)

![sd create delete confirmation c](images/DeleteConfirmationSequenceDiagram.png)

![sd run confirmed delete](images/ConfirmedDeleteSequenceDiagram.png)

`JsonContactFileStorage` serializes to a sibling temporary file, reloads and
compares the candidate, then uses atomic replacement. Failures before
replacement preserve the previous active file. Temporary-file cleanup never
turns an already successful replacement into a reported transaction failure.
No unsafe in-place fallback is used when atomic replacement is unavailable.

#### Design considerations

**Aspect: How a failed or cancelled request is undone**

* **Alternative 1 (current choice):** Logic takes a `ModelSnapshot` before each
  request and calls `restore` if the request fails or is cancelled.
  * Pros: Rollback is handled in one place, so no command needs its own undo
    logic. The snapshot copies only the list of contacts, not the contacts
    themselves, because `Person` is immutable. The same snapshot also tells
    Logic whether anything needs saving.
  * Cons: Every request copies the contact list and preferences, even one
    that changes nothing. The snapshot covers only the current request, so it
    is not a multi-step undo history.
* **Alternative 2:** Each command reverses its own changes when something
  fails.
  * Pros: Nothing is copied unless a command needs it.
  * Cons: Every command needs its own correct reverse logic, including for
    the filter and preferences. A missed case leaves the model showing a
    change that was never saved.

<!-- @@author -->

### Tag filtering (UC20)

`FilterCommandParser` uses the shared prefix tokenizer with repeated `t/`
values and one optional `mode/`. It validates every tag through
`ParserUtil#parseTags` before the command is created, so a missing or invalid
tag, text before the first prefix, or an unknown or repeated mode is rejected
without changing the displayed list. Because neither tag names nor mode values
contain whitespace, the parser first collapses runs of whitespace. Mode values
ignore letter case; prefixes and tags remain case-sensitive.

`TagFilterPredicate` keeps the requested tags as a set, so a repeated tag
counts once. In AND mode it keeps a contact whose tags contain every requested
tag (`containsAll`); in OR mode, a contact with at least one of them.
`FilterCommand` sets this predicate as the model's filter, so it always
searches all stored contacts rather than only the current results, and the
displayed list keeps the stored order. No contact is copied or sorted.

The result reports the number of matches and the mode, or a no-match message
with an empty list. `view INDEX` then picks from the filtered list, and `list`
shows all contacts again. Filtering changes no contact data, so Logic saves
nothing. If parsing fails, Logic restores the previous filter from its snapshot.

The activity diagram below shows how `filter` handles its input.

![Filtering contacts by tags](images/TagFilterActivityDiagram.png)

<!-- @@author Cookiemunchrr -->

#### Design considerations

**Aspect: How the requested tags are combined**

* **Alternative 1 (current choice):** One mode applies to the whole command:
  AND by default, or OR with `mode/or`.
  * Pros: Quick to type and easy to explain. AND narrows a large list, and OR
    gathers contacts from several categories at once. The parser only has to
    check one optional value.
  * Cons: A mixed query, such as contacts tagged `alumni` and either `finance`
    or `data`, cannot be written as one command.
* **Alternative 2:** A query language with AND, OR, NOT and brackets, such as
  `filter alumni AND (finance OR data)`.
  * Pros: Any combination of tags can be expressed in one command.
  * Cons: Slower to type and easier to get wrong. It needs an expression
    parser with its own error messages, which is more than the few tags a
    contact usually has call for.

**Aspect: Which contacts a filter searches**

* **Alternative 1 (current choice):** Each `filter` replaces the model's
  filter and searches all stored contacts.
  * Pros: The result depends only on the command typed, not on earlier
    commands, so the same command always gives the same list.
  * Cons: The results of an earlier `find` cannot be narrowed further by tag.
* **Alternative 2:** Each `filter` searches only the contacts on display.
  * Pros: Users can narrow a list step by step.
  * Cons: The same command gives different results depending on what was
    shown before, so users have to remember to run `list` first.

<!-- @@author -->

### Starred status and follow-up integration (UC21)

`Person.withStarred(boolean)` copies every contact field and the complete ordered
follow-up history. Appending a follow-up and editing contact details retain the
starred flag. Strong equality and hashing include both metadata fields, so Logic
saves actual changes and rolls both back together on storage failure. JSON stores
both fields; older files default to unstarred contacts with empty action history.

### Follow-up actions (UC16)

`Person` holds an immutable, ordered `List<FollowUp>`. Each follow-up has a
positive ID that is unique within its contact, a non-blank description with
outer spaces removed, an optional due date and a status. A new follow-up's ID
is one more than the contact's `lastFollowUpId`, the largest ID the contact has
ever given out, including follow-ups that were later deleted. Existing IDs and
the order of follow-ups never change. If the counter has reached
`Integer.MAX_VALUE`, the command is rejected without changing anything. Every
copy of a `Person` keeps the counter, and `Person#equals` compares it.
`appendFollowUp` rejects an ID that is not larger than the counter, and
replacing a follow-up keeps its ID.

<!-- @@author Cookiemunchrr -->

The sequence diagram below shows how a follow-up is added inside the Model.
It starts at `FollowUpCommand.AddFollowUpCommand`, a private command class
nested in `FollowUpCommand`, which `FollowUpCommand` creates for the resolved
contact. That command runs straight away, or after a `y` reply when the same
pending follow-up already exists.

1. The new follow-up gets the status `PENDING`, the requested due date, if
   any, and ID `n + 1`, where `n` is the contact's `lastFollowUpId`.
1. `Person` is immutable, so `appendFollowUp` returns a new `Person` with the
   follow-up added and the counter raised to `n + 1`. The old `Person` is not
   changed.
1. `Model#setPerson` passes the new `Person` down to the `UniquePersonList`,
   which puts it in the old one's position. The filtered list and the UI
   update without being rebuilt.

![Adding a follow-up inside the Model](images/FollowUpSequenceDiagram.png)

The object diagram below shows why IDs come from `lastFollowUpId` rather than
the list size. Each frame is a new `Person`, since every change creates one.
After follow-up 2 is deleted, the counter stays at 2, so the next follow-up
gets ID 3. Using the list size would have given it ID 2 again, and a command
or note that referred to the deleted follow-up 2 would then point at the new
one.

![Follow-up IDs are never reused](images/FollowUpIdObjectDiagram.png)

<!-- @@author -->

Status changes are described in
[Completing and reopening follow-ups](#completing-and-reopening-follow-ups-uc17).

`FollowUpCommandParser` takes a positive displayed index followed by exactly one
`a/DESCRIPTION` and at most one `d/DATE`, in either order. Any other
prefix-shaped word, such as `confirm/yes`, or a repeated `a/` or `d/`, is
rejected instead of being stored as part of the description. A due date may be
written as `yyyy-MM-dd`, `dd/MM/yyyy` or `d MMM yyyy`, and `DateParserUtil`
rejects a date before 2000-01-01. Two follow-ups are duplicates when their
trimmed descriptions are equal, with letter case counted, and only a pending
duplicate asks for confirmation. `FollowUpCommand` resolves the contact before
asking, and the confirmed command keeps that contact, so a later change to the
displayed list cannot redirect it. If the contact is no longer stored unchanged
when the user replies `y`, the command fails without changing anything.

`Person#equals` compares the follow-ups and the ID counter, so Logic notices a
follow-up change and saves it. `Person#isSamePerson` and the phone and email
checks ignore follow-ups. `EditCommand` carries the follow-ups and the counter
over to the edited contact. Replacing the `Person` in the model updates the UI
at once: the contact's details list each follow-up with its ID and status, and
a contact without follow-ups shows no follow-up row.

`JsonAdaptedPerson` reads a missing or null `followUps` property, as in older
data files, as an empty list. `JsonAdaptedFollowUp` checks the JSON type of each
field, and that the ID is a positive integer, the description is not blank,
the status is `PENDING` or `COMPLETED` and any due date is a valid ISO date. A
null entry, or two follow-ups with the same ID, makes the whole contact invalid,
and startup recovery then skips and reports that contact like any other invalid
one. `lastFollowUpId` is stored as an integer at least as large as every stored
ID. When it is missing or null, as in older files, the largest stored ID (or
zero) is used instead; a value of the wrong type, out of range, or below a
stored ID makes the contact invalid. IDs deleted before the counter was stored
cannot be recovered from such a file. If saving fails, Logic restores the
previous follow-ups and the displayed list.

The activity diagram below shows how `followup` checks its input, asks about a
duplicate, and saves the new follow-up.

![Recording a follow-up](images/FollowUpActivityDiagram.png)

<!-- @@author Cookiemunchrr -->

#### Design considerations

**Aspect: How a contact is updated**

* **Alternative 1 (current choice):** `Person` is immutable. Every change
  creates a new `Person`, and `Model#setPerson` puts it in place of the old
  one.
  * Pros: Old `Person` objects never change, so `ModelSnapshot` can keep them
    as they are and rollback needs no deep copy. Replacing the whole object
    is one list change, which the UI already observes.
  * Cons: Each change copies every field. The full constructor takes many
    arguments, and every new field has to be carried through it.
* **Alternative 2:** Change the fields of the existing `Person` in place.
  * Pros: Less copying, and simpler code for each update.
  * Cons: A snapshot would need a deep copy of every contact. A command that
    fails halfway could leave a half-changed contact, and the UI would need
    an observable property for every field to notice the change.

**Aspect: How follow-up IDs are allocated**

* **Alternative 1 (current choice):** Each contact stores `lastFollowUpId`,
  and a new follow-up gets that counter plus one. The counter never goes
  down.
  * Pros: A deleted ID is never handed out again, so an ID the user types,
    such as in `deletefollowup 1 i/2`, always means the same follow-up. IDs
    stay short and easy to type, and existing IDs never change.
  * Cons: The counter must be saved and validated with each contact. Deleting
    leaves gaps in the numbering. Older data files without the counter cannot
    recover IDs deleted before it was added.
* **Alternative 2:** Use the follow-up's position, or the list size plus one.
  * Pros: Nothing extra to store.
  * Cons: Deleting a follow-up renumbers the later ones or reuses an ID, so a
    command that names an older ID could change the wrong follow-up.
* **Alternative 3:** Use random unique IDs, such as UUIDs.
  * Pros: IDs never clash, even across contacts.
  * Cons: Long IDs are slow and error-prone to type in a command-line app.

<!-- @@author -->

### Completing and reopening follow-ups (UC17)

`completefollowup` and `reopenfollowup` share `ChangeFollowUpStatusCommand`, which
holds the displayed contact index, the follow-up ID and the target `FollowUp.Status`.
`ContactFileParser` registers one `ChangeFollowUpStatusCommandParser` per command
word, each fixed to its target status, so the parser only accepts
`CONTACT_INDEX i/FOLLOW_UP_ID` and reports the usage of the word that was typed.

On execution the command resolves the contact from the filtered list and the
action by its stable ID, then builds a replacement `FollowUp` that copies the ID,
description and due date with the new status. `Person#replaceFollowUp` keeps the
action at the same position, so history order and later ID allocation are
unaffected. The command rejects, without mutation, a follow-up that already has
the target status, and a reopen that would leave two pending actions with the
same description. That matches the duplicate rule used by `editfollowup`.
Because the overdue flag and every `followups` view (`FollowUpFilter`) read the
status, a completed action moves from the pending and date-based views to
`followups completed` and loses `[OVERDUE]` with no extra bookkeeping, and a
reopened one moves back. Logic saves the change through the usual transactional save, and
a failed save restores the previous status.

### Deleting follow-ups and the confirmation panel

`DeleteFollowUpCommandParser` accepts `deletefollowup CONTACT_INDEX i/FOLLOW_UP_ID`.
`DeleteFollowUpCommand` finds the displayed contact and its follow-up with that ID, and returns a
`ConfirmationRequest` that previews the follow-up. When the user replies `y`, the confirmed command checks that
the contact is still stored unchanged, and `Person#removeFollowUp` then removes that one follow-up without
renumbering the others or lowering `lastFollowUpId`. Logic saves the change, and restores the previous contact
and displayed list if the save fails. A deleted follow-up cannot be brought back with `undo`, which only
reverses `edit` and `delete`.

`ConfirmationPanel` shows the preview of any `ConfirmationRequest`, such as those of `edit`, `delete`, `clear`,
`deletefollowup` and a duplicate `followup`. Numbered selection requests are not shown in it. `MainWindow` passes
typed replies and the panel's **Confirm** and **Cancel** buttons, which send `y` and `cancel`, to Logic in the
same way, so the panel never changes the model itself. The panel increases a revision number each time it is
shown or hidden, and ignores a button click from an older revision. An invalid reply keeps the panel open;
completing, cancelling or failing the request hides it. The preview wraps and scrolls, and the buttons can be
reached with Tab while the command box stays usable.

The activity diagram below shows the confirmation flow shared by these commands.

![Shared confirmation workflow](images/ConfirmationActivityDiagram.png)

### Display-field order (UC26)

`FieldOrder` is an immutable list of the ten `FieldOrder.Field` values, each exactly once. Its constructor
accepts field names in any letter case and rejects unknown or repeated names. Orders saved before the
introducer and interactions fields existed lack those fields, so the constructor fills in just those two
when they are the only ones missing: a missing `introducer` goes just before `interactions`, or at the end if
`interactions` is missing too, and a missing `interactions` goes at the end. The `order` command goes through
the same constructor, so it also accepts such a list. Any other missing field is rejected, so an order can
never hide a field. `FieldOrder.DEFAULT` is
`name tags company role phone email linkedin followups introducer interactions`.

`UserPrefs` stores the one global order and includes it in its copies and `equals`. In `preferences.json` the
order is the `fieldOrder` array of lower-case names. A file without it uses the default, and a malformed or null
order makes preference loading fall back to the default preferences.

`OrderCommandParser` turns `order` with no arguments into a query, and otherwise passes the field names to
`FieldOrder`; an invalid list is reported together with the accepted fields. `OrderCommand` changes only the
preferences. A query, or an order equal to the current one, changes nothing, so nothing is saved. Because
`ModelSnapshot` also copies the preferences, Logic notices the change with `hasPreferencesChanged`, saves only
the preferences file, and restores the previous order if that save fails. No command changes both files, and
`order` never writes the contact file.

`JsonUserPrefsStorage` writes the preferences to a temporary file next to `preferences.json`, reads it back to
check it, and then replaces the old file in one step. If any of this fails, the old file is kept. A failure to
delete the temporary file after a successful replacement is only logged, since the save itself succeeded.
Preferences saved at startup and shutdown go through the same path.

Logic exposes the field order as a read-only observable property. `MainWindow` listens to it and redraws
`PersonDetailsPanel`, the pane that shows the selected contact's details, in the new order.
`Messages#format(Person, FieldOrder)` uses the same order for the details printed in command results, such as
those of `add` and `delete`. In both, a field without a value is left out instead of leaving an empty row.
Events are not part of `FieldOrder` and always come last. `PersonCard`, the full contact card that
`PersonListPanel` can show, arranges its rows in the same order but does not show interactions, which appear
only in the full details; `MainWindow` uses the compact form of `PersonListPanel`, which shows only each
contact's name and company. Changing the order leaves the contacts, their order, the filter and the selected
contact unchanged. After a failed `order`, `MainWindow` shows the same details as before, in the restored order.

`order` reorders the fields within each contact, not the contacts themselves; sorting contacts is a separate,
lower-priority user story. The activity diagram below shows how `order` handles its input.

![Changing the contact field order](images/FieldOrderActivityDiagram.png)

<!-- @@author Cookiemunchrr -->

#### Design considerations

**Aspect: How much of the order a user must give**

* **Alternative 1 (current choice):** `order` takes every field exactly once,
  apart from `introducer` and `interactions`, which are filled in when they
  are the only fields missing.
  * Pros: An order can never hide a field by leaving it out, and a typo in a
    field name is reported instead of silently dropping that field. Orders
    saved before the two newer fields existed still load.
  * Cons: Moving one field means typing all ten names. The two fields that
    are filled in make the rule slightly harder to explain.
* **Alternative 2:** Accept a partial list, such as `order phone email`, and
  put the listed fields first, followed by the others in their current order.
  * Pros: Quicker to type for a small change.
  * Cons: The final order is harder to predict, and a mistyped name could not
    be told apart from a field the user chose not to list.

**Aspect: Where the field order is stored**

* **Alternative 1 (current choice):** One global order in `UserPrefs`, saved
  in `preferences.json`.
  * Pros: Every contact is shown the same way, so details are easy to scan.
    The contact file is not touched, so `order` cannot damage contact data.
  * Cons: Different kinds of contacts cannot be shown differently.
* **Alternative 2:** A separate order stored with each contact.
  * Pros: Each contact can put its most useful details first.
  * Cons: The user has to set or copy the order for every contact, details of
    different contacts no longer line up, and every saved contact grows.

<!-- @@author -->

<!-- @@author Yx0905 -->

### Interaction history (UC10)

Each `Person` holds an immutable list of `Interaction`s, kept in date order, oldest first. An
`Interaction` is a `LocalDate` and a non-blank note; two interactions are the same if they have the same date
and the same note, ignoring letter case. The class diagram below shows where interactions sit across the
components.

![Classes involved in the interaction history](images/InteractionClassDiagram.png)

`Person` has one constructor that takes every field. `withInteraction`, `withInteractions`, `appendFollowUp`,
`withStarred` and `withEvent` all go through it, as do `edit`, `drop` and `introduce`, so changing one part of a
contact never drops another, such as logging an interaction on a contact with follow-ups.

The sequence diagram below shows how `log 1 nt/Coffee chat` is handled.

![Interactions inside the Logic, Model and Storage components for `log 1 nt/Coffee chat`](images/LogSequenceDiagram.png)

1. `ContactFileParser` passes the arguments to `LogCommandParser`, which fills in today's date when `d/` is left
   out and rejects a future date or a blank note before any `LogCommand` is created.
2. `LogCommand` finds the contact through `PersonNameResolver#resolveTarget`, by displayed index or by name. A
   name that matches several contacts lists them and changes nothing.
3. If the contact already has the same interaction, the command fails and the contact file is left unchanged.
   Otherwise `Person#withInteraction` returns a new `Person`, and `Model#setPerson` replaces the old one.
4. The displayed list keeps its filter, so an index still points to the same contact on the next command.
5. `LogicManager` saves the contact file because the contact data changed.

`Messages#format` shows the history as a numbered list, and it is one of the fields `order` can move, so it
appears wherever full details are shown. `JsonAdaptedInteraction` stores the date as `YYYY-MM-DD`. A contact
file written before this feature has no `interactions` field, which loads as an empty history. Loading checks
the main rules that `log` applies: a missing or invalid date, a future date, a blank note or a repeated
interaction makes that contact invalid, and it goes through the usual recovery path. Unlike `log`, loading does
not reject a date before 2000-01-01.

A name target goes through `PersonNameResolver#resolveTarget`, which filters the displayed list to the
contacts matching the name. `log`, `unlog` and `editlog` leave that filter in place, so after a name target the list shows
those matches. After an index target the list is normally unchanged; the exception is a number that also
appears in a contact's name, where `ContactSelection` first filters the list to the candidates, as described
above.

`unlog` (`UnlogCommand`) removes one entry, addressed by its number in the date-ordered history. Because a
removal cannot be undone and later entries are renumbered, the command returns a `ConfirmationRequest` that
shows the entry, and only the confirmed inner command changes the contact. That inner command first checks that
the contact is still stored unchanged, so a reply cannot remove an entry from a history that changed in between.

`find /notes KEYWORDS` is parsed by `FindCommandParser` into a `FindNotesCommand` with a
`NoteContainsKeywordsPredicate`. A contact matches when any of its notes contains any keyword as a substring,
ignoring letter case. The command filters the displayed list like `find`, and its result groups the matching
notes under each contact, shortening notes longer than 60 characters to an excerpt around the first match. When
nothing matches, the displayed list is left as it was.

`editlog` (`EditLogCommand`) corrects one entry in place. `EditLogCommandParser` accepts the same target
forms as `log` and `unlog`, a required `i/ENTRY`, and at least one of `d/` and `nt/`; it reuses
`LogCommandParser#parsePastDate`, so an edited date follows the same rules as a logged one. The command resolves
the contact through `PersonNameResolver`, builds a new `Interaction` from the given parts and the current
values of the rest, and replaces the entry through `Person#withInteractions`, which sorts the history again.
It rejects an edit that changes nothing and an edit that would duplicate another entry, so the stored
history never breaks the rules that loading checks. A new date can move the entry, so the result reports
its new number together with the renumbered history. No confirmation is asked, because the change can be
reversed by another `editlog`, unlike `unlog`.

Design considerations:

* **Where the history lives:** as a list inside `Person` (chosen) or as a separate store keyed by contact.
  Keeping it in `Person` means `edit`, deletion, undo snapshots and saving all handle it with no extra code. The
  cost is that every method that copies a `Person` must pass the history on, which the single full constructor
  makes hard to forget.
* **Order:** sorted by date when stored (chosen), or kept in the order logged. Sorting means an interaction
  logged late appears where it belongs, and entries are numbered in date order. The cost is that logging,
  editing or removing an entry can renumber the entries after it, which is why `editlog` reports the new number
  and `unlog` asks for confirmation.
* **Correcting an entry:** an `editlog` command (chosen), or only `unlog` followed by `log`. Editing keeps
  the other part of the entry, needs one command instead of two and no confirmation, and cannot lose the
  entry if the second command fails.

### Command shortcuts

`alias` (`AliasCommand`, parsed by `AliasCommandParser`) defines, replaces, removes and lists shortcuts. Shortcuts
are a `Map<String, String>` from a lower-case shortcut to the command text it stands for, held in `UserPrefs` and
saved to `preferences.json`. `LogicManager` saves the preferences straight after a command changes them, and a
failed save restores the previous shortcuts.

`ContactFileParser` keeps the only list of built-in command words. `ContactFileParser#expandShortcut` replaces a
leading shortcut with its command text and keeps the rest of the input, and is skipped when the first word is a
built-in command word, so a shortcut can never hide a command. `AliasCommandParser` uses the same list to reject a
shortcut named after a command, and a target that does not start with a built-in command word other than `alias`,
which also rules out a shortcut standing for another shortcut. When Logic
starts, `AliasCommandParser#validAliases` drops any entry in a hand-edited preferences file that breaks these rules.

`LogicManager` expands the input before parsing. When a shortcut was used, it prefixes the result, and any
parse or command error, with `Ran: <expanded command>`, so the user sees exactly what was run. An error from
saving the file is shown without it.

<!-- @@author -->

### Undo and session history

Astra does not use the AB3 `VersionedContactFile` redo stack. The implemented undo feature is session-scoped and command-driven: `UndoManager` keeps a list of successful data-changing commands and remembers the latest undoable `edit` or `delete` entry. This is intentionally limited to the current app session; when Astra closes, the undo history is cleared.

`UndoCommand` calls `UndoManager#executeUndo`, which returns a result carrying a `ConfirmationRequest` that previews the most recent edit or deletion. The preview does not mutate the model or save the contact file. If the user replies `y`, the confirmed request restores the previous person version by replacing the changed contact or re-inserting the deleted contact at its original index. It also validates the resulting list against contact uniqueness rules before accepting the undo, so a restored person cannot introduce duplicate names, emails or phone numbers. Only edit and deletion actions are tracked for undo; other commands either do not modify contact data or are explicitly not undoable.

`history` shows, newest first, every successful `add`, `edit`, `delete`, `drop`, `clear` and `introduce` in the session, and marks each undone item as `(undone)`. This gives the user a concise record of restore actions without exposing the AB3 redo model or stale state diagrams.

The sequence diagram below shows a batch-style delete undo flow: the user deletes a contact, reviews the undo preview, then confirms restoration. The same pattern is used for edit undo, with the restore step replacing the changed person rather than re-adding it.

![Batch delete undo flow](images/BatchDeleteUndoSequenceDiagram.png)

#### Design considerations

**Aspect: How undo is implemented**

* **Alternative 1 (current choice):** Keep a session list of successful edit/delete snapshots in `UndoManager`.
  * Pros: Undo is centralized, easy to reason about, and fits the app's confirmation workflow. The preview and confirm steps are handled in one place, and the data model need not implement its own rollback logic.
  * Cons: Undo is limited to the current session and only tracks the latest undoable change, which is deliberate for the app's current user flow.

* **Alternative 2:** Store a full undo/redo stack in the model.
  * Pros: Keeps a longer history of state changes.
  * Cons: It is more complex than the app needs, and it does not match the implemented confirmation-first undo workflow or the existing session history design.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* attends networking events, career fairs or conferences regularly
* meets many new people in a short span and struggles to recall who was who afterwards
* wants to capture contact details (company, role phone number, email, LinkedIn) and context in seconds, mid-conversation or right after
* prefers desktop apps over other types of applications
* can type fast and prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Networking events generate more contacts than anyone can remember and more context than a phone contacts app can hold. Astra lets a fast typist capture each new person (role, company, LinkedIn, who introduced them, and what was discussed) in the seconds between conversations, and search any of it back up instantly, so no one you met becomes "that person from the fair".

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                                    | I want to …​                     | So that I can…​                                                        |
| -------- | ------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| `* * *`  | user                                       | add a new contact with their name, company, role and contact details               | remember and reach out to the people I meet                            |
| `* * *` | user                                        | view all the information stored about a contact in one place   | refresh my memory before meeting them again                      |
| `* * *`  | user                                       | record a contact's LinkedIn profile | look up their professional background later                       |
| `* * *`  | user                                       | delete a person                | remove entries that I no longer need                                   |
| `* * *`  | user                                       | find a person by name          | locate details of persons without having to go through the entire list |
| `* * *`  | user                                     | edit a contact's details                                            | fix mistakes or add details I learn later                         |
| `* * *`  | user                                       | clear all contacts | start over when I no longer need the list                              |
| `* *`    | careless user                              | be asked to confirm before contacts are deleted or cleared | avoid wiping out contacts by a mistyped command                  |
| `* * *`  | user                                       | exit the app with all saved changes kept | close it without worrying about losing data
| `* * *`  | user                                       | find a contact by name, phone number or email                       | retrieve their details quickly                                    |
| `* * *`  | user                                       | find a contact using only part of their name                        | look someone up when I only remember their first or last name     |
| `* * *`  | user                                       | list all contacts                                                   | browse my contact list                                            |
| `* * *`  | user                                     | retrieve my contacts after closing and reopening the app            | keep what I have recorded                                         |
| `* * *`  | new user                                 | view help for the available commands                                | learn the commands without memorising them                        |
|`* * `| user                                          | record where I met the contact and what we discussed       | have context for the next conversation                         |
|`* * `| user                                          | record who introduced me to a contact                     | remember how I am connected to them |
| `* *`    | user who struggles with names              | find someone without typing the exact spelling of their name        | look them up even when I misremember their name                   |
| `* * *`  | user                                       | find a contact by company or role                                   | retrieve contacts from a particular organisation or profession     |
| `* * *`  | user                                       | find contacts by an event                                          | recall who I met at a particular networking event                 |
| `* *`    | user                                       | filter contacts by categories or custom tags                        | narrow down a large list quickly                                  |
| `* *`    | frequent user                              | star important contacts                                             | revisit them quickly                                              |
| `* *`    | user                                       | hide private contact details   | minimize chance of someone else seeing them by accident                |
| `* *`    | user                                       | record follow-up actions for a contact | remember what I need to do after meeting them                          |
| `* *`    | user                                       | mark a follow-up action as completed | tell pending tasks apart from finished ones                            |
| `* *`    | user                                       | see when I last interacted with someone | know how long it has been since we last met                            |
| `* *`    | careless user                            | undo an accidental deletion or edit                                 | avoid losing important information                                |
| `* *`    | potential user exploring the app         | see the app populated with sample data                              | get a feel for how the app looks in use                           |
| `*`      | user with many persons in the contact file | sort persons by name           | locate a person easily                                                 |
| `*`      | user                                     | see which contact entries are incomplete                            | remember to fill in missing details later                         |
| `*`      | frequent user                            | reorder the information shown for a contact                         | see the details I care about first                                |
| `*`      | expert user                              | define shortcuts for commands                                       | add contacts faster than typing commands in full                  |
| `*`      | frequent user                            | reuse templates of common fields when adding contacts               | enter repetitive data faster                                      |
| `*`      | first-time user                          | follow a tutorial of the main features                              | start using the app confidently                                   |
| `*`      | user                                     | protect my contact data with a password                             | stop other people using my device from viewing it                 |
| `* *`    | user                                     | remove one detail of a contact without retyping the others          | discard outdated details quickly                                  |
| `* *`    | user                                     | log what I discussed with a contact and when                        | prepare for our next conversation                                 |
| `* *`    | user                                     | correct or remove an interaction I logged by mistake                | keep a contact's history accurate                                 |
| `* *`    | user                                     | find contacts by what we discussed                                  | recall who mentioned a topic                                      |
| `* *`    | user                                     | review overdue, upcoming and undated follow-ups across all contacts | decide what to do next                                            |
| `* *`    | user                                     | edit or delete a follow-up action                                   | keep my to-do list accurate                                       |
| `*`      | user                                     | see the changes I made to my contacts in this session               | check what I changed before I undo anything                       |

### Use cases

The following use case diagram summarizes the main interactions supported by Astra.

<img src="images/UseCaseDiagram.png" width="700" />

(For all use cases below, the **System** is `Astra` and the **Actor** is the `user`, unless specified otherwise)

**Use case: UC01 - Add a contact**

**MSS**

1. User enters an add command with the contact's name and whichever optional details they have: company,
   role, phone number, email, LinkedIn profile, introducer, an event and tags.
2. Astra validates and normalises the details.
3. Astra adds the contact, saves the contact file and shows the added details.

   Use case ends.

**Extensions**

* 1a. The name is missing, a prefix is unsupported, a single-valued prefix is repeated, or a value is invalid.
    * 1a1. Astra shows the relevant error message and accepted format, and adds nothing.

      Use case ends.

* 2a. The phone number or email already belongs to an existing contact.
    * 2a1. Astra rejects the command, stating which detail is already taken, and adds nothing.

      Use case ends.

* 2b. The name matches an existing contact's name, ignoring letter case, and the two contacts either both
  have no email or have the same email.
    * 2b1. Astra reports that the contact already exists and adds nothing.

      Use case ends.

* 2c. The introducer's name is the contact's own name.
    * 2c1. Astra reports that a contact cannot be their own introducer and adds nothing.

      Use case ends.

* 3a. Saving fails.
    * 3a1. Astra shows a save error. The contact list and file are unchanged.

      Use case ends.

**Deferred to a later version**
* When the phone number or email belongs to an existing contact, offering to keep the existing contact,
  replace it, or merge the two instead of rejecting the command.

**Use case: UC02 - Delete a contact**

**MSS**

1. User requests to delete a contact, identifying it by its index in the displayed list, its name, its phone
   number or its email, or requests to delete several contacts by their emails.
2. Astra shows the contacts to be deleted and asks for confirmation.
3. User confirms.
4. Astra deletes the contacts, saves the change, and shows the deleted contacts, the number of contacts
   remaining and the full contact list.

   Use case ends.

**Extensions**

* 1a. There are no contacts.
    * 1a1. Astra shows that there are no contacts to delete.

      Use case ends.

* 1b. The command format is invalid, the index is not in the displayed list, or no contact has the given name,
  phone number or email.
    * 1b1. Astra shows the error and deletes nothing. If any one of several emails matches no contact, no
      contact is deleted.

      Use case ends.

* 1c. The given name matches several contacts.
    * 1c1. Astra shows a numbered list of the matching contacts.
    * 1c2. User enters the number of the contact to delete.

      Use case resumes at step 2.

    * 1c2a. User enters a number that is not in the list, or any other reply.
        * 1c2a1. Astra shows the list again.

          Use case resumes at step 1c2.

    * 1c2b. User cancels.
        * 1c2b1. Astra deletes nothing and shows the list as it was before the request.

          Use case ends.

* 1d. The same email is given more than once.
    * 1d1. Astra lists that contact only once.

      Use case resumes at step 2.

* 3a. User cancels.
    * 3a1. Astra deletes nothing and shows the list as it was before the request.

      Use case ends.

* 3b. User enters any other reply.
    * 3b1. Astra asks for confirmation again.

      Use case resumes at step 3.

* 3c. User enters `exit`.
    * 3c1. Astra deletes nothing and closes.

      Use case ends.

* 4a. Saving fails.
    * 4a1. Astra shows a save error. The contacts, the contact file and the displayed list are unchanged.

      Use case ends.

**Use case: UC03 - Find and view a contact**

**MSS**

1. User enters `view INDEX` from the displayed list (optionally narrowed with `find`), or `view NAME`.
2. Astra resolves the displayed index or searches all contacts by partial, case-insensitive name and shows
   the selected contact's full stored information, omitting absent optional fields. No contact data is saved.

   Use case ends.

**Extensions**

* 1a. The contact list is empty.
    * 1a1. Astra reports an invalid displayed index or a missing named contact, without changing data.

      Use case ends.

* 1b. The selector is missing or malformed, or the displayed index is out of range.
    * 1b1. Astra shows usage or an index error and preserves the previous filter.

      Use case ends.

* 2a. No contact matches.
    * 2a1. Astra shows that the contact was not found and restores the previous filter.

      Use case ends.

* 2b. Several contacts match.
    * 2b1. Astra narrows the displayed list to matches in stored order and shows numbered names,
      companies, and emails where present. No contact is chosen automatically.
    * 2b2. User enters `view INDEX` using a candidate's displayed number.
    * 2b3. Astra shows all stored information of the selected contact.

      Use case ends.

    * 2b2a. User enters an invalid index.
        * 2b2a1. Astra shows an error message.

          Use case resumes at step 2b2.

**Use case: UC04 - Edit a contact**

**MSS**

1. User enters an edit command, identifying the contact either by its index in the displayed list or by name, with the new values of one or more fields.
2. Astra finds the contact, validates the new values, and previews the changes and resulting contact.
3. User confirms with `y` or the confirmation panel's Confirm button.
4. Astra updates the contact, saves the change, lists every updated field, and shows the full contact list again.

   Use case ends.

**Extensions**

* 1a. No field to change is given.
    * 1a1. Astra shows that at least one field must be provided.

      Use case ends.

* 1b. The command format is invalid, a single-valued prefix is repeated, or a value is malformed.
    * 1b1. Astra shows the relevant error message, with the command usage if the format is invalid.

      Use case ends.

* 1c. A company, role, contact number, email or LinkedIn prefix is given with no value, e.g. `c/`.
    * 1c1. Astra treats it as removing that field from the contact.

      Use case resumes at step 2.

* 1d. The command contains a prefix that `edit` does not support.
    * 1d1. Astra names the unsupported prefix and changes nothing. For `i/`, it tells the user to use
      `introduce NAME i/INTRODUCER` instead.

      Use case ends.

* 1e. `t/` is given with no tag after it.
    * 1e1. Astra treats it as removing all of the contact's tags.

      Use case resumes at step 2.

* 2a. The given index is not in the displayed list.
    * 2a1. Astra shows that the index is invalid.

      Use case ends.

* 2b. The given name matches no contact.
    * 2b1. Astra shows that no contact of that name was found, and changes nothing.

      Use case ends.

* 2c. Several contacts match the given name.
    * 2c1. Astra shows a numbered list of the matching contacts with their names, companies and emails, and changes nothing.
    * 2c2. User enters the command again with the index of the contact they meant.

      Use case resumes at step 2.

* 2d. The new contact number or email already belongs to another contact.
    * 2d1. Astra rejects the whole edit, names the other contact, and changes nothing.

      Use case ends.

* 2e. The new name matches another contact's name, ignoring letter case, and the two contacts either both have
  no email or have the same email.
    * 2e1. Astra reports that the contact already exists and changes nothing.

      Use case ends.

* 2f. Every new value is identical to the current one.
    * 2f1. Astra shows that there are no changes to apply and does not save.

      Use case ends.

* 2g. The event to add is already associated with the contact.
    * 2g1. Astra shows that the contact already has that event and changes nothing.

      Use case ends.

* 2h. The new name is the same as the contact's recorded introducer.
    * 2h1. Astra reports that a contact cannot be their own introducer and changes nothing.

      Use case ends.

* 2i. The index is a number that also appears in contact names, so the displayed contact at that index and the
  contacts with that number in their names give several candidates.
    * 2i1. Astra shows a numbered list of the candidates.
    * 2i2. User enters a candidate's number, or `n`/`cancel` to stop.

      Use case resumes at step 2.

* 3a. User cancels.
    * 3a1. Astra changes nothing and shows the list as it was before the request.

      Use case ends.

* 3b. User enters any other reply.
    * 3b1. Astra asks for confirmation again.

      Use case resumes at step 3.

* 3c. User enters `exit`.
    * 3c1. Astra changes nothing and closes.

      Use case ends.

* 4a. Saving fails.
    * 4a1. Astra shows a save error. The contact, the contact file and the displayed list are unchanged.

      Use case ends.

**Deferred to a later version**
* Choosing one of several contacts that match a typed name at a prompt instead of entering the command again.
* Warning and asking for confirmation when the new name or LinkedIn URL matches another contact, instead of
  rejecting an identical name outright.

**Use case: UC05 - Clear all contacts**

**MSS**

1. User requests to clear all contacts.
2. Astra shows how many contacts are stored and asks for confirmation.
3. User confirms.
4. Astra deletes every contact, saves the change, and reports how many contacts were cleared and that none remain.

   Use case ends.

**Extensions**

* 1a. There are no contacts.
    * 1a1. Astra shows that there is nothing to clear.

      Use case ends.

* 1b. User adds extra text after the command.
    * 1b1. Astra shows the accepted format and clears nothing.

      Use case ends.

* 2a. Only some contacts are displayed, such as after a search.
    * 2a1. Astra counts every stored contact, not only the displayed ones.

      Use case resumes at step 3.

* 3a. User cancels.
    * 3a1. Astra clears nothing and shows the list as it was before the request.

      Use case ends.

* 3b. User enters any other reply.
    * 3b1. Astra asks for confirmation again.

      Use case resumes at step 3.

* 4a. Saving fails.
    * 4a1. Astra shows a save error. The contacts and the contact file are unchanged.

      Use case ends.

**Use case: UC06 - Start Astra and restore saved contacts**

**MSS**

1. User launches Astra.
2. Astra reads the contact file and loads every contact in stored order.
3. Astra shows how many contacts were loaded and from which file, then waits for a command.

   Use case ends.

**Extensions**

* 2a. The contact file does not exist (first launch).
    * 2a1. Astra loads sample contacts to help the user explore the app.
    * 2a2. Astra explains that these are sample contacts and that the user
      can remove them using the `clear` command.

      Use case ends.

* 2b. The contact file can be parsed but contains invalid or conflicting records.
    * 2b1. Astra validates records in stored order, retaining valid contacts
      and the first valid occurrence of each conflicting contact.
    * 2b2. Astra preserves the original file unchanged in a uniquely named,
      timestamped recovery folder.
    * 2b3. Astra writes a report listing the original positions and reasons
      for skipped records.
    * 2b4. Astra safely replaces the active contact file with the recovered contacts.
    * 2b5. Astra shows the recovered and skipped counts, backup location,
      and report location.

      Use case ends.

    * 2b4a. No valid contacts can be recovered.
        * 2b4a1. Astra leaves the active file unchanged, shows the backup
          and report locations, and offers the fresh-start choice described in 2c.

          Use case ends.

    The following sequence diagram illustrates storage recovery in extension 2b,
    including the case where no valid contacts can be recovered.

    ![UC06 extension 2b: recovery of invalid or conflicting contact records](images/UC06Extension2bSequenceDiagram.png)

* 2c. The contact file cannot be read or reliably parsed, or recovery cannot
  create its backup/report or safely replace the active file.
    * 2c1. If Astra can read the failed contact file, it preserves its exact bytes
      in a uniquely named incident folder under `data/reports` and writes a failure report.
    * 2c2. Astra shows the error, confirms where the original file is preserved,
      and offers **Start anew** or **Exit**. Exit is the default choice.
    * 2c3. If the user chooses Start anew, Astra verifies the backup and checks
      that the active file has not changed, then atomically replaces it with an
      empty contact file and opens the command interface. The original backup remains unchanged.
    * 2c4. Astra shows an empty contact list and reminds the user of the backup and report locations.

    * 2c2a. The original file cannot be read or safely preserved under reports.
        * 2c2a1. Astra explains that starting anew is unavailable and stops without clearing the contact file.
    * 2c2b. The user chooses Exit or dismisses the dialog.
        * 2c2b1. Astra stops without replacing the active file.
    * 2c3a. Backup verification or atomic replacement fails, or the active file changes.
        * 2c3a1. Astra shows the failure and available archive locations and stops without opening the command interface.

      Use case ends.

* 2d. Another Astra instance is already using the contact file.
    * 2d1. Astra shows that the storage is in use and the second instance stops.

      Use case ends.

**Use case: UC07 - Find contacts by company or role**

**MSS**

1. User enters a company search, role search, or combined company-and-role search.
2. Astra validates the search criteria.
3. Astra displays the matching contacts.
4. User selects a contact using its displayed index.
5. Astra displays the selected contact's details.
   Use case ends.

**Extensions**

- **1a.** No search value is provided.
  - Astra displays the accepted command format.
  - Use case ends.

- **3a.** No contacts match the search criteria.
  - Astra reports that no matching contacts were found.
  - Use case ends.

- **3b.** Both company and role are specified.
  - Astra displays only contacts matching both criteria.
  - Use case resumes at step 4.


**Use case: UC09 - Record and retrieve contacts by event**

**MSS**

1. User adds an event while creating or editing a contact.
2. Astra validates and stores the event with the contact.
3. User enters `find /event EVENT`.
4. Astra displays contacts associated with the event.
5. User selects a displayed contact.
6. Astra displays the contact's stored details and events.
   Use case ends.

**Extensions**

- **1a.** The event name is missing or blank.
  - Astra displays the accepted command format.
  - Use case ends.

- **2a.** The event value is invalid.
  - Astra rejects the command and leaves the contact unchanged.
  - Use case ends.

- **4a.** No contacts are associated with the event.
  - Astra reports that no matching contacts were found.
  - Use case ends.

**Use case: UC21 - Star and retrieve important contacts**

**MSS**

1. User requests to star a contact.
2. Astra marks the contact as starred and saves the change.
3. User later requests the starred contact list.
4. Astra displays all starred contacts.

   Use case ends.

**Extensions**

* 1a. The contact is already starred.
    * 1a1. Astra reports that the contact is already starred and makes no changes.

      Use case ends.

* 1b. User requests to remove a contact's star.
    * 1b1. Astra removes the star, saves the change and displays the updated status.

      Use case ends.

* 4a. No contacts are starred.
    * 4a1. Astra reports that there are no starred contacts.

      Use case ends.

**Use case: UC16 - Record a follow-up action**

**MSS**

1. User requests to record a follow-up for a contact in the displayed list, giving a description and,
   optionally, a due date.
2. Astra adds the follow-up as pending, saves the change, and shows the contact's follow-ups.

   Use case ends.

**Extensions**

* 1a. The index is not in the displayed list, the description is missing or blank, the command has an unknown
  or repeated prefix, or the due date is invalid or before 2000-01-01.
    * 1a1. Astra shows the error and adds nothing.

      Use case ends.

* 1b. The contact already has a pending follow-up with the same description.
    * 1b1. Astra shows the existing follow-up and asks whether to add another.
    * 1b2. User confirms.

      Use case resumes at step 2.

    * 1b2a. User cancels.
        * 1b2a1. Astra adds nothing.

          Use case ends.

    * 1b2b. User enters any other reply.
        * 1b2b1. Astra asks again.

          Use case resumes at step 1b2.

* 2a. Saving fails.
    * 2a1. Astra shows a save error. The contact's follow-ups and the contact file are unchanged.

      Use case ends.

**Notes**

Duplicate confirmation uses a y/n reply rather than a confirmation suffix on the
command, so the user does not have to retype the command. The command accepts an optional
`d/DATE` before or after `a/DESCRIPTION`, in ISO, day/month/year, or English abbreviated-month format. Dates
display in ISO format with an overdue indicator for pending actions due before
today. Duplicate confirmation retains the requested date and creates a separate
action. Completion and reopening (UC17), editing and deletion have separate commands;
they are not part of this use case.

**Use case: UC17 - Complete or reopen a follow-up action**

**MSS**

1. User requests to complete a pending follow-up of a displayed contact, giving its ID.
2. Astra marks the action as completed, keeping its ID, description, due date and position.
3. Astra saves the change and shows the updated action, which now appears only in the completed follow-ups view.

   Use case ends.

**Extensions**

* 1a. The index or format is invalid: report the usage without modifying or saving contacts.
* 1b. The contact has no follow-up with that ID: report the error without saving.
* 1c. User requests to reopen a completed action instead: Astra marks it as
  pending, and it reappears in the pending follow-ups (with `[OVERDUE]` if its
  due date is before today).
    * 1c1. Another pending action of the contact has the same description:
      report the conflicting ID without saving.
* 2a. The action already has the requested status: report it without saving.
* 3a. Saving fails: restore the previous status, file and view, and report the failure.

**Use case: UC10 - Log an interaction**

**MSS**

1. User requests to log an interaction with a contact, identified by index or name, with a note and an optional
   date.
2. Astra adds the interaction to the contact's history in date order, using today's date if none is given.
3. Astra saves the change and shows the contact's numbered interaction history.

   Use case ends.

**Extensions**

* 1a. The format is invalid, a prefix is repeated, the note is blank, or the date is in the future or before
  2000-01-01: report the error without modifying or saving contacts.
* 1b. The index is not in the displayed list, or no contact matches the name: report the error without saving.
* 1c. The name matches several contacts: list them and change nothing.
* 2a. The contact already has an interaction with the same date and note, ignoring letter case: report it
  without saving.
* 3a. Saving fails: restore the previous history, file and view, and report the failure.

**Use case: UC11 - Edit a logged interaction**

**MSS**

1. User requests to change the date and/or note of an entry in a contact's interaction history.
2. Astra replaces the entry, keeping the history in date order.
3. Astra saves the change and shows the entry's new number and the updated history.

   Use case ends.

**Extensions**

* 1a. The format is invalid, a prefix is repeated, neither a date nor a note is given, the date is in the
  future or before 2000-01-01, or the note is blank: report the error without modifying or saving contacts.
* 1b. The name matches several contacts: list them and change nothing.
* 1c. The contact has no entry with that number: report how many entries it has, without saving.
* 1d. The index is not in the displayed list, or no contact matches the name: report the error without saving.
* 2a. The edit leaves the entry unchanged, or makes it the same as another entry: report it without saving.
* 3a. Saving fails: restore the previous history, file and view, and report the failure.

**Use case: UC12 - Review follow-ups**

**MSS**

1. User enters `followups`, optionally adding `overdue`, `today`, `upcoming`, `completed` or `undated`.
2. Astra retrieves actions across all stored contacts, including contacts hidden by a contact filter,
   and applies the selected status or date filter. The default view includes pending actions only.
3. Astra displays numbered entries with the contact name, stable action ID,
   description, status and due date. Dates sort ascending; equal dates retain
   contact order and stored action order. Pending actions due before today are
   marked `[OVERDUE]`.
4. User enters a displayed row number.
5. Astra displays the selected action's details and its associated contact.
   Use case ends.

**Extensions**

* 1a. An unknown selector or extra arguments: reject the command and display usage instructions.
* 2a. No matching actions remain: show an empty message for the selected view.
* 3a. Undated actions: list them after dated actions with `No due date`.
* 4a. Invalid numeric reply: repeat the retained list and selection instructions.
* 4b. User enters `n` or `cancel`: clear the request without changing data.
* 4c. Selected contact or action history changed or the contact was deleted:
  clear the request and ask the user to run `followups` again.
* 4d. Non-number input: clear selection and parse/execute the input as a normal
  command. Invalid commands show their normal errors and leave selection closed.

**Implementation and scope**

`ModelManager.getFollowUps()` returns an unmodifiable, sorted snapshot of all actions paired
with their contacts as `FollowUpEntry` records. The pending-only query reuses this snapshot.
The command applies `FollowUpFilter` using one local date from its clock per execution.
Completed includes all completed actions; undated includes only pending actions without dates.
Upcoming has no upper date limit; date views exclude undated and completed actions.
The default view includes all pending actions. Filtered snapshots retain
the existing order. The command uses the shared formatter, preserves the contact filter and
never saves data. A nonempty list returns a `FollowUpSelectionRequest`, which
copies entries in displayed order. Logic retains it through the existing
pending-request flow. Valid replies call `ViewCommand.forPerson` with the selected contact,
then attach the selected action using `CommandResult.withSelectedFollowUp`. This reuses the
normal contact-view result without another name/index lookup or a contact-filter change.
Invalid numeric replies keep the same request.
Non-number replies are dispatched through the normal command parser by Logic,
after clearing the selection request. Other pending requests retain their existing
reply handling, including confirmation prompts.
Selection verifies that the captured contact still exists with the same data.
The UI lists the entries in `FollowUpListPanel` and displays the selected
contact and follow-up in `PersonDetailsPanel`, while retaining the contact filter. Running the list again retrieves current results. Requests are held
only in memory and do not survive restart.

The selection request declares itself read-only. Logic captures the current
model before each reply instead of reusing the pre-prompt transaction snapshot,
so cancellation or stale-selection errors cannot undo intervening changes.
Selection does not save those intervening changes either. The detail result
retains the contact and optional selected follow-up as separate data. The UI renders
contact details through the same path as `view`, including field-order refreshes, and
shows the selected follow-up alongside them without showing the contact's details twice.

**Use case: UC20 - Filter contacts by tags**

**Preconditions:** Some contacts have tags.

**MSS**

1. User requests to list the contacts that have all of one or more given tags.
2. Astra shows, in stored order, every stored contact that has all of the tags, with letter case counted, and
   the number of matching contacts.

   Use case ends.

**Extensions**

* 1a. User requests the contacts that have any of the given tags instead.
    * 1a1. Astra shows every stored contact that has at least one of the tags.

      Use case ends.

* 1b. No tag is given, a tag is invalid, or the matching mode is unknown or given more than once.
    * 1b1. Astra shows the error and keeps the displayed list.

      Use case ends.

* 1c. The same tag is given more than once.
    * 1c1. Astra uses it once.

      Use case resumes at step 2.

* 2a. No contact matches.
    * 2a1. Astra shows an empty list and a message that no contacts match.

      Use case ends.

**Use case: UC26 - Reorder displayed contact information**

**MSS**

1. User requests to see the current order of contact fields.
2. Astra shows the current field order.
3. User requests a new order, listing every field once in the order wanted.
4. Astra saves the new order, which is kept when Astra restarts, and shows contact details in that order.

   Use case ends.

**Extensions**

* 3a. A field name is unknown or repeated, or a field other than the introducer or the interaction history
  is missing.
    * 3a1. Astra shows the accepted field names and keeps the current order.

      Use case ends.

* 3b. Only the introducer, the interaction history, or both are missing.
    * 3b1. Astra places a missing introducer just before the interaction history, and a missing interaction
      history last.

      Use case resumes at step 4.

* 3c. The new order is the same as the current one.
    * 3c1. Astra reports that no change is needed.

      Use case ends.

* 4a. Saving fails.
    * 4a1. Astra shows an error and keeps the previous order.

      Use case ends.

* 4b. A contact has no value for a field.
    * 4b1. Astra leaves that field out of the contact's details.

      Use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should hold up to 1000 contacts and respond to any command within 1 second on a typical laptop, so that the user can keep up while meeting people in quick succession.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  Should work without an internet connection, and should never send contact data over the network.
5. Should store data locally in a human-readable JSON file, without needing a database management system.
6. Changes from a successful command should survive closing and reopening the app. A crash during a save should leave either the complete old file or the complete new file, never a partially written one.
7. Should never silently discard data from a _corrupted contact file_: the original file is backed up before anything is replaced.
8.  Should allow only one running instance to use the contact file at a time.
9.  Should accept names containing non-English (Unicode) letters.
10. Every error message should state what was wrong and, where relevant, the accepted format.
11. Should be packaged as a single JAR file that runs without an installer.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Contact**: A record of one person. Only the name is required. A company, a role, a contact number, an
  email address, a LinkedIn profile and an introducer are each optional and hold at most one value. A contact
  can also have any number of tags, events, follow-up actions and logged interactions
* **Contact number**: A phone number made up of at least three digits, which may also use spaces, hyphens, a leading plus sign for a country code, and balanced parentheses
* **Displayed index**: The number shown beside a contact in the currently displayed list, starting at 1. Commands that take an `INDEX` use it
* **Prefix**: A field marker in a command, such as `c/` for a company or `e/` for an email. Prefixes are case-sensitive
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Normalisation**: Trimming outer spaces and collapsing repeated spaces before a value is stored or compared
* **Unique key**: A contact number or email. No two contacts may share one
* **Duplicate contact**: An incoming contact whose contact number or email already belongs to an existing contact, or whose name and email identify an existing contact
* **Follow-up action**: A task to do for a contact, with a description, an optional due date, a status (`PENDING` or `COMPLETED`) and an ID that is never reused within that contact
* **Interaction**: A dated note of a conversation or meeting with a contact
* **Pending request**: A prompt that waits for the user's next reply, such as a y/n confirmation or a choice from a numbered list
* **MSS (Main Success Scenario)**: The most common path through a use case, where everything goes as expected
* **CLI (Command Line Interface)**: A text-based interface where the user types commands instead of clicking
* **Candidate list**: The numbered list of matching contacts shown when a name matches several contacts. Indices start at 1
* **Contact file**: The file that stores all contacts, at `data/contactfile.json` in the folder Astra is launched from
* **Corrupted contact file**: A contact file with records that are malformed, have missing fields or invalid values, or conflict with earlier records
* **Backup file**: An unchanged copy of a corrupted contact file, saved as `contactfile.original.json` in a timestamped folder under `data/recovery/` or `data/reports/`, next to a `report.txt`

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually. They complement the User Guide, which describes each
command's rules in full, so the steps below give a path through the features and the inputs worth trying
rather than every variation.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

**Starting from the sample data.** Unless stated otherwise, each section starts from a fresh copy of the
sample data: close Astra, delete the `data` folder next to the JAR file, and launch Astra again. Delete
`preferences.json` too if you want the default field order and no shortcuts. The six sample contacts are, in
order: 1 Alex Yeoh, 2 Bernice Yu, 3 Charlotte Oliveiro, 4 David Li, 5 Irfan Ibrahim and 6 Roy Balakrishnan.
Sample follow-up and interaction dates are counted from the day the samples are first loaded.

### Launch and shutdown

1. Initial launch
   1. Copy the JAR file into an empty folder, and run `java -jar contactfile.jar` from that folder.<br>
      Expected: the GUI opens with the six sample contacts and a welcome message explaining how to clear them.
1. Saving window preferences
   1. Resize the window and move it to a different location. Enter `exit`.
   1. Launch Astra again.<br>
      Expected: the most recent window size and location are retained.
1. Running a second instance
   1. While Astra is running, launch the same JAR file again from the same folder.<br>
      Expected: the second instance reports that the contact file is in use and stops. The first is unaffected.

### Adding and editing contacts

1. `add n/Jane Tan`<br>
   Expected: Jane Tan is added with only a name. Enter it again: rejected as an existing contact.
   `add n/jane tan e/janetan@example.com` is accepted, because the two contacts have different emails.
1. `add n/Sam Lee p/8743 8807`<br>
   Expected: rejected, because Alex Yeoh already has that number once spaces are ignored.
   `add n/Sam Lee i/sam  lee` is rejected because a contact cannot be their own introducer.
1. `edit 1 p/91234567`<br>
   Expected: a preview of the change and the resulting contact, with nothing changed yet. Enter `n`: Alex is
   unchanged. Repeat the edit and enter `y`: Alex's phone is updated.
1. `edit 1 c/ r/`, then `y`<br>
   Expected: Alex's company and role are removed; other details remain.
1. `edit Yu e/alexyeoh@example.com`<br>
   Expected: rejected, because the email belongs to Alex Yeoh. `edit 1 i/Jane` is rejected with a pointer to
   `introduce`.

### Removing details and recording introducers

1. `drop 1 t/mentor`, then `y`<br>
   Expected: only the `mentor` tag is removed from Alex; the other tags remain.
   `drop 1 t/nosuchtag` and `drop 1 n/Alex Yeoh` are rejected without changes.
1. `introduce 4 i/Jane Smith`<br>
   Expected: David Li has no introducer, so Jane Smith is recorded at once.
1. `introduce 1 i/Jane Smith`<br>
   Expected: Alex already has Bernice Yu as introducer, so Astra asks before replacing. Enter `n`: unchanged.
1. `add n/Alex Tan e/alextan@example.com`, then `introduce Alex i/Roy Balakrishnan`<br>
   Expected: the two Alexes are listed with indexes. Enter `9`: the list is shown again. Enter `2`: Roy
   Balakrishnan is recorded as Alex Tan's introducer.

### Starring contacts

1. `starred`<br>
   Expected: Alex Yeoh, Bernice Yu, Irfan Ibrahim and Roy Balakrishnan, in stored order.
1. `list`, then `star 3`<br>
   Expected: Charlotte Oliveiro is starred. Entering `star 3` again reports that she is already starred.
1. `unstar /email royb@example.com`<br>
   Expected: Roy Balakrishnan is no longer starred.

### Finding contacts

1. `find yeo`<br>
   Expected: Alex Yeoh. Part of a name is enough.
1. `find /fuzzy Bernise` and `find /fuzzy Bernise Yu`<br>
   Expected: Bernice Yu in both cases, despite the misspelling.
1. `find /contact 8743-8807`<br>
   Expected: Alex Yeoh. `find /contact 8743` reports `Contact not found.`, since partial numbers do not match.
1. `find /email ALEXYEOH@example.com`<br>
   Expected: Alex Yeoh. Letter case is ignored.
1. `find /company sho`, `find /role engineer`, and `find /company DBS /role Engineer`<br>
   Expected: Alex Yeoh, Alex Yeoh, and `Contact not found.` respectively.
1. `find /event meetup`<br>
   Expected: Alex Yeoh, Charlotte Oliveiro and Irfan Ibrahim.
1. `find /notes internship`<br>
   Expected: Alex Yeoh, with the date and an excerpt of the matching note.
1. `find`, `find /fuzzy` and `find /notes`<br>
   Expected: an error with the accepted format; the displayed list is unchanged.

### Filtering by tags

1. `filter t/careerfair`, then `filter t/careerfair t/referral`<br>
   Expected: Alex, Bernice and Roy; then only Bernice and Roy, who have both tags.
1. `filter mode/or t/finance t/data`<br>
   Expected: David Li and Irfan Ibrahim.
1. `filter t/CareerFair`<br>
   Expected: an empty list and a no-match message, because tags are case-sensitive.
1. `filter mode/xor t/data`, `filter t/` and `filter mode/and t/data mode/or`<br>
   Expected: an error each time, with the previous list kept.

### Viewing contacts

1. `view 2`<br>
   Expected: Bernice Yu's full details, including her interaction history and events, in the detail panel.
1. `find Yu`, then `view Alex Yeoh`<br>
   Expected: Alex's details, even though the previous search hid him.
1. `add n/Alex Tan e/alextan@example.com`, then `view Alex`<br>
   Expected: both Alexes are listed with indexes and no prompt appears. `view 2` then shows Alex Tan.
1. `view 0`, `view 99`, and `view n/Alex`<br>
   Expected: an error each time, with the displayed list kept.

### Deleting contacts

1. `delete 6`<br>
   Expected: a preview of Roy Balakrishnan and nothing deleted. Enter `list`: the prompt is repeated. Enter `n`:
   nothing changes. Repeat and enter `y`: Roy is deleted and the remaining count is shown.
1. `add n/Alex Tan e/alextan@example.com`, then `delete Alex`<br>
   Expected: both Alexes are listed with indexes. Enter `2`: a preview of Alex Tan. Enter `y`: only Alex Tan
   is deleted.
1. `delete /contact 9927 2758`<br>
   Expected: a preview of Bernice Yu. Enter `cancel`: nothing changes.
1. `delete /email lidavid@example.com CHARLOTTE@example.com`<br>
   Expected: a preview of both David Li and Charlotte Oliveiro. Enter `n`.
   `delete /email lidavid@example.com nobody@example.com` is rejected as a whole, with no preview.

### Undoing changes and viewing history

1. `edit 2 p/90000000`, then `y`, then `undo`<br>
   Expected: a preview of the edit to be reversed. Enter `y`: Bernice's previous phone number is restored.
   Enter `undo` again: there is nothing to undo.
1. `delete 3`, then `y`, then `undo`, then `y`<br>
   Expected: Charlotte Oliveiro is restored as the 3rd contact.
1. `add n/Temp Person`, then `undo`<br>
   Expected: there is nothing to undo, since only edits and deletions can be undone.
1. `history`<br>
   Expected: the changes above, newest first, with the undone edit and deletion marked `(undone)`.

### Clearing all entries

1. `find Yu`, then `clear`<br>
   Expected: the prompt counts all six stored contacts, not just the one displayed. Enter `cancel`: every
   contact remains and the `find Yu` results are shown again.
1. `clear`, then `y`<br>
   Expected: all contacts are removed and `0 contact(s) remaining` is reported. Restart Astra: the list is
   still empty, and no sample data is added.
1. `clear` again, and `clear 3`<br>
   Expected: nothing to clear; then an error with the accepted format.

### Logging interactions

1. `log 4 nt/Coffee chat about quant roles`<br>
   Expected: logged with today's date as David Li's 3rd interaction, after his two sample interactions.
1. `log 4 d/1 Jan 2020 nt/Met at an alumni talk`<br>
   Expected: the interaction becomes number 1, since the history is kept in date order.
   `log 4 d/1999-12-31 nt/x`, a future date, and `log 4 nt/coffee chat about QUANT roles` are rejected.
1. `editlog 4 i/1 nt/Met at an alumni dinner`<br>
   Expected: entry 1 shows the new note with the same date. `editlog 4 i/1` and `editlog 4 i/99 nt/x` are
   rejected.
1. `unlog 4 i/1`<br>
   Expected: the entry is shown for confirmation. Enter `y`: it is removed and the history is renumbered.
1. `log David nt/Chat by name`<br>
   Expected: logged for David Li, and the list shows the contacts matching `David`.

### Recording and managing follow-ups

1. `followup 3 a/Send thank-you note d/2020-01-01`<br>
   Expected: Charlotte Oliveiro gets `#2 [PENDING] Send thank-you note | Due: 2020-01-01 [OVERDUE]`.
1. `followup 3 a/Send thank-you note`<br>
   Expected: Astra shows the existing follow-up and asks **Add another?** Enter `n`: nothing is added.
1. `completefollowup 3 i/2`, then `reopenfollowup 3 i/2`<br>
   Expected: `#2` becomes completed without `[OVERDUE]`, then pending and overdue again. Completing an
   already completed follow-up, or reopening a pending one, is rejected.
1. `editfollowup 3 i/2 d/`<br>
   Expected: `#2` shows `No due date`. `editfollowup 3 i/2 a/Ask about the next developer community session`
   is rejected, because it would duplicate pending follow-up `#1`.
1. `deletefollowup 3 i/2`, then `y`, then `followup 3 a/Call Charlotte`<br>
   Expected: `#2` is removed, and the new follow-up gets ID `#3`, never `#2` again.
1. `followup 0 a/x`, `followup 1 a/`, `followup 1 a/x d/2026-02-30`, `followup 1 a/x d/1999-12-31` and
   `followup 1 a/x confirm/yes`<br>
   Expected: an error each time, with no change.

### Reviewing follow-ups

1. `followups`<br>
   Expected: pending follow-ups of all contacts, dated ones first by due date, then undated ones. Alex Yeoh's `#1`
   is marked `[OVERDUE]`.
1. `followups overdue`, `followups today`, `followups upcoming`, `followups undated` and `followups completed`<br>
   Expected: only the matching follow-ups each time, e.g. `undated` lists Bernice Yu's `#2` and David Li's `#2`.
   `followups bogus` is rejected.
1. `followups`, then `2`<br>
   Expected: the contact and follow-up of the 2nd row are shown in the detail panel. Clicking a row or using the
   arrow keys does the same.
1. `followups`, then `list`<br>
   Expected: the follow-up list is closed and `list` runs as a normal command.

### Reordering displayed fields

1. `order`<br>
   Expected: `Current field order: name tags company role phone email linkedin followups introducer interactions`.
1. `view 1`, then `order phone name tags company role email linkedin followups introducer interactions`<br>
   Expected: `Field order updated: phone name tags company role email linkedin followups introducer interactions`,
   and Alex Yeoh's details in the detail panel now start with the phone number. The order of contacts is unchanged.
1. `order name`, `order name name tags company role phone email linkedin followups introducer` and
   `order name tags company role phone email linkedin followups introducer interactions extra`<br>
   Expected: an error listing the accepted fields each time; the order is unchanged.
1. Restart Astra, then enter `order`.<br>
   Expected: the saved order, with `phone` first, is still used.
1. `order name tags company role phone email linkedin followups`<br>
   Expected: accepted, because only `introducer` and `interactions` are missing, and they are added at the end:
   `Field order updated: name tags company role phone email linkedin followups introducer interactions`.

### Creating command shortcuts

1. `alias fn find /notes`, then `fn internship`<br>
   Expected: the result starts with `Ran: find /notes internship` and shows Alex Yeoh.
1. `alias fn list`<br>
   Expected: rejected, showing what `fn` already runs. `alias /replace fn list` then replaces it, `alias`
   lists it, and `alias /remove fn` removes it.
1. `alias list find` and `alias x y`<br>
   Expected: rejected, because `list` is a command and `y` is not.
1. Create a shortcut and restart Astra.<br>
   Expected: the shortcut still works.

### Using the confirmation panel

1. `delete 1`<br>
   Expected: the confirmation panel shows the preview. Press Tab to reach the buttons and choose **Cancel**:
   the panel closes and nothing is deleted.
1. `edit 1 p/90000001`, then click **Confirm**<br>
   Expected: the edit is applied, exactly as if `y` had been typed.
1. Make the window as small as it goes, and run `delete 1` again.<br>
   Expected: the preview wraps and can be scrolled. Enter `n`.

### Saving data

1. Dealing with a missing data file
   1. Close Astra and delete `data/contactfile.json`. Launch Astra.<br>
      Expected: the sample contacts are shown, as on the first launch.
1. Dealing with a partly corrupted data file
   1. Close Astra. In `data/contactfile.json`, change one contact's `"email"` value to `"not-an-email"`.
      Launch Astra.<br>
      Expected: the message area reports how many contacts were recovered and that one record was skipped,
      with the paths of the backup and report. Check that `data/recovery/` contains a folder with
      `contactfile.original.json`, an unchanged copy of the edited file, and `report.txt`, which names the
      skipped record and the reason.
1. Dealing with an unreadable data file
   1. Close Astra and replace the whole content of `data/contactfile.json` with `{`. Launch Astra.<br>
      Expected: **Astra startup failed** is shown with the backup and report paths under `data/reports/`.
      Choose **Exit**: the file is unchanged.
   1. Launch Astra again and choose **Start anew**.<br>
      Expected: Astra opens with an empty contact list, and the backup remains in `data/reports/`.
1. Dealing with a failed save (macOS or Linux)
   1. While Astra is running, make the `data` folder read-only, e.g. `chmod a-w data`. Enter
      `add n/Save Test`.<br>
      Expected: an error that the contact file could not be saved, and Save Test is not in the list. Restore
      the permission with `chmod u+w data`.

--------------------------------------------------------------------------------------------------------------------

<!-- @@author Cookiemunchrr -->

## **Appendix: Planned enhancements**

Team size: 5

1. **Let follow-up descriptions contain words that end in `/`.**
   `followup` treats every word ending in `/` as a parameter prefix, so `followup 1 a/Email HR and/or call`
   and `followup 1 a/Send to Jane c/o HR` are rejected with only "Invalid command format!".
   We plan to treat only `a/` and `d/` as prefixes, so that such words stay part of the description.
   A mistyped prefix at the start of a word, such as `followup 1 x/Call back`, will still be rejected,
   with the message `Unknown prefix x/. followup accepts only a/DESCRIPTION and d/DATE.`

2. **Let users pick from the matches when a name is ambiguous in every command.**
   When a name matches several contacts, `delete`, `drop` and `introduce` list the matches and accept a
   candidate number, but `edit`, `view`, `log`, `editlog` and `unlog` only list them and ask the user to
   enter the whole command again with an index. We plan to make these five commands use the same numbered
   selection, e.g. `view an` will show `2 contacts found. Enter the number of the contact to view, or n to
   cancel.`, and replying `2` will show the second match, without retyping the command.

3. **Accept names that contain a slash.**
   Names such as `Ravi s/o Kumar` and `Siti d/o Ahmad` are common in Singapore, but `add n/Ravi s/o Kumar`
   is rejected because a name may contain only letters, numbers, spaces, hyphens, apostrophes and full stops.
   We plan to also allow `/` inside a name, after its first character, so that such names are accepted as
   typed. Prefixes in the rest of the command will still be recognised only when they start a new word.

4. **Let `order` place events like the other fields.**
   `order` arranges ten fields, but events are not one of them, so they always appear last in a contact's
   details, even when the user wants them near the top. We plan to add `events` as an eleventh field,
   accepted by `order` like the others, with older saved orders that lack it keeping events last.

<!-- @@author -->
