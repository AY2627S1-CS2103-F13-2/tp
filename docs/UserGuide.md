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

   * `add n/John Doe c/Acme r/Engineer p/98765432 e/johnd@example.com` : Adds a contact named `John Doe` to the Contact File.

   * `delete 3`, then `y`: Deletes the 3rd contact shown in the current list.

   * `clear`, then `y`: Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Starting Astra

Always launch Astra from the same home folder so it can find your saved contacts in `data/contactfile.json`.

* **No saved contact file:** Astra shows sample contacts and a welcome message explaining how to clear them.
* **Saved contacts load successfully:** The message area shows the number of loaded contacts and the full path to the contact file. A saved empty contact file stays empty.
* **Another Astra instance is using the same contact file:** Startup stops with an error. Close that instance, then launch Astra again.

### Recovering contacts at startup

If the file can be read but contains some invalid or duplicate contacts, Astra automatically keeps the valid contacts and skips the affected records. For duplicate contacts, the first valid record is kept. Recovery proceeds only if at least one valid contact remains.

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

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, and `exit`, are ignored.<br>
  For example, `help 123` is interpreted as `help`. `clear` is the exception: it rejects extra parameters instead of ignoring them.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the contact file.

Format: `add n/NAME [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [t/TAG]…​`

* Only the name is required. Add whichever details you managed to collect and fill in the rest later with `edit`.

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe c/Acme r/Software Engineer p/98765432 e/johnd@example.com l/linkedin.com/in/johndoe`
* `add n/Betsy Crowe c/Newgate e/betsycrowe@example.com t/careerfair`
* `add n/Hans Gruber` — just a name, when that is all you got before the conversation moved on.

### Listing all persons: `list`

Shows a list of all persons in the contact file.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the contact file.

Format: `edit INDEX [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE] [e/EMAIL] [l/LINKEDIN] [t/TAG]…​` or `edit NAME [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE] [e/EMAIL] [l/LINKEDIN] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* Instead of an index, you can give all or part of the person's name, e.g. `edit John Lim p/91234567`. Letter case is ignored.
  * If exactly one person's name contains it, that person is edited.
  * If several do, they are listed with numbers and nothing is edited. Enter the command again with the person's number from that list in place of the name.
* At least one of the optional fields must be provided.
* If every value you enter is the same as the current one, nothing is changed and an error message is shown.
* Existing values will be updated to the input values.
* The new phone number or email must not already belong to another person. Emails are compared without regard to letter case, so `JohnDoe@example.com` and `johndoe@example.com` are the same email.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.
* To remove a person's company, role, phone, email or LinkedIn, enter its prefix with nothing after it, e.g. `c/`. The name cannot be removed.
* After the edit, each updated field is listed, e.g. `Updated John Lim's phone to 91234567.`

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.
*  `edit Betsy p/91234567` Edits the phone number of the person named Betsy, if she is the only person whose name contains `Betsy`.
*  `edit 3 c/ r/` Removes the company and role of the 3rd person.

### Locating persons: `find`

Finds persons by name, phone number or email, and shows the matches in the list.

Format: `find NAME`, `find /contact PHONE_NUMBER`, or `find /email EMAIL`

Searching by name:

* The search is case-insensitive, so `alex` matches `Alex`.
* Part of a name is enough, and it does not have to be a whole word: `Yeo` matches `Alex Yeoh`.
* What you type must appear in the name **in one piece, in order**. `Alex Yeoh` matches `Alex Yeoh`, but `Yeoh Alex` does not, and neither does `Alex Li`.
* Extra spaces do not matter, so `find  alex   yeoh` is read as `find alex yeoh`.
* Only names are searched. To search a phone number or an email, use the forms below.

Searching by phone number or email:

* `find /contact PHONE_NUMBER` matches the complete phone number, ignoring spaces, hyphens, and parentheses; partial numbers do not match. A leading plus remains significant.
* `find /email EMAIL` matches an email exactly, ignoring letter case, so `ALEX@example.com` finds `alex@example.com`.
* The value has to be a valid phone number or email, or Astra reports the problem instead of searching.

If nothing matches, Astra reports `Contact not found.` and the list is left as it was.

Examples:
* `find Alex Yeoh` returns `Alex Yeoh`.
* `find Yeo` returns `Alex Yeoh`, since part of a name is enough.
* `find /contact 87438807` returns the person with that exact number.
* `find /email berniceyu@example.com` returns `Bernice Yu`.

<div markdown="span" class="alert alert-info">:information_source: **Moved to after the MVP:**
searching several keywords at once (`find alex david` returning both `Alex Yeoh` and `David Li`),
and matching keywords in any order. For now a search is one name, one number or one email, matched
in one piece. Searching by company, role or tag is also planned for a later version.
</div>

### Viewing a person's full details: `view`

Shows everything Astra has stored about one person, including details the contact list does not show.

Format: `view INDEX`

* Shows the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list, and **must be a positive integer** 1, 2, 3, …​
* Details the person does not have are left out rather than shown as empty.

Examples:
* `list` followed by `view 2` shows the full details of the 2nd person in the contact file.
* `find Yeo` followed by `view 1` shows the full details of the first search result.

<div markdown="span" class="alert alert-info">:information_source: **Moved to after the MVP:**
naming the person instead of giving an index, as `delete` and `edit` allow.
</div>

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

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Confirmed deletion cannot be undone. Check the preview before entering `y`.
</div>

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

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Astra automatically saves successful contact changes. You do not need to save
manually. Viewing, searching, pending prompts, cancellation, and commands that
change nothing do not rewrite the contact file. If saving fails, Astra reports
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
**Add** | `add n/NAME [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [t/TAG]…​` <br> e.g., `add n/James Ho c/Acme r/Analyst p/22224444 e/jamesho@example.com t/colleague`
**Clear** | `clear`, then `y` to confirm or `n`/`cancel` to stop
**Delete** | `delete INDEX`, `delete NAME`, `delete /contact PHONE`, or `delete /email EMAIL [EMAIL]...`<br> Review the preview, then enter `y`, `n`, or `cancel`.
**Edit** | `edit INDEX [n/NAME] [c/COMPANY] [r/ROLE] [p/PHONE_NUMBER] [e/EMAIL] [l/LINKEDIN] [t/TAG]…​` or `edit NAME …​` (same fields)<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`, `edit James Ho p/91234567`, `edit 3 c/` (removes the company)
**Find** | `find NAME`, `find /contact PHONE_NUMBER`, or `find /email EMAIL`<br> e.g., `find Alex Yeoh`, `find /contact 87438807`, `find /email alexyeoh@example.com`
**List** | `list`
**View** | `view INDEX`<br> e.g., `view 2`
**Help** | `help`
