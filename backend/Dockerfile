FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
# Copy root parent POM so the backend module can resolve it locally
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
RUN apk add --no-cache maven && mvn -B -pl backend -am package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/backend/target/*.jar app.jar
VOLUME /uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
