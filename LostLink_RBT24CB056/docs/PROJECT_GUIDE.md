# LostLink project guide

## Problem statement

College students frequently lose personal belongings. Scattered messages and informal desk registers make it hard to compare reports, verify ownership, or track a return. LostLink keeps lost/found reports, explainable suggestions, claims and verified recoveries together on one local desktop.

## Features

The dashboard summarizes activity. Students can report an item with optional photo, search and filter existing reports, inspect and edit details, compare suggested pairs, and submit ownership claims. A desk operator manually verifies ownership and records a return. The app keeps an audit history and offers explicitly requested demo records without replacing real records.

## Architecture

This is a deliberately small layered application in package `com.lostlink`:

- `App`: configures the FlatLaf light theme and starts Swing on the event dispatch thread.
- `MainFrame`, `ReportDialog`: presentation and user interaction. `SwingWorker` runs database queries, image decoding/copying and matching off the event thread; results update Swing on the event thread. Database action buttons are disabled while work is pending. Dialog forms scroll and tables resize with the window.
- `Models`: immutable Java records and enums representing reports, claims and suggested matches.
- `Validation`: required-field, allowed-category, length and incident-date checks. Both UI and repository use validation, so direct repository calls are checked too.
- `MatchingService`: pure, deterministic comparison and unique-pair ranking.
- `ReportSearch`: literal case-insensitive search and combined filters; no regex injection or wildcard semantics.
- `Repository`: prepared SQL statements and business invariants. Recovery, demo loading and claim creation use transactions. One connection per operation; try-with-resources closes statements, result sets and connections.
- `Database`: writable app-data paths, schema initialization, foreign-key enablement and busy timeout on every connection.
- `PhotoStore`: validates image decoding, copies a bounded PNG to persistent storage and creates aspect-preserving previews. Input files above 15 MB are rejected; stored images have a maximum side of 1,600 pixels.

FlatLaf supplies consistent native desktop styling; SQLite JDBC embeds the database. JUnit tests use isolated databases. Maven compiles Java 17 bytecode and shades a self-contained executable JAR. NetBeans uses a checked-in run action.

## Database tables

`reports`: primary key `id`; `type` (LOST/FOUND), name, category, colour, location, incident date, description, reporter, contact, photo filename, `status` (ACTIVE/RECOVERED), creation timestamp. Optional text fields use the empty string, not null. Dates are ISO local calendar dates. The dashboard's recent reports are ordered by ID, so editing does not make a report “new”.

`claims`: primary key `id`; found report foreign key, optional lost report foreign key, claimant, normalized contact, identifying details, status (PENDING/APPROVED/REJECTED), timestamp. `(found_id, contact)` is unique. Linked report types, category and chronology are validated in the repository. A foreign key cannot itself check a report's type; this is a business-layer rule.

`recoveries`: primary key `id`; unique claim foreign key, unique found foreign key, optional unique lost foreign key, required verification notes, timestamp. Uniqueness prevents an item or claim being recovered twice. SQLite permits multiple null lost IDs, allowing separate unlinked returns. Foreign keys use restricted deletion to preserve history.

`settings`: key/value metadata; `demo_loaded` records that the optional sample dataset has already been added.

SQLite foreign keys are enabled on every connection. CHECK constraints restrict enums and some empty values; repository validation handles full field/date rules. All values are bound using prepared statements. SQLite timestamps are UTC, labelled in the interface. Incident dates are local dates without times.

## Matching algorithm

Only compare active LOST reports with active FOUND reports. Categories must be exactly equal, and found date must be on or after lost date. Equal IDs, recovered records, reversed report types and incompatible categories are excluded. Pair keys `lostId:foundId` prevent duplicates. Every eligible pair appears; no arbitrary cutoff hides low-scoring pairs.

Normalize each text field by lowercasing with `Locale.ROOT`, Unicode decomposition, removal of combining marks, replacement of punctuation with spaces, splitting on whitespace and removal of duplicate tokens. Text similarity is **Jaccard similarity**: number of shared tokens divided by number of distinct tokens across both fields. Word order and repeated words do not affect the score. Example: “black phone” versus “phone” scores 1/2. Exact normalized nonempty text scores 1; no shared tokens scores 0.

The score out of 100 is:

```
25 × categoryEquality
+ 25 × Jaccard(name)
+ 15 × Jaccard(colour)
+ 15 × Jaccard(location)
+ 10 × max(0, 1 − daysBetween(lostDate, foundDate) / 30)
+ 10 × Jaccard(description)
```

Category equality is 1 for every eligible pair. Date similarity is 1 on the same date, 0.5 at 15 days and 0 at 30 or more days. Optional missing colour or description scores zero, including when both sides are missing. Weights are **not** redistributed; two otherwise identical reports with both optional fields missing score 75, not 100. Required text is checked before storage. Scores are rounded to one decimal place only for display.

The explanation lists same category, exact or overlapping colours/names/location words, date gap and shared description tokens. Location similarity means shared words; it does not infer geographical distance or “nearby” places. This is transparent weighted similarity, not machine learning, a probability, or evidence of ownership. A low-information pair can still score 25 from category alone.

## Claim and recovery workflow

A claim targets an active found report. A linked lost report is optional, but must be active, have the same category and satisfy chronology. Claimant, contact and identifying details are required. Contact is trimmed and lowercased to prevent simple duplicate submissions. Claims begin PENDING.

Rejection changes a pending claim to REJECTED. Approval is only available through verified recovery. The operator reviews the details, supplies notes and checks the manual verification/return checkbox. The repository rechecks pending claim and active reports inside a transaction, inserts a recovery, marks the found and optional lost reports RECOVERED, rejects other pending claims touching the same returned items, and approves the selected claim. Any SQL/runtime failure rolls back all these changes. An injected database-trigger failure test verifies rollback after intermediate updates.

Reports with claims retain their type, category and incident date so editing cannot invalidate linked evidence. Recovered reports are locked. Reports referenced by claims cannot be deleted, even after rejection. Ordinary active reports can be deleted after confirmation. Lost reports cannot be recovered alone; a found report and verified claim are required.

## Limitations

This is a single-machine college project, with no authentication or permission separation: the operator is trusted to verify ownership. Photos, contact details, notes and the SQLite file are stored locally without application encryption. Backups are manual. The search/ranker loads all reports into memory; matching compares lost/found combinations and is not designed for a huge campus network. Category compatibility is exact; there are no synonyms, fuzzy spelling, maps or image recognition. Names/contact formatting is permissive to support phone numbers and emails; contact ownership is not verified. Manual verification notes identify the method but no authenticated verifier account exists. Selected/replaced/deleted photos can leave unreferenced copies; automatic cleanup is intentionally deferred. There is no undo for deletion or completed recovery. Database schema creation supports this version, with no future migration engine yet.

## Future scope

Add student/operator authentication, verified institutional email, an auditable correction workflow, encrypted backups and export, image cleanup, schema version migrations, item pickup appointments, and tested synonym/location dictionaries. For multi-user deployment, introduce an authenticated server and role-based access rather than sharing a live SQLite file over a network.
