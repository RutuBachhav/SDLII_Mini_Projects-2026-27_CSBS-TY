# Build the WAR, then run it on Tomcat 9 (javax.servlet compatible).
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/hospital-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/hospital.war
EXPOSE 8080
# Bind Tomcat to Render's $PORT (defaults to 8080 locally).
CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"'${PORT:-8080}'\"/' /usr/local/tomcat/conf/server.xml && catalina.sh run"]
