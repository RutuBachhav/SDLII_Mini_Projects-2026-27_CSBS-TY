# Cloth Factory Inventory Management System

Jakarta EE 10 learning project for managing cloth-factory products, suppliers, stock movements, production, and reports.

## Current phase

Phase 1 creates a Java 21-targeted Maven WAR, a MySQL 8.4 Docker Compose service, an initial normalized schema, and sample inventory records. Application features are being added incrementally so each phase can be built and tested independently.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker Engine with Docker Compose
- Apache Tomcat 10.1 for deployment

The current Codespace has Java 25 installed. Maven is configured for Java 21, so install/select JDK 21 before compiling if Maven reports `release version 21 not supported`.

## Start MySQL

Run these commands from the repository root:

```bash
docker compose up -d mysql
docker compose ps
```

The schema and sample data are loaded automatically on the first startup. The Compose credentials are development-only values and must not be reused in production.

## Run the web application

Build the WAR and start the application server with:

```bash
mvn clean package
docker compose up -d
docker compose ps
```

The application is deployed at the Tomcat root context. Open `http://localhost:8080/`
or the forwarded Codespaces port URL. The main routes are `/dashboard`, `/products`,
`/suppliers`, `/inventory`, `/reports`, and `/health`.

## Database connection settings

The JDBC utility reads `DB_URL`, `DB_USER`, and `DB_PASSWORD` from the environment. For local development, it falls back to the credentials in `docker-compose.yml`. Set those variables before running Tomcat when using another MySQL instance:

```bash
export DB_URL='jdbc:mysql://localhost:3306/cloth_factory?useSSL=false&serverTimezone=UTC'
export DB_USER='cloth_app'
export DB_PASSWORD='your-local-password'
```

The `/health` servlet will return `UP` when the application can open and close a JDBC connection.

## Build the WAR

```bash
mvn clean package
```

The deployable file is `target/cloth-factory-inventory.war`. Docker Compose mounts it
as Tomcat's root application. If deploying manually, copy it to Tomcat 10.1's
`webapps/ROOT.war` and open the server root through the forwarded Tomcat port.

## Database design

See [database-design.md](docs/database-design.md) for table purposes and relationships. Database credentials for later Java code should be supplied through environment variables or a local, ignored configuration file, never committed to Git.

## Project stages

1. Environment and database foundation
2. JDBC connection and health check
3. Product management
4. Supplier management
5. Stock-in, stock-out, and production transactions
6. Dashboard and reports
7. Authentication and authorization hardening
8. Tomcat deployment, testing, and academic documentation