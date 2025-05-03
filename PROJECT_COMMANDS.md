# Plan B Support Ticket System - Essential Commands

This document provides a comprehensive list of commands for working with the Plan B Support Ticket System project.

## Table of Contents
- [Build Commands](#build-commands)
- [Run Commands](#run-commands)
- [Database Commands](#database-commands)
- [Testing Commands](#testing-commands)
- [Docker Commands](#docker-commands)
- [Git Commands](#git-commands)
- [Deployment Commands](#deployment-commands)
- [Troubleshooting](#troubleshooting)

## Build Commands

### Clean and Compile the Project
```bash
mvn clean compile
```

### Package the Application
```bash
mvn clean package
```

### Install the Application to Local Maven Repository
```bash
mvn clean install
```

### Skip Tests During Build
```bash
mvn clean package -DskipTests
```

### Build with a Specific Profile
```bash
mvn clean package -P<profile-name>
```

## Run Commands

### Run the Application with Maven
```bash
mvn spring-boot:run
```

### Run with a Specific Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=<profile-name>
```

### Run with Debug Mode
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
```

### Run the Packaged JAR
```bash
java -jar target/support-ticket-system-api-0.0.1-SNAPSHOT.jar
```

### Run with Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=<profile-name>
export DATABASE_URL=<database-url>
export DATABASE_USERNAME=<username>
export DATABASE_PASSWORD=<password>
mvn spring-boot:run
```

## Database Commands

### Run Database Migrations
```bash
mvn flyway:migrate
```

### Clean Database and Re-run Migrations
```bash
mvn flyway:clean flyway:migrate
```

### Generate Database Schema
```bash
mvn hibernate:schema
```

### Connect to PostgreSQL Database
```bash
psql -h <host> -U <username> -d <database-name>
```

### Create Database Backup
```bash
pg_dump -h <host> -U <username> -d <database-name> -F c -b -v -f backup.dump
```

### Restore Database from Backup
```bash
pg_restore -h <host> -U <username> -d <database-name> -v backup.dump
```

## Testing Commands

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=<TestClassName>
```

### Run Specific Test Method
```bash
mvn test -Dtest=<TestClassName>#<methodName>
```

### Run Integration Tests
```bash
mvn verify -P integration-test
```

### Generate Test Coverage Report
```bash
mvn jacoco:report
```

## Docker Commands

### Build Docker Image
```bash
docker build -t support-ticket-system:latest .
```

### Run Docker Container
```bash
docker run -p 8080:8080 support-ticket-system:latest
```

### Run with Docker Compose
```bash
docker-compose up
```

### Stop Docker Compose Services
```bash
docker-compose down
```

### View Docker Logs
```bash
docker logs <container-id>
```

## Git Commands

### Clone the Repository
```bash
git clone <repository-url>
```

### Create a New Branch
```bash
git checkout -b feature/new-feature
```

### Commit Changes
```bash
git add .
git commit -m "Description of changes"
```

### Push Changes
```bash
git push origin <branch-name>
```

### Pull Latest Changes
```bash
git pull origin <branch-name>
```

### Merge Branch
```bash
git checkout main
git merge <branch-name>
```

## Deployment Commands

### Deploy to AWS
```bash
aws elasticbeanstalk create-application-version --application-name <app-name> --version-label <version> --source-bundle S3Bucket=<bucket>,S3Key=<key>
aws elasticbeanstalk update-environment --environment-name <env-name> --version-label <version>
```

### Deploy to Google Cloud
```bash
gcloud app deploy
```

### Deploy to Azure
```bash
az webapp deploy --resource-group <resource-group> --name <app-name> --src-path <path-to-jar>
```

## Troubleshooting

### View Application Logs
```bash
tail -f logs/application.log
```

### Check Spring Boot Actuator Health
```bash
curl http://localhost:8080/actuator/health
```

### View JVM Memory Usage
```bash
jmap -heap <pid>
```

### Analyze Thread Dumps
```bash
jstack <pid> > thread-dump.txt
```

### Check Database Connection
```bash
curl http://localhost:8080/actuator/health/db
```

### Common Issues and Solutions

#### Application Won't Start
- Check if the port is already in use: `lsof -i :8080`
- Verify database connection settings
- Check for conflicting bean definitions
- Ensure all required environment variables are set

#### Database Connection Issues
- Verify database credentials
- Check if database server is running
- Ensure firewall allows connections
- Check connection string format

#### Out of Memory Errors
- Increase heap size: `java -Xmx1g -jar application.jar`
- Analyze memory usage with VisualVM or JConsole
- Check for memory leaks

#### Slow Performance
- Enable debug logging for slow components
- Check database query performance
- Monitor CPU and memory usage
- Consider profiling the application

## Project-Specific Commands

### Generate API Documentation
```bash
mvn springdoc:generate
```

### Run Security Scan
```bash
mvn dependency-check:check
```

### Update Dependencies
```bash
mvn versions:display-dependency-updates
mvn versions:use-latest-versions
```

### Check for Outdated Dependencies
```bash
mvn versions:display-dependency-updates
```

### Format Code
```bash
mvn spotless:apply
```

### Check Code Style
```bash
mvn checkstyle:check
```
