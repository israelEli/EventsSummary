# Event Alerting
Receives a file and creates alerts in database

# Features
Alert creating in hsql database
Stand-alone jar
Integration tests (using JUnit)


# Requirements
Java 8
Gradle 5.1

# How to build the application
Checkout the project from this repository and run:
gradle fatjar

# How to run tests
.\gradlew clean test --info
Result of the test will be located from the root directory of the project: build/test-results/test/TEST***.xml


# How to use the application
Locate the built jar in the build/libs directory
java -jar eventalert-all-0.0.1-SNAPSHOT.jar <path to logfile>
