# Security Configuration Guide

This document explains the security configuration in the Support Ticket System.

## Overview

The application uses Spring Security with Firebase Authentication to secure both REST endpoints and WebSocket connections. The security configuration includes:

- Firebase authentication
- Role-based authorization
- WebSocket security
- CORS configuration

## Firebase Authentication

Firebase Authentication is used to authenticate users. The application verifies Firebase ID tokens and extracts user information and roles.

### Configuration

Firebase authentication is configured in `application.yml`:

```yaml
# Firebase Configuration
firebase:
  enabled: ${FIREBASE_ENABLED:true}
  credentials:
    path: ${FIREBASE_CREDENTIALS_PATH:firebase-service-account.json}
  database:
    url: ${FIREBASE_DATABASE_URL:}
```

- `enabled`: Whether Firebase authentication is enabled
- `credentials.path`: Path to the Firebase service account JSON file
- `database.url`: Firebase Realtime Database URL (optional)

### Firebase Service Account

To use Firebase Authentication, you need to create a Firebase project and download a service account JSON file:

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Go to Project Settings > Service accounts
4. Click "Generate new private key"
5. Save the JSON file to `src/main/resources/firebase-service-account.json`

### Custom Claims for Roles

Firebase Authentication supports custom claims, which are used to store user roles. To add roles to a user:

```javascript
// Using Firebase Admin SDK
admin.auth().setCustomUserClaims(uid, { roles: ['ADMIN', 'SUPPORT'] });
```

The application will extract these roles and convert them to Spring Security authorities.

## Security Components

### FirebaseUserDetails

Adapts Firebase user data to Spring Security's `UserDetails` interface. It extracts user information and roles from Firebase token claims.

### FirebaseAuthenticationToken

Custom authentication token for Firebase authentication. It extends Spring Security's `AbstractAuthenticationToken` to integrate Firebase auth.

### FirebaseTokenValidator

Validates Firebase ID tokens and extracts user information. It includes a token cache to improve performance.

### FirebaseAuthenticationFilter

Filter to authenticate requests with Firebase tokens. It extracts and validates Firebase ID tokens from HTTP requests.

## Security Configuration

### SecurityConfig

The main security configuration class that configures:

- Web security
- Firebase authentication
- Role-based authorization
- CORS configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Configuration...
}
```

### WebSocketSecurityConfig

Configures WebSocket security, including:

- WebSocket authentication
- WebSocket authorization
- STOMP message broker

```java
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
    // Configuration...
}
```

## Authorization Rules

### REST Endpoints

The application uses role-based authorization for REST endpoints:

- `/auth/**` and `/public/**` - Public access
- `/ws/**` - Public access (WebSocket endpoint)
- `/admin/**` - Requires ADMIN role
- `/support/**` - Requires SUPPORT or ADMIN role
- All other endpoints - Requires authentication

### WebSocket Destinations

The application also uses role-based authorization for WebSocket destinations:

- `/topic/public/**` - Public access
- `/topic/room/**` - Requires authentication
- `/topic/ai/**` - Requires authentication
- `/user/**` - Requires authentication
- `/topic/admin/**` - Requires ADMIN role
- `/topic/support/**` - Requires SUPPORT role

## CORS Configuration

CORS is configured to allow requests from the frontend application:

```yaml
# CORS Configuration
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://support.example.com}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers
  exposed-headers: Authorization
  allow-credentials: true
  max-age: 3600
```

## Client-Side Integration

### REST API Authentication

To authenticate REST API requests, include a Firebase ID token in the Authorization header:

```javascript
// Get Firebase ID token
const idToken = await firebase.auth().currentUser.getIdToken();

// Include token in API requests
fetch('/api/tickets', {
  headers: {
    'Authorization': `Bearer ${idToken}`
  }
});
```

### WebSocket Authentication

To authenticate WebSocket connections, include a Firebase ID token in the connection headers:

```javascript
// Get Firebase ID token
const idToken = await firebase.auth().currentUser.getIdToken();

// Connect to WebSocket with token
const stompClient = new Client({
  webSocketFactory: () => new SockJS('/api/ws'),
  connectHeaders: {
    'Authorization': `Bearer ${idToken}`
  }
});
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `FIREBASE_ENABLED` | Enable Firebase authentication | true |
| `FIREBASE_CREDENTIALS_PATH` | Path to Firebase service account JSON | firebase-service-account.json |
| `FIREBASE_DATABASE_URL` | Firebase Realtime Database URL | - |
| `CORS_ALLOWED_ORIGINS` | Allowed origins for CORS | http://localhost:3000,http://localhost:5173,https://support.example.com |
| `WEBSOCKET_SECURITY_REQUIRE_AUTH` | Require authentication for WebSocket | true |
| `WEBSOCKET_SECURITY_ADMIN_TOPICS_ROLE` | Role for admin topics | ROLE_ADMIN |
| `WEBSOCKET_SECURITY_SUPPORT_TOPICS_ROLE` | Role for support topics | ROLE_SUPPORT |

## Security Best Practices

1. **Keep Firebase service account secure**: The service account JSON file contains sensitive credentials. Never commit it to version control.

2. **Use environment variables**: Use environment variables for sensitive configuration in production.

3. **Validate tokens server-side**: Always validate Firebase ID tokens on the server side, never trust client-side validation.

4. **Use HTTPS**: Always use HTTPS in production to protect authentication tokens.

5. **Implement proper role management**: Assign roles based on user responsibilities and follow the principle of least privilege.

6. **Regularly rotate secrets**: Regularly rotate Firebase service account keys and other secrets.

7. **Monitor authentication events**: Monitor and log authentication events to detect suspicious activity.

## Testing Security

### Testing with Postman

1. Get a Firebase ID token:
   ```javascript
   await firebase.auth().currentUser.getIdToken()
   ```

2. Add the token to the Authorization header:
   ```
   Authorization: Bearer <your-firebase-id-token>
   ```

3. Send the request to a protected endpoint.

### Testing with curl

```bash
# Get a Firebase ID token (using Firebase Authentication REST API)
curl 'https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_API_KEY' \
  -H 'Content-Type: application/json' \
  --data-binary '{"email":"user@example.com","password":"password","returnSecureToken":true}'

# Use the token to access a protected endpoint
curl -H "Authorization: Bearer YOUR_ID_TOKEN" http://localhost:8080/api/tickets
```

### Testing WebSocket Security

Use a WebSocket client library that supports custom headers:

```javascript
const socket = new SockJS('/api/ws');
const stompClient = Stomp.over(socket);

const headers = {
  'Authorization': 'Bearer YOUR_ID_TOKEN'
};

stompClient.connect(headers, frame => {
  console.log('Connected: ' + frame);
  stompClient.subscribe('/topic/room/123', message => {
    console.log('Received: ' + message.body);
  });
});
```
