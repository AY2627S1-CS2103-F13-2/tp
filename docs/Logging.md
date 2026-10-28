---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Transaction diagnostics

Logic logs successful contact/preference saves and cancellations at INFO. FINE records
command classes and pending-request transitions, plus temporary-file validation and atomic
replacement in Storage. Save/permission failures and unexpected command failures are WARNINGs
with their exception causes, followed by snapshot rollback. Logic and MainWindow diagnostics
use operation/state information rather than copying complete commands or contact previews.
Existing parser/model diagnostics at finer levels can still contain user data.

Assertions check internal invariants: pending requests cannot mutate data or preferences,
rollback restores the data and filtered results, deletes remove the resolved count, and action
allocation/deletion/editing preserve monotonically allocated IDs. Input validation uses normal
exceptions and remains active when assertions are disabled.
