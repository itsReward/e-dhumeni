FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Install PostGIS JDBC dependencies
RUN apk add --no-cache postgresql-client

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]