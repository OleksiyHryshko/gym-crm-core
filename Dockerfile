FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/gym-crm-core-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/initial_data.csv /app/initial_data.csv

ENV SPRING_PROFILES_ACTIVE=standalone
ENV DATA_FILE_PATH=/app/initial_data.csv

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]