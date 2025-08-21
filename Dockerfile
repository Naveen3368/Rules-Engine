# Build all modules and install to local repo
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -ntp -DskipTests install

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/underwriter-app/target/underwriter-app-0.2.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
