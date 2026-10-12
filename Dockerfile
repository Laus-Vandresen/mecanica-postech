# Requer o jar gerado antes: ./mvnw package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/*.jar app.jar
COPY certs certs

ENV SPRING_PROFILES_ACTIVE=aws \
    DB_SSL_ROOT_CERT=/app/certs/us-east-1-bundle.pem

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
