FROM maven:3.8.6-jdk-11 AS backend_base
COPY src /opt/modelfun/src
COPY pom.xml /opt/modelfun/
WORKDIR /opt/modelfun/
RUN mvn clean package \
    && cp target/modelfun.jar /opt/app.jar
FROM openjdk:11.0.15-jdk
COPY --from=backend_base /opt/modelfun/target/modelfun.jar /opt/app.jar
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 8080