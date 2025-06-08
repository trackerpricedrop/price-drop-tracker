# backend/Dockerfile

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew build --no-daemon

CMD ["java", "-jar", "build/libs/pricedropproject-1.0-all.jar"]
