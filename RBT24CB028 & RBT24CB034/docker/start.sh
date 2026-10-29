#!/bin/sh
# Container entrypoint: wires the MySQL datasource from environment variables,
# deploys CampusCare at the site root and keeps GlassFish in the foreground.
set -e

: "${DB_HOST:?DB_HOST is required}"
: "${DB_USER:?DB_USER is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-campuscare}"
DB_SSL_MODE="${DB_SSL_MODE:-REQUIRED}"
DOMAIN_LOG="$GF_HOME/glassfish/domains/domain1/logs/server.log"

asadmin start-domain domain1

asadmin create-jdbc-connection-pool \
    --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
    --restype javax.sql.DataSource \
    --property "ServerName=${DB_HOST}:PortNumber=${DB_PORT}:DatabaseName=${DB_NAME}:User=${DB_USER}:sslMode=${DB_SSL_MODE}:allowPublicKeyRetrieval=true" \
    CampusCarePool
# Set separately so special characters in the password need no escaping
asadmin set "resources.jdbc-connection-pool.CampusCarePool.property.password=${DB_PASSWORD}"
asadmin create-jdbc-resource --connectionpoolid CampusCarePool jdbc/CampusCareDS

asadmin deploy --force=true --contextroot / --name CampusCare /opt/app/CampusCare.war

echo "CampusCare is up on port 8080"
exec tail -F "$DOMAIN_LOG"
