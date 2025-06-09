FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew shadowJar --no-daemon

CMD ["java", "-jar", "build/libs/pricedropproject-1.0-all.jar"]
