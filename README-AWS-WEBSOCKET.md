# AWS and WebSocket Integration Guide

This document explains how to use the AWS and WebSocket features in the Support Ticket System.

## AWS Integration

The application integrates with the following AWS services:

### AWS S3 (Simple Storage Service)

Used for storing files such as ticket attachments and user profile pictures.

#### Configuration

```yaml
aws:
  region: ${AWS_REGION:us-east-1}
  s3:
    bucket-name: ${AWS_S3_BUCKET_NAME:support-ticket-system-files}
```

#### Usage

The `AwsStorageService` provides methods for:
- Uploading files to S3
- Generating pre-signed URLs for secure file downloads
- Deleting files from S3

Example:
```java
@Autowired
private AwsStorageService storageService;

// Upload a file
String fileKey = storageService.uploadFile(multipartFile, "tickets");

// Generate a pre-signed URL (valid for 1 hour)
String downloadUrl = storageService.generatePresignedUrl(fileKey, Duration.ofHours(1));

// Delete a file
storageService.deleteFile(fileKey);
```

### AWS SES (Simple Email Service)

Used for sending email notifications to users and experts.

#### Configuration

```yaml
aws:
  region: ${AWS_REGION:us-east-1}
  ses:
    from-email: ${AWS_SES_FROM_EMAIL:no-reply@example.com}
```

#### Usage

The `EmailService` provides methods for:
- Sending emails to single recipients
- Sending emails with CC and BCC recipients

Example:
```java
@Autowired
private EmailService emailService;

// Send a simple email
emailService.sendEmail("user@example.com", "Ticket Update", "<p>Your ticket has been updated.</p>");

// Send an email with CC and BCC
List<String> toAddresses = List.of("user@example.com");
List<String> ccAddresses = List.of("support@example.com");
List<String> bccAddresses = List.of("admin@example.com");
emailService.sendEmail(toAddresses, ccAddresses, bccAddresses, 
                      "Ticket Update", "<p>Your ticket has been updated.</p>");
```

### AWS Parameter Store

Used for securely storing and retrieving configuration parameters.

#### Configuration

```yaml
aws:
  region: ${AWS_REGION:us-east-1}
  ssm:
    parameter-path: ${AWS_SSM_PARAMETER_PATH:/support-ticket-system/}
```

#### Usage

The `ParameterStoreService` provides methods for:
- Getting a single parameter
- Getting all parameters under a path
- Putting a parameter (secure or non-secure)

Example:
```java
@Autowired
private ParameterStoreService parameterStoreService;

// Get a parameter
Optional<String> apiKey = parameterStoreService.getParameter("api-key");

// Get all parameters under a path
Map<String, String> emailConfig = parameterStoreService.getParametersByPath("/email/");

// Put a secure parameter
parameterStoreService.putParameter("api-key", "secret-value", true);

// Put a non-secure parameter
parameterStoreService.putParameter("max-file-size", "10485760", false);
```

## WebSocket Integration

The application uses WebSockets for real-time communication between clients and the server.

### Configuration

```yaml
websocket:
  allowed-origins: ${WEBSOCKET_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
  endpoint: ${WEBSOCKET_ENDPOINT:/ws}
  topic-prefix: ${WEBSOCKET_TOPIC_PREFIX:/topic}
  queue-prefix: ${WEBSOCKET_QUEUE_PREFIX:/queue}
  app-prefix: ${WEBSOCKET_APP_PREFIX:/app}
```

### WebSocket Endpoints

- `/ws` - Main WebSocket endpoint with SockJS support
- `/topic/public` - Public topic for broadcasting messages to all connected clients
- `/user/{username}/queue/messages` - Private queue for user-specific messages
- `/app/chat.sendMessage` - Endpoint for sending public messages
- `/app/chat.addUser` - Endpoint for user joining notifications
- `/app/chat.sendPrivateMessage` - Endpoint for sending private messages

### Security

WebSocket connections are secured using Spring Security. The configuration includes:
- Authentication required for most message destinations
- Public access to connection endpoints and public topics
- Role-based access control for administrative topics

### Client-Side Integration

To connect to the WebSocket from a client (e.g., React):

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Create a new STOMP client
const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/api/ws'),
  connectHeaders: {
    Authorization: 'Bearer ' + jwtToken
  },
  debug: function (str) {
    console.log(str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
});

// Connect to the WebSocket
stompClient.activate();

// Subscribe to public messages
stompClient.onConnect = (frame) => {
  stompClient.subscribe('/topic/public', (message) => {
    const receivedMessage = JSON.parse(message.body);
    console.log(receivedMessage);
  });
  
  // Subscribe to private messages
  stompClient.subscribe('/user/queue/messages', (message) => {
    const receivedMessage = JSON.parse(message.body);
    console.log(receivedMessage);
  });
  
  // Send a join message
  stompClient.publish({
    destination: '/app/chat.addUser',
    body: JSON.stringify({ type: 'JOIN' })
  });
};

// Send a public message
function sendPublicMessage(content) {
  stompClient.publish({
    destination: '/app/chat.sendMessage',
    body: JSON.stringify({ content })
  });
}

// Send a private message
function sendPrivateMessage(recipient, content) {
  stompClient.publish({
    destination: '/app/chat.sendPrivateMessage',
    body: JSON.stringify({ recipient, content })
  });
}
```
