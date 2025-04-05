# Support Ticket System Configuration Guide

This document explains how to configure the Support Ticket System application for different environments.

## Configuration Profiles

The application uses Spring profiles to manage different environments:

- `dev`: Development environment (default)
- `prod`: Production environment

## How to Run with Different Profiles

### Development Profile

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using Java
java -jar support-ticket-system.jar --spring.profiles.active=dev
```

### Production Profile

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Using Java
java -jar support-ticket-system.jar --spring.profiles.active=prod
```

## Environment Variables for Production

When running in production mode, you need to set the following environment variables:

### Database Configuration
- `DB_HOST`: PostgreSQL database host
- `DB_PORT`: PostgreSQL database port
- `DB_NAME`: PostgreSQL database name
- `DB_USERNAME`: PostgreSQL database username
- `DB_PASSWORD`: PostgreSQL database password

### JWT Configuration
- `JWT_SECRET`: Secret key for JWT token generation and validation
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds (default: 86400000 - 24 hours)

### CORS Configuration
- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins for CORS

### Server Configuration
- `PORT`: Server port (default: 8080)

## Example Production Deployment

```bash
# Set environment variables
export DB_HOST=production-db.example.com
export DB_PORT=5432
export DB_NAME=support_ticket_prod
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your_secure_jwt_secret_key
export JWT_EXPIRATION=86400000
export CORS_ALLOWED_ORIGINS=https://your-frontend-app.com
export PORT=8080

# Run the application
java -jar support-ticket-system.jar --spring.profiles.active=prod
```

## Local Development Setup

For local development, the application is pre-configured to connect to a PostgreSQL database with the following settings:

- URL: `jdbc:postgresql://localhost:5432/support_ticket_db`
- Username: `postgres`
- Password: `postgres`

Make sure to create the database before running the application:

```sql
CREATE DATABASE support_ticket_db;
```
