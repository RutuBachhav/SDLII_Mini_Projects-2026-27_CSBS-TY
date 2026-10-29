# 60-second presentation

“LostLink is a Java desktop system that helps college students recover lost belongings. Instead of searching through scattered messages, we keep lost and found reports in one place.

The dashboard shows lost, found, active and recovered-pair totals. I can report an item with its category, colour, location, date, contact and an optional photo. The browse screen supports search, filters and editing.

Suggested matches use a transparent weighted score: category and name get 25 percent each, colour and location 15 each, and date and description 10 each. This is similarity, not a probability or proof of ownership.

A student submits identifying details in a claim. The operator must manually verify ownership before recording recovery. The claim, reports and recovery history update together in a SQLite transaction, so partial updates cannot leave inconsistent records.

The interface uses Swing, storage uses SQLite JDBC, and Maven makes it easy to run in NetBeans. Data and copied photos survive restarts. Automated tests check matching, validation, persistence and recovery consistency.”

# Ten simple viva questions and answers

1. **What problem does LostLink solve?** It centralizes college lost/found reports, compares compatible items and records verified returns.
2. **Why Swing?** It is Java's standard desktop toolkit and runs without an application server. FlatLaf gives the Swing components a consistent light theme.
3. **Why SQLite?** It stores data in a local file and needs no database server setup. JDBC lets Java access it using SQL.
4. **What is Maven's role?** It manages dependencies, compiles source, runs JUnit tests and packages the executable JAR. NetBeans recognizes `pom.xml` as a project.
5. **What is a prepared statement?** SQL and values are supplied separately. This avoids treating typed item/contact data as executable SQL.
6. **How is similarity calculated?** A fixed weighted sum combines category equality, Jaccard text overlap and a date score that decreases to zero over 30 days. Missing optional text earns zero.
7. **Why is a score not a probability?** The weights are design choices for comparing attributes; they are not calibrated ownership predictions. A human must check evidence.
8. **What does a transaction do during recovery?** It groups the recovery insert, report status updates and claim decisions so they all commit or all roll back.
9. **How are duplicate recoveries stopped?** The service requires a pending claim and active reports. Unique constraints on recovery claim/found/lost IDs provide a second safeguard.
10. **Why use SwingWorker?** Slow database/image work runs on a background thread. Its completion handler safely updates the interface on Swing's event dispatch thread.
