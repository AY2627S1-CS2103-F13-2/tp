[![CI Status](https://github.com/AY2627S1-CS2103-F13-2/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103-F13-2/tp/actions)

[![codecov](https://codecov.io/gh/AY2627S1-CS2103-F13-2/tp/graph/badge.svg?token=AH8IY34U2W)](https://codecov.io/gh/AY2627S1-CS2103-F13-2/tp)

![Ui](docs/images/Ui.png)

# Astra

**Astra is a command-line contact book for people who network a lot.** Capture each new person you meet (name, company, role, phone, email and LinkedIn) in the seconds between conversations, then pull them up again before you next meet.

* **Who it is for:** students and professionals who attend networking events, career fairs and conferences, type fast, and are comfortable with a CLI.
* **What it does:**
  * `add` a contact with their name, and optionally a company, role, phone number, email, LinkedIn profile and tags
  * `find` a contact by part of their name, or by phone number or email, and `view` everything stored about them
  * `edit` details that were mistyped or have changed, one field or several at a time
  * `delete` a contact by name or by its number in the list, or `clear` the whole list
  * `list` all contacts, `help` for command formats, and `exit`
  * Refuses to store two contacts under the same name, or to give one contact a phone number or email already saved for another, whether you are adding or editing
  * Saves automatically after every change, and recovers what it can from a corrupted data file

* **Planned for after the MVP:** several companies, roles, numbers or emails per contact; deleting by
  email or phone number; deleting several contacts at once; a confirmation prompt before `delete` and
  `clear`; merging duplicate records rather than only refusing them; and treating names that differ
  only in letter case as the same contact.

* **Getting started:** see the [User Guide](https://ay2627s1-cs2103-f13-2.github.io/tp/UserGuide.html).
* **For developers:** see the [Developer Guide](https://ay2627s1-cs2103-f13-2.github.io/tp/DeveloperGuide.html).

## Acknowledgements

* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education)
