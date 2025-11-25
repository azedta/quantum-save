############################################
# Stage 1: Build the JAR
############################################
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn -B -DskipTests package


############################################
# Stage 2: Run the application
############################################
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy whatever jar Maven built, rename to app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]