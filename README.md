# Support Ticket System API

A Spring Boot application for managing support tickets.

## Technologies Used

- Java 17
- Spring Boot 3.2.x
- Spring Security with JWT Authentication
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven
- AWS SDK (S3, SES, Parameter Store)
- WebSocket with STOMP
- WebSocket Security

## Features

- User authentication and authorization with JWT
- Role-based access control (User, Support, Admin)
- CRUD operations for support tickets
- Ticket assignment and status tracking
- RESTful API design
- Real-time communication with WebSockets
- File storage with AWS S3
- Email notifications with AWS SES
- Secure configuration with AWS Parameter Store

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL

### Database Setup

1. Create a PostgreSQL database named `support_ticket_db`
2. Update the database configuration in `src/main/resources/application.properties` if needed

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 with context path `/api`.

## API Endpoints

### Authentication

- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signin` - Authenticate a user and get JWT token

### Tickets

- `GET /api/tickets` - Get all tickets (requires authentication)
- `GET /api/tickets/{id}` - Get a specific ticket by ID
- `POST /api/tickets` - Create a new ticket
- `PUT /api/tickets/{id}` - Update a ticket (Support or Admin only)
- `DELETE /api/tickets/{id}` - Delete a ticket (Admin only)
- `GET /api/tickets/my-tickets` - Get tickets created by the authenticated user
- `GET /api/tickets/assigned-to-me` - Get tickets assigned to the authenticated user (Support or Admin only)

### Test Endpoints

- `GET /api/test/all` - Public content
- `GET /api/test/user` - User content (requires User role)
- `GET /api/test/support` - Support content (requires Support role)
- `GET /api/test/admin` - Admin content (requires Admin role)

## Security

The application uses JWT (JSON Web Token) for authentication. To access protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Project Structure

```
src/main/java/com/example/supportticketsystemapi/
├── config/
│   └── DatabaseInitializer.java
├── controller/
│   ├── AuthController.java
│   ├── TestController.java
│   └── TicketController.java
├── entity/
│   ├── ERole.java
│   ├── Role.java
│   ├── Ticket.java
│   ├── TicketPriority.java
│   ├── TicketStatus.java
│   └── User.java
├── payload/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   └── SignupRequest.java
│   └── response/
│       ├── JwtResponse.java
│       └── MessageResponse.java
├── repository/
│   ├── RoleRepository.java
│   ├── TicketRepository.java
│   └── UserRepository.java
├── security/
│   ├── jwt/
│   │   ├── AuthEntryPointJwt.java
│   │   ├── AuthTokenFilter.java
│   │   └── JwtUtils.java
│   ├── services/
│   │   ├── UserDetailsImpl.java
│   │   └── UserDetailsServiceImpl.java
│   └── WebSecurityConfig.java
└── SupportTicketSystemApiApplication.java
```
