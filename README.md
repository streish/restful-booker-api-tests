# API tests restful-booker-api-tests

This is a test project that uses JUnit, Maven, and Docker to run automated API tests for a REST client. The tests are performed against the [Restful Booker API](http://restful-booker.herokuapp.com/apidoc/index.html).

## Technologies Used

- Java 11
- JUnit
- Maven
- Docker

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Docker
   
## Project Structure

The project follows a standard Maven project structure:

- The `src` directory contains the main source code and test code.
- The `src/main/java` directory contains the REST client implementation (`BookingClient.java`), data models and utils.
- The `src/test/java` directory contains the test code with a basic class(`BaseTest.java`).

## Tests

The test classes and methods in `com.hotelbooking.api.contracts` perform various tests for the REST client methods:

- `DeleteBookingTest`: Tests the `deleteBooking` method of the REST client.
- `GetBookingIdsTest`: Tests the `getBookingIds` method of the REST client.
- `PartialUpdateBookingTest`: Tests the `partialUpdateBooking` method of the REST client. There is a test: testPartialUpdateBooking_ResponseXml is disabled because the server returns the wrong content type for XML.

## Building the Project

To build the project and run tests in Docker, follow these steps:

1. Clone the project repository to your local machine.
2. Navigate to the project's root directory.
3. Open a terminal or command prompt.
4. Run the following command to build the Docker image:

   ```bash
   docker build -t booker-junit-tests .
5. Run the following command to run tests inside the Docker container:

   ```bash
    docker run -it booker-junit-tests mvn test

## Reports
- In case you need a report from the Docker, you could run the tests with shared volume for the reports and your local directory:
   ```bash
   docker run -v {$pwd}/target/surefire-reports/:/app/target/surefire-reports/ -it booker-junit-tests /bin/bash mvn test