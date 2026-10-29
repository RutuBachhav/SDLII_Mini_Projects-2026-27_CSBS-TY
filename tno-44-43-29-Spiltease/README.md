# SplitEase: Group Expense Splitter & Settlement System

Mini project, Skill Development Lab-II (CSBS, JSPM's RSCOE), A.Y. 2026-27.

SplitEase records shared expenses in a group (trip, flat, club event), splits each bill
**equally, by exact amounts or by percentage**, keeps a running balance for every member and
works out the **fewest payments** that settle everyone (greedy min-cash-flow algorithm).

| Layer | Technology |
|---|---|
| Front end | HTML5, CSS3, jQuery 3.7.1, AngularJS 1.8.3 (ngRoute) |
| Back end | Java 17+, Jakarta EE 10, JAX-RS (Jersey inside GlassFish) |
| Server | Eclipse GlassFish 7.x |
| Database | MySQL 8.x, via JDBC (MySQL Connector/J 8.4) |
| IDE / build | Apache NetBeans, Maven |

## 1. Set up the database
1. Open MySQL Workbench (or `mysql -u root -p`).
2. Run `database/schema.sql`. It creates the database `splitease`, all 6 tables and an app user
   `splitease_app` / `Split@2026`.
3. (Optional) Run `database/sample_data.sql` to load demo data (7 people, 3 groups, 12 bills).
4. If you use other credentials, edit `src/main/resources/db.properties`.

## 2. Open and run in NetBeans
1. **File > Open Project** and select this `SplitEase` folder. NetBeans recognises it as a Maven web app.
2. Add GlassFish if it isn't added yet: **Tools > Servers > Add Server > GlassFish Server**,
   then point it at your GlassFish 7 folder. GlassFish 7 runs on JDK 17 or 21.
3. Right-click the project, open **Properties > Run**, and choose the GlassFish server.
4. Right-click the project and choose **Run**. Maven downloads the two dependencies on the first build
   (an internet connection is needed once), builds `SplitEase.war` and deploys it.
5. The browser opens at **http://localhost:8080/SplitEase/**

## 3. REST API (base URL `/SplitEase/api`)
| Method | Path | Purpose |
|---|---|---|
| GET / POST | `/users` | list people / add a person |
| GET / POST | `/groups` | list groups / create a group with members |
| GET / DELETE | `/groups/{id}` | group details / delete |
| POST | `/groups/{id}/members` | add a member |
| GET / POST | `/groups/{id}/expenses` | list / add an expense |
| DELETE | `/expenses/{id}` | delete an expense |
| GET | `/groups/{id}/balances` | balances + simplified settle-up plan |
| GET / POST | `/groups/{id}/settlements` | list / record a payment |
| GET | `/groups/{id}/export` | CSV summary |
| GET | `/stats` | dashboard totals |

Errors come back as JSON, for example `{"status":400,"error":"Percentages add up to 90% - they must total exactly 100%"}`.

## 4. Package structure
```
com.splitease
 ├─ rest       JAX-RS resources + ApiExceptionMapper + ApplicationConfig (@ApplicationPath "api")
 ├─ service    validation and business rules (User, Group, Expense, Balance, Settlement, Export, Stats)
 ├─ split      SplitStrategy (abstract) -> EqualSplit / ExactSplit / PercentSplit, SplitFactory, Money
 ├─ algo       DebtSimplifier (greedy min-cash-flow with two priority queues)
 ├─ dao        JDBC data access with PreparedStatement + transactions (DBUtil, *DAO)
 ├─ model      User, Group, Expense, Share, Settlement, Balance, Payment, SettlementPlan, DashboardStats
 ├─ dto        request/response bodies (ExpenseRequest, GroupRequest, ParticipantInput, ErrorMessage)
 └─ exception  ApiException (carries the HTTP status)
webapp
 ├─ index.html          AngularJS shell (ng-app, ng-view)
 ├─ views/*.html        dashboard, group, people
 ├─ js/app.js           routes, controllers, services, filters + jQuery helpers
 ├─ css/style.css       responsive CSS3 layout
 └─ js/lib, fonts       jQuery, AngularJS, Inter font (bundled, so no internet needed)
```

## 5. Troubleshooting
* **"Cannot connect to the database"**: MySQL isn't running, or the user/password in `db.properties` is wrong.
* **Public Key Retrieval is not allowed**: this is already handled by `allowPublicKeyRetrieval=true` in the URL.
* **Port 8080 busy**: change the HTTP port of GlassFish in NetBeans (Services > Servers > GlassFish > Properties).
