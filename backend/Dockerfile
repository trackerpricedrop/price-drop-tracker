FROM gradle:8.8-jdk21 AS build

WORKDIR /app
COPY . .
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/pricedropproject-1.0-all.jar app.jar
COPY .env .

CMD ["java", "-jar", "app.jar"]
