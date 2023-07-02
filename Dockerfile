# Base image with Java and Maven
FROM maven:3.8.1-jdk-11
WORKDIR /app

# Copy the project files
COPY . .

# Build the project
RUN mvn clean install -DskipTests