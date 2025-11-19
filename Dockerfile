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

# Copy jar from the build stage
COPY --from=build /app/target/quantum-save-0.0.1-SNAPSHOT.jar quantum-save-v1.0.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "quantum-save-v1.0.jar"]