FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/quantum-save-0.0.1-SNAPSHOT.jar quantum-save-v1.0.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "quantum-save-v1.0.jar"]