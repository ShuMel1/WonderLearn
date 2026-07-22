FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew ./
COPY gradle gradle
COPY settings-server.gradle.kts settings.gradle.kts
COPY shared/src/commonMain/kotlin shared/src/commonMain/kotlin
COPY server server
RUN chmod +x gradlew && ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/server/build/install/server ./
ENV PORT=8080
EXPOSE 8080
CMD ["./bin/server"]
