# College submission verification — 29 October 2026

## Project and scope

The existing Desktop project and NetBeans desktop working copy contain identical Java source/test trees. Both are Java Swing/FlatLaf and SQLite Maven applications; neither has Git initialized or a remote configured. This submission uses the Desktop source tree. The separate LostLinkWeb application is outside this submission. Application source, pom.xml, wrapper configuration and existing tests have not been changed.

## Build and tests

`./mvnw verify` passed with Oracle JDK 26.0.2 on macOS (Java source release 17):

- RestartTest: 2 passed; database, photo, recovery notes and demo marker survive separate JVM processes.
- RepositoryTest: 9 passed.
- MatchingServiceTest: 9 passed.
- ValidationTest: 3 passed.
- ReportSearchTest: 2 passed.
- FeatureWorkflowTest: 9 passed.
- PhotoStoreTest: 4 passed.

Total: 38 tests, zero failures, errors or skips. The shaded executable JAR was generated successfully. Initial execution in the restricted agent environment caused native JVM aborts in image/restart tests; the same unchanged tests passed outside that restriction. No application fix or test suppression was needed. Tests use temporary directories; existing personal application data was not copied or edited. A separate JDK 17 runtime and Windows/Linux GUI execution were not tested. This preparation did not repeat a manual GUI walkthrough.

## Repository inspection

Upstream: https://github.com/RutuBachhav/SDLII_Mini_Projects-2026-27_CSBS-TY

Inspected default branch main at 9bb14a77ce00479e6155dc0b0fd23de774944980. The remote advertises main as its only branch. There is no root README, CONTRIBUTING file, .github submission template or AGENTS.md in the inspected checkout. Existing submissions use separate top-level project/team folders, several including PRNs; folder naming is inconsistent. Therefore `LostLink_RBT24CB056/` is a proposed collision-free folder following that observed pattern, not an explicitly documented naming requirement.

Connected GitHub account: aaryakhorate. Reported permissions: pull=true, push=false. A fork of this same repository and pull request targeting professor main is required unless the professor grants collaborator access. No independent repository, fork, commit, push or pull request has been created during preparation. Other students' files and the upstream root .gitignore remain untouched.

## Exclusions and review

Runtime SQLite databases/photos, generated packages, crash reports, credentials and IDE-private state are ignored. The schema is created from Database.java; Maven resolves the declared dependencies. Machine-specific NETBEANS_DIAGNOSIS.md and outdated VERIFICATION.md are retained in the original project but excluded from the submission copy. README.md and .gitignore were backed up locally before updating. The exact planned file list and Git commands are recorded outside the submission folder in the local .submission review directory.
