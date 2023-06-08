# Base image with Java and Maven
FROM maven:3.8.1-jdk-11 AS build
WORKDIR /app

# Resolve project dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy the project files
COPY . .

# Build the project, run tests, and generate Surefire reports
RUN mvn clean verify

# Set the classpath and execute the tests
CMD ["java", "-jar", "target/*.jar"]