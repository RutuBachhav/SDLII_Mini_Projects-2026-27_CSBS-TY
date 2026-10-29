# Library-Management

An Ant-based Java Web Application for Library Management built for Apache NetBeans, Eclipse GlassFish 7 (Jakarta EE 10), JPA (EclipseLink), MySQL, Servlets, and RESTful web services.

## Overview

This project provides a full-featured Library Management System featuring:
- **Book Management**: Add, view, update, and delete catalog books.
- **Member Management**: Register and manage library patrons.
- **Circulation & Loans**: Issue books to members with a 14-day borrowing period, calculate fines for overdue returns, and handle book returns.
- **RESTful API**: JSON API endpoints for programmatic access.

## Project Structure

```text
LibraryManagement/
├── src/
│   ├── conf/
│   │   ├── MANIFEST.MF
│   │   └── persistence.xml          # JPA Persistence Unit (LibraryPU)
│   └── java/
│       └── com/
│           ├── entity/              # JPA Entities
│           │   ├── Book.java
│           │   ├── Member.java
│           │   └── Loan.java
│           ├── rest/                # REST API (Jakarta RESTful Web Services)
│           │   ├── ApplicationConfig.java
│           │   ├── BookResource.java
│           │   ├── MemberResource.java
│           │   └── LoanResource.java
│           └── web/                 # Servlets (Controller Layer)
│               ├── BookServlet.java
│               ├── BookDeleteServlet.java
│               ├── BookUpdateServlet.java
│               ├── MemberServlet.java
│               ├── MemberDeleteServlet.java
│               ├── MemberUpdateServlet.java
│               ├── LoanIssueServlet.java
│               ├── LoanReturnServlet.java
│               └── LoanDeleteServlet.java
├── web/                             # Web Pages & Views
│   ├── WEB-INF/
│   │   ├── glassfish-web.xml
│   │   └── web.xml
│   ├── index.html                   # Dashboard navigation
│   ├── addBook.html                 # Add book form
│   ├── viewBook.jsp                 # Book directory with search & stats
│   ├── updateBook.jsp               # Update book details
│   ├── deleteBook.jsp               # Delete book record
│   ├── addMember.jsp                # Register member form
│   ├── viewMember.jsp               # Member directory with search & stats
│   ├── updateMember.jsp             # Update member details
│   ├── deleteMember.jsp             # Delete member record
│   ├── issueLoan.jsp                # Issue book to member
│   ├── viewLoan.jsp                 # Loan circulation records
│   ├── returnLoan.jsp               # Process book returns
│   └── deleteLoan.jsp               # Delete loan history
├── lib/
│   └── mysql-connector-j-9.7.0.jar
├── nbproject/                       # NetBeans Ant project configuration
└── build.xml                        # Ant build script
```

## Tech Stack & Requirements

- **JDK**: Version 21
- **Application Server**: Eclipse GlassFish 7.x (Jakarta EE 10)
- **Database**: MySQL 8.x
- **Persistence Provider**: EclipseLink JPA
- **IDE**: Apache NetBeans

## Database connection settings

Set `DB_USER` and `DB_PASSWORD` in the environment used to run GlassFish. Set
`DB_URL` to override the default JDBC URL in `src/conf/persistence.xml`.
`DB_PASSWORD` must be set even when it is empty for a passwordless local database.
