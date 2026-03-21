# ==========================================
# STEP 1: BUILD ENVIRONMENT (Using Maven + Java 21)
# ==========================================
# Use the official Maven image included with Java 21 (Eclipse Temurin)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# OPTIMIZATION: Copy pom.xml first and load the libraries (Dependencies)
# This helps Docker "remember" (cache) the libraries. The next time you modify the Java code,
# Docker will not have to reload these libraries from scratch, saving a lot of time.

COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the entire source code directory (src) and begin packaging.
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STEP 2: RUNNING ENVIRONMENT (Using the lightweight JRE 21)
# ==========================================

# Use only the Alpine version of JRE (Java Runtime Environment) (ultralight, only about ~50MB)
# instead of using the heavy JDK to save RAM for the server.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Get the specific JAR file created in Step 1 to Step 2.
COPY --from=build /app/target/expensetracker-0.0.1-SNAPSHOT.jar app.jar

# Open port 8080 for Backend
EXPOSE 8080

# Command to start the application when the Container runs
ENTRYPOINT ["java", "-jar", "app.jar"]