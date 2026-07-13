FROM gradle:8.14-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle --no-daemon :app:bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
