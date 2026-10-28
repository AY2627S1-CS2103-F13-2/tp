---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

## Yaozhu's Monday implementation and Wednesday review

Reviewed on 26 October 2026 against local `master` at `5ffd1d0a`. This review follows
`seedu-java-coding-standard`, `seedu-git-standard`, and the Astra delivery skill.
The Monday work adds confirmed follow-up deletion with persisted retired IDs and a
shared confirmation panel, including confirmation before every contact edit.

The author-history scan covers 65 commits reachable from local refs under
Cookiemunchrr / Song yaozhu, including merge and stash metadata. Of these, 61 are
ancestors of the current checkout: 28 non-merge commits and 33 merges. Feature
verification examines the current integrated implementations rather than replaying
obsolete patches. Branch-only `29b29616` (earlier named delete) and `22f709df`
(legacy remark) are superseded/not current features; `ddcf2be8` and `397ac32d`
are stash snapshots, not separate delivered features.

### Current feature audit

| Area | Reviewed behavior and evidence |
| --- | --- |
| Model and optional fields | Required name, optional contact values, phone/email matching, equality/hash, immutable histories; model, matcher, add/parser, and sample-data tests |
| Transactional saving | Frozen contact snapshot, validated temporary file, atomic replacement, rollback of model/preferences/filter and stable observable bindings; AtomicContactSaveTest, FieldOrderPreferenceStorageTest, TransactionalCommandTest |
| Delete and clear | All contact selectors, email batches, selection before confirmation, cancellation, survivor order, full-count clear, one save, failed-save restoration; PendingWorkflowTest and command/parser tests |
| Follow-up creation | Pending duplicates, exact descriptions, dates, stable per-contact IDs, JSON/recovery compatibility, ordinary edit preservation; FollowUpWorkflowTest, FollowUpStorageTest |
| Follow-up deletion | Pending/completed actions, sparse IDs, filtered index, resolved target, metadata/order preservation, no ID reuse after deleting highest/all actions and restart, failed-save rollback and stale replies; DeleteFollowUpWorkflowTest, FollowUpIdStorageTest |
| Tag filters and named viewing | AND/OR/default, dynamic filter restoration, displayed indices, ambiguous name retry and zero writes; TagFilterWorkflowTest, TransactionalCommandTest, ViewCommandTest |
| Field ordering | Preference validation/migration, no-op saving, immediate card/detail refresh and failed preference rollback; FieldOrderWorkflowTest, FieldOrderUiTest, FieldOrderStartupTest |
| Confirmed edits and panel | Every valid contact edit previews first; typed/buttons use one logic path; invalid replies block other commands, cancel restores the view, stale targets/events cannot replay; EditConfirmationWorkflowTest, ConfirmationPanelUiTest |
| Branding, version and assertion launcher setup | Current application branding/resources and v1.4 version checked; Gradle enables assertions for JavaExec and tests. No release version bump requested |
| Documentation and older metadata commits | User/Developer Guides reconciled for this work; historical diagram/profile/README-only changes introduce no executable paths requiring assertions |

The audit corrected misplaced/repeated plus signs being accepted in phone numbers,
stale contact-identity documentation, and test-builder copies losing event/interaction metadata.
New assertions cover pending-request immutability, rollback results, deletion counts, and
allocated IDs. INFO logs cover saves/cancellations; FINE logs trace operations and atomic storage;
WARNING logs retain I/O/runtime causes before rollback. Invalid input uses exceptions, not assertions.

### Commit-message audit

Subjects and body lines were measured mechanically. All 28 current non-merge subjects
are within the 72-character hard limit. Fourteen older commits have body lines of
73–75 characters; their exact offending line numbers/lengths are recorded below.
Subjects longer than the 50-character target are retained where within the hard limit.
The three short documentation/profile commits without bodies are recorded as such.
The initial audit preserved shared history. Publication follows in three v1.5 PRs for
follow-up deletion, shared confirmations, and diagnostics; no old commits are reworded.

| Commit | Subject | Subject length | Body lines over 72 (line:length) |
| --- | --- | ---: | --- |
| `5fb4c770` | Bump the reported version to v1.4 | 33 | None |
| `8beb661e` | Enable assertions when Gradle runs the app and the tests | 56 | None |
| `bb9fb519` | Add contact viewing by name | 27 | None |
| `e6fe7945` | Persist and apply contact field order | 37 | None |
| `f3e584d8` | Add AND and OR tag filtering | 28 | None |
| `a2e14427` | Add persistent contact follow-up actions | 40 | None |
| `fc53830f` | Complete confirmed delete selectors and batches | 47 | None |
| `be609869` | Confirm clear requests before deleting contacts | 47 | None |
| `8f4757d1` | Make contact saves transactional | 32 | None |
| `42064c60` | Fix model contact key comparisons | 33 | None |
| `847eed95` | Restore optional contact fields on add | 38 | 7:73, 11:73 |
| `88982a70` | Replace the AB3 leftovers that are visible to users | 51 | 5:73 |
| `d18f460f` | Sync the README and UG with what landed late in v1.3 | 52 | 3:73 |
| `205a9c52` | Make the README and DG describe the MVP we actually ship | 56 | 13:73, 16:73 |
| `3e72107d` | Correct the User Guide's find section and document view | 55 | 23:73, 25:74 |
| `121917ec` | Delete a contact by name | 24 | 5:73 |
| `15b61208` | Fix master: list the new contact fields on edit | 47 | 3:74, 4:74, 5:73, 9:73, 10:74 |
| `1c1625c4` | Merge branch 'master' into rework-person-model | 46 | 3:74, 4:74, 7:74, 8:74, 9:75 |
| `d3ce2267` | Cover hashCode and the sample data | 34 | 8:74, 10:74, 13:75 |
| `98815425` | Regenerate model class diagrams | 31 | 4:74 |
| `d43b4b9a` | Hold contact fields to the rules the DG specifies | 49 | 8:73, 9:75, 14:75, 18:75, 19:75, 26:74 |
| `03d97620` | Rework Person model for Astra's contact fields | 46 | 4:73, 7:75, 8:74, 9:75, 11:73, 14:74 |
| `f71f3c34` | Align README and DG with AB3 command syntax | 43 | 3:73, 6:75 |
| `0718bac9` | Harden clear command against extra arguments | 44 | 13:73 |
| `24857a9c` | Add delete and clear requirements to DG | 39 | No body |
| `10383dbe` | Update README with Astra features | 33 | No body |
| `162d4529` | AboutUs.md: Add Yao Zhu's details | 33 | None |
| `af281765` | Add profile photo for cookiemunchrr | 35 | No body |

### Verification and remaining manual testing

`./gradlew check` passes on Java 25.0.3: 1,079 tests, zero failures, errors, or skips,
and both Checkstyle tasks. This adds 31 regression tests to the 1,048-test baseline.
The real JavaFX window was rendered with sample data at 900×800 and 450×600; previews,
wrapping, scrolling, buttons, and the remaining contact list were inspected.
The disposable Monocle renderer logged `BufferOverflowException` during stage resizing,
although both correctly sized captures were produced and inspected. The normal test suite
passed without that error. Native resizing and keyboard interaction remain explicit Wednesday
manual cases; headless rendering does not replace those checks.

Use the Wednesday 28 October checklist in the Developer Guide for the team test session.
New syntax: `deletefollowup CONTACT_INDEX i/FOLLOW_UP_ID`, then `y` or `n`/`cancel`.
Contact edits now also require a reply after their preview. Follow-up deletion is permanent
and is not added to the existing contact-undo feature. Old files infer their initial allocated
ID from surviving history; IDs deleted before this field ever existed cannot be reconstructed.
