FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

COPY /initializr-service-sample/target/initializr-service-sample-0.22.0-SNAPSHOT.jar jarfile

EXPOSE 8080

ENTRYPOINT ["java","-jar","jarfile"]