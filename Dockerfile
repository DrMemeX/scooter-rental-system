FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

COPY config ./config

COPY common-module ./common-module
COPY user-module ./user-module
COPY fleet-module ./fleet-module
COPY discount-module ./discount-module
COPY rental-module ./rental-module
COPY maintenance-module ./maintenance-module
COPY web-module ./web-module

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/web-module/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

