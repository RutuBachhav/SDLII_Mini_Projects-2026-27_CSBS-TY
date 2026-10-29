# LostLink — Lost and Found Management System

A local desktop application for reporting lost/found belongings, finding possible matches, and recording manually verified returns.

**Student:** Aarya Khorate · **PRN:** RBT24CB056  
**Course:** Skill Development Lab-II · **Academic year:** 2026–27  
**Guide:** Prof. Rutuja S. Bachhav

## Technologies

- Java 17 source compatibility; Java Swing desktop interface with FlatLaf 3.6.
- SQLite through Xerial sqlite-jdbc 3.49.1.0; no separate database server.
- Maven Wrapper (Maven 3.9.11), JUnit Jupiter 5.12.2 and SLF4J NOP 2.0.17.

This submitted desktop version uses **Swing**, not JavaFX. It does not require GlassFish, MySQL, a web browser or a login server. The separate LostLinkWeb application is not included in this submission.

## Requirements and quick start

Install a JDK 17 or later and have internet access for the initial Maven/dependency downloads. A graphical desktop is required to use the application.

From this folder (containing pom.xml), on macOS/Linux:

```sh
./mvnw clean verify
java --enable-native-access=ALL-UNNAMED -jar target/lostlink-1.0.0.jar
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
java --enable-native-access=ALL-UNNAMED -jar target/lostlink-1.0.0.jar
```

The native-access flag is intended for recent JDKs; omit it on JDK 17 if unsupported. The packaged JAR contains runtime dependencies. Alternatively run `./mvnw exec:java` (Windows: `.\mvnw.cmd exec:java`). The macOS helper `Run LostLink.command` builds the JAR when absent and launches it.

### NetBeans

Choose **File → Open Project** and select this project's folder containing `pom.xml`. Enable Java/Maven support if NetBeans requests it, select an installed JDK 17 or later, then build. Run `com.lostlink.App` or use the Maven `exec:java` goal. On macOS, place the checkout in an IDE-accessible folder such as `~/NetBeansProjects` if Desktop privacy permissions block access.

## Project structure

```text
.mvn/wrapper/                 Maven distribution configuration
mvnw, mvnw.cmd               Maven launchers
pom.xml                      Dependencies, tests and executable JAR packaging
src/main/java/com/lostlink/   UI, validation, matching, repository and SQLite setup
src/test/java/com/lostlink/   Unit, workflow, image and restart tests
docs/                        Project guide, viva notes and current verification
scripts/package-macos.sh     Optional macOS app-image packaging
```

The database schema is created automatically by `Database.java`. Dependencies are resolved from Maven; generated JARs, personal databases and photos are intentionally excluded from Git.

## Use the application

- **Dashboard:** all-time lost/found counts, active report count, verified linked recovery count, and the ten newest reports by creation order.
- **Report an item:** choose lost/found, fill the required fields marked `*`, and use ISO date `YYYY-MM-DD`. Colour, description and photo are optional. Dates must be real and not in the future. Photos are copied to app storage; moving the original does not break them.
- **Browse reports:** literal case-insensitive search plus type, category and status filters. Select a row before View, Edit, Delete or Claim. Clear the search and choose all filters to see everything. Recovery history is protected: recovered reports cannot be edited/deleted, and reports referenced by claims cannot be deleted. Claimed reports retain their original type/category/date.
- **Suggested matches:** all compatible active lost/found pairs, sorted by similarity. Select a pair to inspect both reports or submit a linked claim. There is no minimum score or guarantee; the explanation shows which attributes overlap.
- **Claims & recovery:** review identifying details; reject a pending claim or choose **Verify & recover…**, provide verification notes, and check the manual ownership/return confirmation. Approval and recovery happen together. Other pending claims for that found item or the same linked lost report are rejected automatically. Repeated recovery is blocked. Recovery history includes the verifier's notes and UTC timestamp.
- To claim a found item with no corresponding lost report, use Browse → Claim found item and leave “No linked lost report”. Such a return is stored in history but does not increase **Recovered pairs**.
- Duplicate claims use found report + trimmed, case-insensitive contact. Rejected claims remain in history and cannot be resubmitted with that same contact for that item.

## Persistent data and backups

On macOS: `~/Library/Application Support/LostLink/lostlink.db` and `~/Library/Application Support/LostLink/photos/`.

Storage is independent of the project and `target/`; rebuilding and restarting retain records. Other operating systems use `%LOCALAPPDATA%/LostLink` (Windows) or `~/.local/share/LostLink` (Linux). To back up or move the data, close the application and copy the entire LostLink application-data folder, including photos. Contact and ownership details stay on this machine. There is no login, cloud sync or encryption at rest.

For an isolated demonstration or test database:

```sh
java --enable-native-access=ALL-UNNAMED \
  -Dlostlink.dataDir="$HOME/Library/Application Support/LostLink-Practice" \
  -jar target/lostlink-1.0.0.jar
```

Verification during development uses `LostLink-Verification`, separate from the normal application data. Automated tests use JUnit temporary directories. They do not insert test records into the normal database.

## Tests and documentation

```sh
./mvnw test
```

See [Project guide](docs/PROJECT_GUIDE.md), [presentation and viva](docs/PRESENTATION.md), and [submission verification](docs/SUBMISSION_VERIFICATION.md).

Source entry point: `src/main/java/com/lostlink/App.java`. The Maven package produces `target/lostlink-1.0.0.jar`.

## Verification and limitations

On 29 October 2026, `./mvnw verify` succeeded on macOS with Oracle JDK 26.0.2: **38 tests, zero failures/errors/skips**. The tests use temporary directories, including separate-JVM persistence probes. See docs/SUBMISSION_VERIFICATION.md for scope and environment details. Java 17 compatibility is configured with `--release 17`; a separate JDK 17 runtime was not available for execution testing.

This is a local operator-managed application. There is no authenticated separation between student/staff users, no cloud sync and no encryption at rest. Matching is a weighted similarity estimate, never proof of ownership. Returns require manual verification.

## Optional macOS app image

Run `./scripts/package-macos.sh target/macos-new` with a JDK that includes jpackage. This creates an unsigned app image for the current platform/architecture; it is not a portable Windows/Linux installer. Generated packages stay outside Git.
