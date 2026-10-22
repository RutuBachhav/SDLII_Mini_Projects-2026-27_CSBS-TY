# DeadlineBuddy: demo credentials

App URL: http://localhost:8080/DeadlineBuddy/login

## Web app accounts

| User | Email | Password | Data |
|---|---|---|---|
| Ananya Pawar (main demo) | `ananya.pawar@student.edu` | `123456789` | 16 tasks, 5 subjects |
| Rohan Mehta | `rohan.mehta@student.edu` | `rohan1234` | 4 tasks |

Ananya's dashboard tiles read 16 / 6 / 8 / 2 / overdue / due-today (the last two depend on today's date).
Signed in as Rohan, opening one of Ananya's tasks (for example `/tasks/edit?id=3`) returns 404.

## MySQL Workbench connection

| Setting | Value |
|---|---|
| Host | `localhost` |
| Port | `3306` |
| User | `root` |
| Password | your MySQL root password (the same one set in `src/java/deadlinebuddy.properties`) |
| Database | `deadlinebuddy_db` |

## Reset the data after a demo

Run `db/schema.sql`, then `db/seed.sql`, then check with `db/verify.sql` (every row should say OK).
