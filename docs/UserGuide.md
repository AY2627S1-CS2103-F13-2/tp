---
layout: page
title: User Guide
---

Astra is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, Astra can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for Astra.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar contactfile.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On first launch, Astra shows sample contacts and a welcome message. Type `clear` and then `y` to remove the sample contacts when you are ready to add your own.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `filter mode/or t/work t/friend` : Lists contacts tagged with work or friend.

   * `add n/John Doe c/Acme r/Engineer p/98765432 e/johnd@example.com` : Adds a contact named `John Doe` to the Contact File.

   * `delete 3`, then `y`: Deletes the 3rd contact shown in the current list.

   * `followup 1 a/Send portfolio`: Records a pending action for the first displayed contact.
   * `followups [overdue|today|upcoming|completed|undated]`: Reviews follow-ups across all contacts by date or status.

   * `order name phone email company role tags linkedin followups interactions`: Changes the field order for every contact.

   * `clear`, then `y`: Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Starting Astra

Always launch Astra from the same home folder so it can find your saved contacts in `data/contactfile.json`.

* **No saved contact file:** Astra shows sample contacts and a welcome message explaining how to clear them.
  The samples include starred contacts, introducers, events, tags, dated interaction notes, and
  pending and completed follow-ups. Try `followups`, `followups today`, `followups upcoming`,
  `followups completed`, or `followups undated` to explore them. Sample dates are based on the
  day the samples are first loaded; once saved, they stay as recorded. Existing saved contacts
  are never replaced by the sample data.
* **Saved contacts load successfully:** The message area shows the number of loaded contacts and the full path to the contact file. A saved empty contact file stays empty.
* **Another Astra instance is using the same contact file:** Startup stops with an error. Close that instance, then launch Astra again.

### Recovering contacts at startup

If the file can be read but contains some invalid or duplicate contacts, Astra automatically keeps the valid contacts and skips the affected records. For duplicate contacts, the first valid record is kept. Recovery proceeds only if at least one valid contact remains.

An invalid saved event entry, such as `null` in a contact's `events` list, causes that entire contact to be skipped and reported. Astra does not recover individual fields or events from the damaged contact.

Before replacing the active file with the recovered contacts, Astra preserves the original file as `contactfile.original.json` and writes a `report.txt` in a separate folder under `data/recovery/`. The startup message shows how many contacts were recovered and skipped, along with the paths to the active file, backup, and report.

Read the report for the positions and reasons for skipped records in the original file. Keep the backup unchanged; use it to look up missing details, correct them, and add those contacts through Astra. Recovery is complete only when Astra displays the successful recovery message.

### When contacts cannot be loaded

If the file is malformed, no valid contacts can be recovered, or recovery cannot finish safely, Astra displays **Astra startup failed** before opening the main window.

When Astra can safely preserve the original contact file, it saves `contactfile.original.json` and `report.txt` in a separate folder under `data/reports/`. The error dialog shows their paths and offers two choices:

* **Start anew:** Replaces the active contact file with an empty one and opens Astra. The startup message confirms the reset and shows the backup and report paths. Keep the backup if you want to restore contact details manually later.
* **Exit:** Closes Astra and leaves the active contact file unchanged. Closing the dialog also exits. You can repair the file or restore a known good backup while Astra is closed, then launch it again.

If Astra cannot safely prepare a reset, **Start anew** is unavailable and the contact file is not cleared. Follow the error message, check that the contact file is readable and its folder is writable, and restart after resolving the problem. If a reset fails, Astra displays an error instead of opening an empty contact file.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`. For `add`, every field except the name is optional.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed contact fields for `add` and `edit` can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `starred`, and `exit`, are ignored.<br>
  For example, `help 123` is interpreted as `help`. `clear` is the exception: it rejects extra parameters instead of ignoring them.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the contact file.

Format: `add n/NAME [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [i/INTRODUCER] [ev/EVENT] [t/TAG]…​`

* Only the contact's name is required. Add whichever details you managed to collect and fill in the rest later with `edit`.
* Use `i/INTRODUCER` to record who introduced the contact. This name is optional and does not need to belong to an existing Astra contact.
* Use `ev/EVENT` to record an event associated with the contact. The event name must not be blank.

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe c/Acme r/Software Engineer p/98765432 e/johnd@example.com l/linkedin.com/in/johndoe`
* `add n/John Doe i/O'Brien-Smith` Adds John Doe and records O'Brien-Smith as the introducer.
* `add n/Betsy Crowe c/Newgate e/betsycrowe@example.com t/careerfair`
* `add n/Hans Gruber` — just a name, when that is all you got before the conversation moved on.
* `add n/Alice Tan ev/NUS Career Fair` Adds Alice Tan and records the NUS Career Fair event.

### Listing all persons: `list`

Shows a list of all persons in the contact file.

Format: `list`

### Starring an important contact: `star`

Marks a contact as important so you can retrieve it later with `starred`.

Format: `star INDEX` or `star QUERY`

* `INDEX` must be a positive integer from the currently displayed list.
* If the contact is already starred, Astra reports that status without changing contact data.
* Starred status is saved automatically and preserved across restarts and edits.

Example: `find Betsy` followed by `star 1` stars the 1st search result.

Both `star QUERY` and `unstar QUERY` accept the same search criteria as [`find`](#locating-persons-find): a name, `/contact PHONE_NUMBER`, or `/email EMAIL`.

* Queries search all contacts and replace the current displayed filter with the search results.
* One match: the contact's starred status is updated. If it already has the requested status, Astra reports this without changing contact data.
* Multiple matches: Astra displays the results without changing any contacts and asks you to retry with `star INDEX` or `unstar INDEX`.
* No matches: Astra displays an empty list and reports `Contact not found.`
* Numeric input is treated as an index. As with other index commands, input beginning with an integer must be a single positive index.

Examples: `star Alex`, `star /contact 87438807`, and `unstar /email alex@example.com`.


### Removing a contact's star: `unstar`

Removes a contact's starred status.

Format: `unstar INDEX` or `unstar QUERY`

* `INDEX` must be a positive integer from the currently displayed list.
* If the contact is not starred, Astra reports that status without changing contact data.
* The change is saved automatically. In the starred list, the contact disappears and the remaining contacts are renumbered.

Example: `starred` followed by `unstar 1` unstars the 1st starred contact.

### Retrieving starred contacts: `starred`

Shows all starred contacts in their existing order, replacing any previous search filter.

Format: `starred`

* If none are starred, the list is empty and Astra reports `There are no starred contacts.`
* Use `list` to return to all contacts.

### Editing a person: `edit`

Edits an existing person in the contact file.

Format: `edit INDEX [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE] [e/EMAIL] [l/LINKEDIN] [ev/EVENT] [t/TAG]…​` or `edit NAME [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE] [e/EMAIL] [l/LINKEDIN] [ev/EVENT] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* Instead of an index, you can give all or part of the person's name, e.g. `edit John Lim p/91234567`. Letter case is ignored.
  * If exactly one person's name contains it, Astra previews an edit for that person.
  * If several do, they are listed with numbers and nothing is edited. Enter the command again with the person's number from that list in place of the name.
* At least one of the optional fields must be provided.
* If every value you enter is the same as the current one, nothing is changed and an error message is shown.
* Valid edits first show the proposed changes and resulting contact in the confirmation panel.
  Enter `y` or choose **Confirm** to apply; `n`/`cancel` or **Cancel** discards them.
  Other replies repeat the prompt. Nothing is saved before confirmation.
* Existing values will be updated to the input values after confirmation.
* A new name that is the same as another person's is rejected when neither of them has an email.
* The new phone number or email must not already belong to another person. Emails are compared without regard to letter case, so `JohnDoe@example.com` and `johndoe@example.com` are the same email.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* Use `ev/EVENT` to add one event to the contact. Existing events are retained. An event the contact already has is rejected, and `ev/` without an event name is not accepted. Events cannot be removed in this version.
* To remove all of a person's tags, enter `t/` without a tag after it.
* To remove a person's company, role, phone, email or LinkedIn, enter its prefix with nothing after it, e.g. `c/`. The name cannot be removed.
* After confirmation, each updated field is listed, e.g. `Updated John Lim's phone to 91234567.`
* After confirmation, the full contact list is shown again.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.
*  `edit Betsy p/91234567` Edits the phone number of the person named Betsy, if she is the only person whose name contains `Betsy`.
*  `edit 3 c/ r/` Removes the company and role of the 3rd person.

### Removing selected details: `drop`

Removes only the explicitly selected detail values from a contact, after confirmation.

Format: `drop INDEX FIELD/VALUE…​ [--confirm]` or `drop NAME FIELD/VALUE…​ [--confirm]`

* `INDEX` refers to the contact's number in the displayed list. You can instead provide all or part of the contact's name; if several contacts match, Astra lists them and makes no changes.
* Select a saved value using its field prefix, such as `p/PHONE`, `e/EMAIL`, `t/TAG`, or `i/INTRODUCER`. Values must match a value currently saved for that contact. The contact's name cannot be removed.
* Astra first displays the proposed removal in the confirmation panel. Reply `y` to apply it,
  or `n`/`cancel` to stop. Until confirmed, the contact file is unchanged and not saved.
  A command submitted initially with `--confirm` still applies immediately for compatibility.
* Repeating `t/TAG` removes only those selected tags; all other tags and contact details are retained.

Examples:
* `drop 1 t/outdated` Shows the selected tag removal for the 1st contact.
* `drop 1 t/outdated --confirm` Removes that tag while keeping the contact's other tags.
* `drop Jane Doe e/jane@example.com` Proposes removing the selected email from Jane Doe.
* `drop 1 i/Old Introducer` Proposes removing the saved introducer from the 1st contact.

### Introducing a person: `introduce`

Records the name of the person who introduced a contact. The introducer does not need to be an existing Astra contact.

Format: `introduce NAME i/INTRODUCER [--confirm]`

* `NAME` can be the full or partial name of the contact. Matching is case-insensitive. If multiple contacts match, Astra lists them and asks you to retry with the displayed index in place of the name.
* Enter the introducer's full name after `i/`. A name is required and follows the same name validation as `add`.
* If an introducer is already recorded, Astra displays that name in the confirmation panel.
  Reply `y` to replace it or `n`/`cancel` to stop. An initial command ending in `--confirm`
  still applies immediately for compatibility.
* Astra displays the updated contact after recording the introducer.

Examples:
* `introduce John Doe i/Jane Smith` Records Jane Smith as the person who introduced John Doe.
* `introduce John i/Jane Smith --confirm` Confirms replacing the introducer for the uniquely matched John Doe.

### Locating persons: `find`

Finds contacts by name, approximate name, phone number, email address, company, role, event, or interaction notes.

Formats:

```text
find NAME
find /fuzzy NAME
find /contact PHONE_NUMBER
find /email EMAIL
find /company COMPANY
find /role ROLE
find /company COMPANY /role ROLE
find /event EVENT
find /notes KEYWORDS
```

Searching by name:

* The search is case-insensitive, so `alex` matches `Alex`.
* Part of a name is enough and does not have to be a whole word: `Yeo` matches `Alex Yeoh`.
* The search text must appear continuously and in the same order.
* Extra spaces do not matter, so `find  alex   yeoh` is read as `find alex yeoh`.

Examples:

* `find Alex Yeoh` returns `Alex Yeoh`.
* `find Yeo` returns `Alex Yeoh`.

Searching approximately by name:

* `find /fuzzy NAME` performs a case-insensitive fuzzy name search.
* Minor spelling differences are accepted.

Example:

* `find /fuzzy Alicee` can match `Alice`.

Searching by phone number or email:

* `find /contact PHONE_NUMBER` matches the complete phone number, ignoring spaces, hyphens, and parentheses. Partial numbers do not match.
* `find /email EMAIL` matches an email exactly, ignoring letter case.
* The phone number or email must be valid, or Astra reports the problem instead of searching.

Examples:

* `find /contact 87438807` returns the person with that exact number.
* `find /email berniceyu@example.com` returns `Bernice Yu`.

Searching by company or role:

* `find /company COMPANY` finds contacts whose company matches the search value.
* `find /role ROLE` finds contacts whose role matches the search value.
* Both selectors can be used together. A contact must match both criteria when both are provided.

Examples:

* `find /company Google`
* `find /role Engineer`
* `find /company Google /role Engineer`

Searching by event:

* `find /event EVENT` finds contacts associated with an event containing the search text.
* Event searches are case-insensitive and accept partial event names.

Example:

* `find /event Career Fair` finds contacts associated with `NUS Career Fair`.

Searching interaction notes:

* `find /notes KEYWORDS` finds contacts with a logged interaction whose note contains any of the keywords.
* Letter case is ignored and partial words are accepted.
* The result lists each matching contact with the date and note of every matching interaction. Notes longer than 60 characters are shortened to an excerpt around the first match.
* Everything after `/notes` is treated as keywords, so `/notes` cannot be combined with other search criteria such as `/company`.
* If no note matches, Astra reports `No matching notes were found.` and leaves the previous list unchanged.

Example:

* `find /notes internship coffee` finds contacts whose notes mention an internship or coffee.

If no contacts match an ordinary search, Astra reports `Contact not found.` and displays an empty result list.

If a name query matches several contacts, Astra displays the matching contacts. Use the displayed index when editing one of them. For example:

```text
edit Alice ev/Tech Conference
edit 1 ev/Tech Conference
```

Use `list` to return to all contacts.

### Filtering contacts by tags: `filter`

Shows contacts matching the tags you request, in their stored order.

Formats:

```text
filter t/TAG [t/TAG]...
filter mode/and t/TAG [t/TAG]...
filter mode/or t/TAG [t/TAG]...
```

| Mode | A contact is included when... | Example |
| --- | --- | --- |
| AND (default) | It has every requested tag. | `filter t/work t/friend` |
| OR | It has at least one requested tag. | `filter mode/or t/work t/friend` |

* Provide at least one tag, using a separate `t/` for each. Use the same
  alphanumeric tag names you assign through `add` and `edit`.
* Tags are case-sensitive: `work` and `Work` are different. Mode values ignore
  case, so `mode/OR` is also accepted. The prefixes `mode/` and `t/` use lower case.
* You may put `mode/` before, between, or after the tags, but specify it only
  once. One mode applies to the whole request; mixed Boolean expressions are
  not supported. Repeated tags have the same effect as requesting them once.
* Every filter searches all stored contacts and replaces the previous search
  or filter. Results keep their stored order; each contact appears once.
* No matches show an empty list with `No contacts match the requested tags.`
  Run another filter or `list` to show contacts again.
* Invalid input, including missing/invalid tags or an empty, unknown, or
  repeated mode, preserves your previous view. Type `filter` to see the format.
* `view INDEX` uses the numbers in the filtered results. `list` restores all
  contacts. Filtering and viewing do not change or save contact data.

Examples:

* `filter t/work t/friend` shows contacts tagged with both `work` and `friend`.
* `filter mode/or t/work t/friend` shows contacts with either or both tags.
* `filter t/work`, then `view 1` shows the full details of the first result.

### Viewing a person's full details: `view`

Shows everything Astra has stored about one person, including details the contact list does not show.

Format: `view INDEX` or `view NAME`

* `INDEX` is a positive integer from the **displayed list**, including filtered results.
* `NAME` searches every stored contact, even if the current filter hides it. Matching ignores letter case,
  trims and collapses whitespace, and accepts a contiguous part of a name, just like `find`, `edit`, and `delete`.
* A unique name match shows the contact's full details and narrows the list to that contact.
* Several matches show a numbered list with names, companies, and emails where present, in stored order.
  Run `view INDEX` using those new numbers to choose one. There is no confirmation prompt.
* A missing contact or invalid index shows an error and keeps the previous displayed list.
* Details the person does not have are omitted. Viewing never changes contacts or saves the contact file.
  Use `list` to show everyone again.
* An argument beginning with an integer is treated as an index. `view 0`, `view -1`, and `view 1 Alice`
  are invalid. Name queries use the same allowed characters as contact names; prefixes such as `n/`
  are not supported.

Examples:
* `list` followed by `view 2` shows the full details of the 2nd stored person.
* `find Yeo` followed by `view 1` shows the full details of the first search result.
* `view alice pauline` or `view Pauline` shows Alice Pauline when she is the only match.
* `view Meier` lists matching Meiers; `view 2` then shows the second match's full details.
* `find Alice` followed by `view Carl Kurz` still finds Carl, because name lookup searches all contacts.

### Logging an interaction: `log`

Records a conversation or meeting with a person, so you can look back at what you discussed before you next meet.

Format: `log INDEX [d/DATE] nt/NOTE` or `log NAME [d/DATE] nt/NOTE`

* Identifies the person by `INDEX` or by all or part of their `NAME`, the same way as `edit`. If several people match the name, they are listed with numbers and nothing is logged; enter the command again with the person's number in place of the name.
* `DATE` is when the interaction happened, in the format `YYYY-MM-DD`. If you leave it out, today's date is used.
* The date cannot be in the future, since the interaction has already happened.
* `NOTE` is what was discussed, and cannot be blank.
* Interactions are kept in date order, so an interaction you log late is placed where it belongs. After logging, the person's full interaction history is shown.
* Logging the same note on the same date twice is rejected, so an accidental repeat does not add a second copy. Letter case is ignored when comparing notes.
* When you give an `INDEX`, the displayed list stays as it was. When you give a `NAME`, the list shows the people matching that name.
* `view` shows a person's interaction history after their other details. Use `order` to move it.

Examples:
* `log John Lim nt/Discussed backend internships` logs today's conversation with John Lim.
* `log 2 d/2026-10-01 nt/Met at the career fair` logs an earlier meeting with the 2nd person in the list.

### Removing a logged interaction: `unlog`

Removes an interaction you logged by mistake, e.g. one with the wrong date or for the wrong person.

Format: `unlog INDEX i/ENTRY` or `unlog NAME i/ENTRY`

* Identifies the person by `INDEX` or by all or part of their `NAME`, the same way as `log`.
* `ENTRY` is the interaction's number in the person's interaction history, as shown by `view` and after `log`. It must be a positive whole number.
* Astra first shows the interaction and asks you to confirm. Enter `y` to remove it, or `n` or `cancel` to keep it.
* After removing it, Astra shows what was removed and the person's remaining history, renumbered.
* When you give an `INDEX`, the displayed list stays as it was, so the same index still points to the same person. When you give a `NAME`, the list shows the people matching that name, the same way as `log`.
  This also applies when removing the last note matching a `find /notes` search. Run the search again
  to refresh its results.
* If the person has fewer interactions than `ENTRY`, nothing is removed.
* To correct an interaction's date or note instead of removing it, use `editlog`.

Examples:
* `unlog John Lim i/2` removes the 2nd interaction in John Lim's history.
* `unlog 1 i/1` removes the earliest interaction with the 1st person in the list.

### Editing a logged interaction: `editlog`

Corrects the date or note of an interaction you have logged, e.g. a typo in the note or the wrong date.

Format: `editlog INDEX i/ENTRY [d/DATE] [nt/NOTE]` or `editlog NAME i/ENTRY [d/DATE] [nt/NOTE]`

* Identifies the person by `INDEX` or by all or part of their `NAME`, the same way as `log`.
* `ENTRY` is the interaction's number in the person's interaction history, as shown by `view` and after `log`. It must be a positive whole number.
* Give at least one of `d/DATE` and `nt/NOTE`. A part you leave out keeps its current value.
* `DATE` and `NOTE` follow the same rules as in `log`: the date uses `YYYY-MM-DD` and cannot be in the future, and the note cannot be blank.
* Interactions stay in date order, so changing the date can move the interaction to a different number. Astra shows its new number and the full history after the edit.
* An edit that leaves the interaction exactly as it was, or that makes it the same as another of the person's interactions (same date and note, ignoring letter case), is rejected and nothing changes.
* When you give an `INDEX`, the displayed list stays as it was, so the same index still points to the same person. When you give a `NAME`, the list shows the people matching that name, the same way as `log`.
  This also applies when an edited note no longer matches a `find /notes` search. Run the search again
  to refresh its results.

Examples:
* `editlog John Lim i/2 nt/Discussed frontend internships` corrects the note of the 2nd interaction in John Lim's history.
* `editlog 1 i/1 d/2026-09-30` moves the earliest interaction with the 1st person in the list to 30 September 2026.

### Deleting a person: `delete`

Previews the selected contacts and asks for confirmation before deleting them.

Formats:

```text
delete INDEX
delete NAME
delete /contact PHONE_NUMBER
delete /email EMAIL [EMAIL]...
```

* `INDEX` is a positive integer referring to the currently displayed list.
* A name can be partial and is matched without regard to letter case. Names,
  phone numbers, and emails search every stored contact, even when filtered.
* If a name matches several contacts, Astra displays numbered candidates. Enter
  the candidate's number by itself. Astra then previews that contact and asks
  for confirmation; selection alone never deletes anything.
* Phone numbers match the complete number, ignoring spaces, hyphens, and
  parentheses. A leading plus remains significant. Emails match complete
  addresses without regard to letter case.
* An email batch is validated and resolved in full before confirmation. One
  malformed or missing address rejects the entire request. Repeated addresses
  select a contact once.
* After reviewing the preview, enter `y` to delete or `n`/`cancel` to stop.
  Invalid replies repeat the prompt. To run another command, cancel first.
* Successful deletion shows all remaining contacts in their stored order and
  reports the deleted details and remaining count. Cancellation restores your
  previous view. A failed save preserves the contact file, contacts, and view.
* If no contacts are stored, Astra reports that there is nothing to delete.

Examples:

* `find Betsy`, then `delete 1`, then `y` deletes the first displayed result.
* `delete John Lim`, then `y` deletes him if exactly one contact matches.
* `delete Meier`, then `2`, then `y` deletes the second numbered candidate.
* `delete /contact +65 (9123)-4567`, then `n` previews and cancels deletion.
* `delete /email alice@example.com bob@example.com`, then `y` deletes both
  contacts in one save if every address resolves successfully.

### Undoing an edit or deletion: `undo`

Reverses the most recent successful edit or deletion in the current Astra session.

Format: `undo [--confirm]`

* Run `undo` to see which edit or deletion will be reversed. Nothing changes until you confirm.
* Run `undo --confirm` to restore the contact information. Astra displays the restored contact and saves the contact file.
* Only edits and deletions can be undone. Adding, clearing, or otherwise changing contact data after an edit or deletion makes that earlier change unavailable to undo.
* If restoring the information would violate contact uniqueness rules, Astra reports the conflict and makes no changes.
* Undo history is kept only for the current session and is cleared when Astra closes.

Examples:
* `edit 1 p/91234567`, followed by `undo`, previews the edit that would be reversed.
* `undo --confirm` restores the previous phone number after the preview.
* `delete John Lim`, followed by `undo` and `undo --confirm`, restores John Lim.

### Viewing edit and deletion history: `history`

Shows the edit and deletion history recorded during the current Astra session, including changes that have been undone.

Format: `history`

* The history is cleared when Astra closes.
* Only edits and deletions are included.
<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Review the deletion preview before entering `y`. Undo is available only for the
most recent edit or deletion and is cleared by any later contact data change.
</div>

### Recording a follow-up: `followup`

Adds a pending action to the contact at the currently displayed index.

Format: `followup INDEX a/DESCRIPTION [d/DATE]`

* `INDEX` must be a positive number from the displayed list, including filtered
  results. The contact stays in the current view after adding an action.
* Provide one non-blank description after `a/`. Outer whitespace is removed;
  letter case and internal spacing are kept. Additional command prefixes such
  as `p/`, repeated `a/`, and `confirm/` are rejected. URLs such as
  `https://example.com/portfolio` can be part of the description.
* Optionally add one `d/DATE` before or after `a/DESCRIPTION`. For example,
  `followup 1 d/2026-10-30 a/Call back` is also accepted. Accepted formats are
  `yyyy-MM-dd` (for example `2026-10-25`), `dd/MM/yyyy` (`25/10/2026`), and
  `d MMM yyyy` (`25 Oct 2026`). Month names use English three-letter abbreviations
  and are case-insensitive. Slash-separated dates use day/month/year.
  Omitting `d/` creates an action with no due date. Empty or repeated `d/`
  parameters and impossible dates are rejected without saving an action;
  date errors show the supported formats.
* A new action is saved immediately with status `PENDING`. The response and
  contact card show the ordered list with stable IDs, for example
  `#1 [PENDING] Send portfolio | Due: 2026-10-25`. `view INDEX` also shows the full history.
  All dates display as `yyyy-MM-dd`, regardless of the input format. Undated
  actions display `No due date`. Past dates are accepted: pending actions due
  before today show `[OVERDUE]`. Actions due today or later and completed
  actions are not overdue. Today follows the computer's local date.
* If an identical description already has a pending action, Astra shows it and
  asks **Add another?** Enter `y` to add another, or `n`/`cancel` to stop.
  Comparison is case-sensitive after trimming: `Call` and `call` are different.
  A different due date still triggers confirmation for the same pending description.
  The prompt shows the existing action and the requested date. Confirming creates
  another action with the requested date and leaves the existing action unchanged.
  Other replies repeat the prompt; cancel before entering another command.
* A completed action with the same description does not require confirmation.
  Completed history is retained. Use `completefollowup` and `reopenfollowup` to
  change an action's status, `editfollowup` to correct an action, and
  `deletefollowup` to remove a mistaken action.
* Action IDs are unique within each contact and are never renumbered or reused after deletion,
  including after restart. Older files infer the greatest allocated ID from their existing history.
* Actions survive restart and ordinary contact edits. A failed save preserves
  the file, previous action history, and current view.

Example:

```text
followup 1 a/Send portfolio d/25 Oct 2026
followup 1 a/Send portfolio d/01/11/2026
```

The first command adds `#1 [PENDING] Send portfolio | Due: 2026-10-25`.
The second asks whether another is wanted; enter `y` to add `#2` due on
`2026-11-01`, or `n` to keep just `#1`. The original
command does not need to be repeated while answering the prompt.

### Reviewing follow-ups: `followups`

Lists follow-ups across all contacts, including contacts hidden by
the current `find` or `filter` results. Your contact filter stays unchanged.
Results appear as selectable rows in the left panel. Selecting a row shows its
contact and selected action in the right detail card. The bottom area shows
command summaries and errors, rather than the full follow-up list or contact details.

Format: `followups [overdue|today|upcoming|completed|undated]`

* Omit the selector to list all pending actions, including undated actions.
* `overdue` includes pending actions due before today; `today` includes those due today.
* `upcoming` includes pending actions due after today, with no upper date limit.
* `completed` includes completed actions with or without due dates.
* `undated` includes pending actions without due dates; completed undated actions are excluded.
* Date-based views exclude completed and undated actions. Selectors ignore letter case.
* Supply at most one selector. Unknown selectors and extra arguments show usage instructions.
* Today uses your computer's local date when the command runs. Each view has its own heading
  and empty-result message.
* Dated actions appear in ascending due-date order, followed by undated actions.
* Equal dates retain contact order, then each contact's stored action order.
  Undated actions retain that same order.
* Each numbered entry shows the contact name, stable action ID, status,
  description, and due date in `yyyy-MM-dd` format or `No due date`.
* Pending actions due before today show `[OVERDUE]`. Actions due today are not overdue.
* Completed actions appear only in the `completed` view. The default view displays
  `No pending follow-ups remain.` when empty. Other views use their own empty-result messages.
* When entries are shown, click a row or use the arrow keys to preview the associated contact.
  You can also enter a displayed number to open the associated contact using
  the same display as `view`. The selected action is retained as context. Use the numbered row, not the action's
  `#ID` or a contact index. The contact filter stays unchanged, even if the
  selected contact is hidden by it.
* Invalid numeric replies keep the list visible and show a short error below it. Non-number input
  leaves selection and runs as a normal command: for example, `list`, `view 1`,
  `help`, or `followup 1 a/Call`. Unknown or malformed commands show their usual
  error and still end selection. Enter `n` or `cancel` to leave without running
  another command. An empty result requires no reply.
* Numbered selection uses the exact order shown in the list. If the selected contact
  or its follow-ups have changed or the contact was deleted, Astra asks you to
  run `followups` again rather than opening stale details.
* Running this command does not change or save contact data. Run it again to
  refresh the results after selecting or cancelling, adding an action, or when
  the day changes. Pending selections are not retained across restart.

Example list contents (when today is 2026-10-21):

```text
Pending follow-ups (3):
1. Alice | #2 [PENDING] Call | Due: 2026-10-19 [OVERDUE]
2. Benson | #1 [PENDING] Send portfolio | Due: 2026-10-25
3. Alice | #3 [PENDING] Check in | No due date
Enter a follow-up number, another command, or n/cancel to stop.
```

Enter `2` to display Benson's `Send portfolio` action and full contact details.
Enter `cancel` instead to leave the list without selecting an action.

### Editing a follow-up: `editfollowup`

Edits an existing follow-up while preserving its ID and pending or completed status.

Format:

`editfollowup CONTACT_INDEX i/FOLLOW_UP_ID [a/DESCRIPTION] [d/DATE]`

* `CONTACT_INDEX` is the contact number in the currently displayed list.
* `FOLLOW_UP_ID` is the ID of the follow-up to edit.
* At least one of `a/DESCRIPTION` or `d/DATE` must be provided.
* The follow-up's ID and status are preserved.
* A pending follow-up cannot be changed to a description that duplicates another
  pending follow-up for the same contact.
* Use `d/` without a value to remove the due date.
* The same date formats supported by `followup` are accepted.

Examples:

```text
editfollowup 1 i/2 a/Call client
editfollowup 1 i/2 d/2026-11-05
editfollowup 1 i/2 a/Call client d/2026-11-05
editfollowup 1 i/2 d/
```

### Completing or reopening a follow-up: `completefollowup`, `reopenfollowup`

Marks a pending follow-up as completed, or reopens a completed follow-up as pending.

Format:

`completefollowup CONTACT_INDEX i/FOLLOW_UP_ID`<br>
`reopenfollowup CONTACT_INDEX i/FOLLOW_UP_ID`

* `CONTACT_INDEX` is the contact number in the currently displayed list.
* `FOLLOW_UP_ID` is the follow-up's stable ID, shown as `#ID` on the contact card,
  in `view`, and in `followups`. It is not the row number of the `followups` list.
* Only the status changes. The ID, description, due date and position in the
  contact's history are kept.
* A completed follow-up moves to `followups completed` and leaves the default,
  `overdue`, `today`, `upcoming` and `undated` views. It is never shown as
  `[OVERDUE]`. Reopening it moves it back to the views that match its due date.
* Completing a follow-up that is already completed, or reopening one that is
  already pending, is rejected and changes nothing.
* A follow-up cannot be reopened while another pending follow-up of the same
  contact has the same description. Complete or edit that follow-up first.
* The change is saved immediately and survives restart.

Examples:

```text
completefollowup 1 i/2
reopenfollowup 1 i/2
```

The first command changes `#2 [PENDING] Call client | Due: 2026-11-05` to
`#2 [COMPLETED] Call client | Due: 2026-11-05`. The second reverts it to pending.

### Deleting a follow-up: `deletefollowup`

Previews one pending or completed action before permanently removing it.

Format: `deletefollowup CONTACT_INDEX i/FOLLOW_UP_ID`

* `CONTACT_INDEX` refers to the currently displayed contact list. `FOLLOW_UP_ID` is
  the stable number after `#` in that contact's history, not a dashboard row number.
  Both values must be positive integers.
* The confirmation panel shows the contact, action ID, description, status, and due date.
  Enter `y` or choose **Confirm** to delete; `n`/`cancel` or **Cancel** keeps the action.
  Invalid replies repeat the prompt and unrelated commands wait until you answer or cancel.
* Only that action is removed. Remaining action order and IDs, contact fields, tags,
  events, introducer, star, and interactions are preserved. The current contact filter remains active.
* IDs are never reused, even after deleting the highest ID or the entire history and restarting.
* Invalid contact indices or action IDs change nothing. A failed save restores the
  original file, contact history, retired IDs, and pre-request view. Start a new request to retry.

Example: `deletefollowup 1 i/9`, then `y`, removes action `#9` from the first displayed contact.

### Using the confirmation panel

Contact edits, contact deletion, clear, follow-up deletion, and duplicate follow-up creation
share one scrollable panel. Existing y/n requests from other commands use it too. Read the preview,
then use the buttons or enter `y`, `n`, or `cancel` in the command box. Tab reaches the buttons;
Enter or Space activates the focused button. Showing the panel does not take focus from the command box.

Invalid replies leave the panel open. Completion, cancellation, or failure hides it.
An old button event cannot confirm a later request. Contact/action selection lists still ask for a
number before showing a final confirmation, where applicable.

### Clearing all entries: `clear`

Previews the total number of stored contacts and asks for confirmation before
clearing them. The count includes contacts hidden by a filter.

Format: `clear`

* Enter `y` to clear every contact, or `n`/`cancel` to stop. Invalid replies
  repeat the prompt. Cancel before running an unrelated command.
* Nothing is cleared until you enter `y`. Success reports how many were cleared
  and that 0 remain. A failed save preserves the file, contacts, and prior view.
* `clear` takes no parameters. `clear 3` is rejected and changes nothing.
* An already empty contact file reports that there is nothing to clear.

Examples:

* `clear`, then `y`, with 12 stored contacts, reports
  `Cleared 12 contact(s). 0 contact(s) remaining.`
* `find Alice`, then `clear`, then `cancel` preserves every contact and restores
  the results of `find Alice`.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Confirmed clearing cannot be undone. Check the count before entering `y`.
</div>

### Reordering contact fields: `order`

Changes the order of information within every contact card and in full details.
The order of the contacts themselves stays the same.

Formats:

```text
order
order FIELD FIELD FIELD FIELD FIELD FIELD FIELD FIELD FIELD
```

* `order` shows the current global field order.
* To change it, supply each of these nine field names **exactly once**:
  `name`, `tags`, `company`, `role`, `phone`, `email`, `linkedin`, `followups`, `interactions`.
  If you leave out only `interactions`, it is placed last. Field names ignore letter case. An unknown, repeated, or missing field is
  rejected with the accepted fields; your existing preference and view remain.
* The default is `name tags company role phone email linkedin followups interactions`,
  matching the original cards with follow-ups and then interaction history last. Interaction history
  appears only in a contact's details (`view`, `log`), not on the contact cards. Contacts without optional
  values omit those rows, even when an absent field appears early in the order.
* A successful change immediately refreshes existing cards. If you are viewing
  a contact's details, they are redisplayed in the new order alongside the
  confirmation. Later `view INDEX` and `view NAME` output use the same order.
* The preference applies to every contact and survives restart. Earlier
  preference files without an order use the default.
* Querying the order or setting the same order performs no save. A changed
  order saves preferences once and does not write contact data. Your current
  search/filter and contact order remain in place.
* If saving fails, Astra reports the error and restores the previous field
  order in preferences, cards, and any displayed contact details. The previous
  preference file remains intact.

Examples:

* `order` shows the active order.
* `order name phone email company role tags linkedin followups interactions` puts phone and
  email near the top while retaining all fields.
* `order interactions name tags company role phone email linkedin followups` shows interaction
  history first in a contact's details.
* `order followups name tags company role phone email linkedin` puts follow-up
  history first on contacts that have it.

### Creating command shortcuts: `alias`

Saves a short word that stands for a command you type often. Entering the shortcut then runs that command, followed by anything you type after the shortcut.

Format: `alias SHORTCUT COMMAND…​`

* `SHORTCUT` must be one word of letters and digits that starts with a letter, e.g. `fe`. Letter case does not matter: `FE` and `fe` are the same shortcut.
* `COMMAND` must start with one of Astra's commands other than `alias` (see the [command summary](#command-summary)), and can include part of its parameters.
* A shortcut cannot have the same name as one of Astra's commands, and cannot stand for another shortcut or for `alias`.
* When you use a shortcut, the command runs exactly as if you had typed it in full, with the same checks and confirmations.
* The result starts with `Ran:` followed by the full command the shortcut stood for, so you can see exactly what ran. Error messages show it too.
* If the shortcut already exists, Astra shows what it currently runs and changes nothing. To replace it, use `alias /replace SHORTCUT COMMAND…​`.
* `alias` on its own lists your shortcuts. `alias /remove SHORTCUT` removes one.
* Shortcuts are saved in `preferences.json` as soon as you create them, so they are still there the next time you start Astra. If saving fails, the shortcut is not created.
* If you edit `preferences.json` by hand, shortcut names are read ignoring letter case, and any shortcut that breaks the rules above is dropped when Astra starts.

Examples:
* `alias fe find /email` saves the shortcut. Entering `fe john.lim@example.com` then runs `find /email john.lim@example.com`, and the result starts with `Ran: find /email john.lim@example.com`.
* `alias fe find /contact` when `fe` already exists reports `Shortcut fe already runs: find /email` and tells you how to replace it.
* `alias /replace fe find /contact` replaces it.
* `alias list find` is rejected, because `list` is already a command.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Astra automatically saves successful contact changes. You do not need to save
manually. Viewing, searching, pending prompts, cancellation, and commands that
change nothing do not rewrite the contact file. Display-field order is saved
separately in `preferences.json`. If saving fails, Astra reports
the error and keeps the previous file, contact data, and displayed view.

### Editing the data file

Contact data is saved automatically as a JSON file `data/contactfile.json` in the folder from which you launch Astra (the home folder when following Quick start). Advanced users can edit this file while Astra is closed.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Back up the file before editing it. On the next launch, Astra may recover valid contacts and skip invalid or duplicate records, or stop with an error and offer **Start anew** if a safe backup can be made. See [Starting Astra](#starting-astra) for the recovery process and backup locations.<br>
Furthermore, certain edits can cause Astra to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous Astra home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [i/INTRODUCER] [ev/EVENT] [t/TAG]…​` <br> e.g., `add n/James Ho c/Acme r/Analyst p/22224444 e/jamesho@example.com ev/Career Fair i/Jane Smith t/colleague`
**Alias** | `alias SHORTCUT COMMAND…​`, `alias /replace SHORTCUT COMMAND…​`, `alias /remove SHORTCUT`, or `alias` (lists shortcuts)<br> e.g., `alias fe find /email`
**Clear** | `clear`, then `y` to confirm or `n`/`cancel` to stop
**Delete** | `delete INDEX`, `delete NAME`, `delete /contact PHONE`, or `delete /email EMAIL [EMAIL]...`<br> Review the preview, then enter `y`, `n`, or `cancel`.
**Drop** | `drop INDEX FIELD/VALUE…​ [--confirm]` or `drop NAME FIELD/VALUE…​ [--confirm]`<br> e.g., `drop 1 t/outdated`, `drop 1 i/Old Introducer --confirm`
**Edit** | `edit INDEX [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [ev/EVENT] [t/TAG]…​` or `edit NAME …​` (same fields), then `y` to confirm or `n`/`cancel` to stop<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`, `edit James Ho p/91234567`, `edit 1 ev/Tech Conference`, `edit 3 c/` (removes the company)
**Introduce** | `introduce NAME i/INTRODUCER [--confirm]`<br> e.g., `introduce John Doe i/Jane Smith`
**Undo** | `undo` to preview the most recent edit or deletion; `undo --confirm` to restore it
**History** | `history`
**Find** | `find NAME`, `find /fuzzy NAME`, `find /contact PHONE_NUMBER`, `find /email EMAIL`, `find /company COMPANY`, `find /role ROLE`, `find /event EVENT`, or `find /notes KEYWORDS`<br> e.g., `find Alex Yeoh`, `find /fuzzy Alicee`, `find /contact 87438807`, `find /email alexyeoh@example.com`, `find /company Google /role Engineer`, `find /event Career Fair`, `find /notes internship`
**Filter** | `filter [mode/and\|mode/or] t/TAG [t/TAG]...`<br> Default AND; e.g., `filter mode/or t/work t/friend`
**Follow-up** | `followup INDEX a/DESCRIPTION [d/DATE]`<br> e.g., `followup 1 a/Send portfolio d/25 Oct 2026`; duplicate pending actions ask `y`/`n`/`cancel`
**Delete follow-up** | `deletefollowup CONTACT_INDEX i/FOLLOW_UP_ID`, then `y` or `n`/`cancel`<br> e.g., `deletefollowup 1 i/2`
**Edit follow-up** | `editfollowup CONTACT_INDEX i/FOLLOW_UP_ID [a/DESCRIPTION] [d/DATE]`<br>
e.g., `editfollowup 1 i/2 a/Call client d/2026-11-05`
**Complete follow-up** | `completefollowup CONTACT_INDEX i/FOLLOW_UP_ID`<br> e.g., `completefollowup 1 i/2`
**Reopen follow-up** | `reopenfollowup CONTACT_INDEX i/FOLLOW_UP_ID`<br> e.g., `reopenfollowup 1 i/2`
**Log** | `log INDEX [d/DATE] nt/NOTE` or `log NAME [d/DATE] nt/NOTE`<br> e.g., `log John Lim d/2026-10-01 nt/Met at the career fair`
**Follow-ups** | `followups [overdue|today|upcoming|completed|undated]`, then a displayed number to view details/contact or `n`/`cancel` to stop
**Unlog** | `unlog INDEX i/ENTRY` or `unlog NAME i/ENTRY`, then `y` to confirm<br> e.g., `unlog John Lim i/2`
**Edit log** | `editlog INDEX i/ENTRY [d/DATE] [nt/NOTE]` or `editlog NAME i/ENTRY [d/DATE] [nt/NOTE]`<br> e.g., `editlog John Lim i/2 nt/Discussed frontend internships`
**List** | `list`
**View** | `view INDEX` or `view NAME`<br> e.g., `view 2`, `view Alice Pauline`
**Order** | `order` to query; `order name phone email company role tags linkedin followups interactions` to change
**Star** | `star INDEX` or `star QUERY`<br> e.g., `star 2`
**Unstar** | `unstar INDEX` or `unstar QUERY`<br> e.g., `unstar 1`
**Starred** | `starred`
**Help** | `help`
