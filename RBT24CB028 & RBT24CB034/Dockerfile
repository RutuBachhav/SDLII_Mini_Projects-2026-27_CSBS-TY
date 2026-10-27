# ---- Stage 1: build the WAR with Maven ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Stage 2: run on GlassFish 7 ----
FROM eclipse-temurin:17-jdk

ENV GLASSFISH_VERSION=7.0.18 \
    GF_HOME=/opt/glassfish7 \
    PATH=/opt/glassfish7/bin:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends curl unzip \
    && curl -fsSL -o /tmp/gf.zip \
       https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip \
    && unzip -q /tmp/gf.zip -d /opt \
    && rm /tmp/gf.zip \
    && curl -fsSL -o ${GF_HOME}/glassfish/domains/domain1/lib/mysql-connector-j-8.3.0.jar \
       https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar \
    && apt-get purge -y unzip && rm -rf /var/lib/apt/lists/*

# Static server config that does not need the database: JMS resources and
# memory limits sized for small (512 MB) cloud instances.
RUN asadmin start-domain domain1 \
    && asadmin create-jms-resource --restype jakarta.jms.QueueConnectionFactory jms/CampusCareConnectionFactory \
    && asadmin create-jms-resource --restype jakarta.jms.Queue --property Name=jms_CampusCareQueue jms/CampusCareQueue \
    && asadmin delete-jvm-options -Xmx512m \
    && asadmin create-jvm-options -Xmx256m:-XX\:MaxMetaspaceSize=192m:-Xss512k:-XX\:+UseSerialGC \
    && asadmin stop-domain domain1

COPY --from=build /app/target/CampusCare.war /opt/app/CampusCare.war
COPY docker/start.sh /opt/app/start.sh
RUN chmod +x /opt/app/start.sh

EXPOSE 8080
CMD ["/opt/app/start.sh"]
