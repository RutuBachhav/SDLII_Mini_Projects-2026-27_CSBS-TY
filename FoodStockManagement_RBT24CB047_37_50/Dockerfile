FROM eclipse-temurin:25-jdk

RUN apt-get update && \
    apt-get install -y wget unzip && \
    rm -rf /var/lib/apt/lists/*

RUN wget -q https://download.eclipse.org/ee4j/glassfish/glassfish-7.0.25.zip \
    -O /tmp/glassfish.zip && \
    unzip -q /tmp/glassfish.zip -d /opt && \
    rm /tmp/glassfish.zip

ENV GLASSFISH_HOME=/opt/glassfish7/glassfish
ENV PATH="${GLASSFISH_HOME}/bin:${PATH}"

COPY dist/FoodStockManagement.war ${GLASSFISH_HOME}/domains/domain1/autodeploy/

EXPOSE 8080

CMD ["asadmin", "start-domain", "--verbose", "domain1"]