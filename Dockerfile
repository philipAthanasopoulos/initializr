FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

COPY . .

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "cd /app && mvn clean install -DskipTests=true && cd /app/initializr-service-sample && mvn spring-boot:run"]

