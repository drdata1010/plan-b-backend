# Advanced Configuration Guide

This document explains the advanced AWS and WebSocket configurations in the Support Ticket System.

## AWS Configuration

The application uses AWS services for various functionalities. The configuration is defined in `application.yml`:

### AWS Region Configuration

```yaml
aws:
  region: ${AWS_REGION:us-east-1}
  access-key: ${AWS_ACCESS_KEY:}
  secret-key: ${AWS_SECRET_KEY:}
  credentials:
    use-default: ${AWS_USE_DEFAULT_CREDENTIALS:true}
```

- `region`: The AWS region to use for all services (default: us-east-1)
- `access-key` and `secret-key`: AWS credentials (optional, uses default credentials provider if not set)
- `use-default`: Whether to use the default credentials provider (default: true)

### S3 Configuration

```yaml
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET_NAME:support-ticket-system-files}
    endpoint: ${AWS_S3_ENDPOINT:}
    presigned-url-expiration: ${AWS_S3_PRESIGNED_URL_EXPIRATION:3600}
    region: ${AWS_S3_REGION:${AWS_REGION:us-east-1}}
    public-bucket: ${AWS_S3_PUBLIC_BUCKET:false}
    folders:
      attachments: ${AWS_S3_FOLDER_ATTACHMENTS:attachments}
      profiles: ${AWS_S3_FOLDER_PROFILES:profiles}
      tickets: ${AWS_S3_FOLDER_TICKETS:tickets}
      temp: ${AWS_S3_FOLDER_TEMP:temp}
    max-file-size: ${AWS_S3_MAX_FILE_SIZE:10485760} # 10MB
    allowed-file-types: ${AWS_S3_ALLOWED_FILE_TYPES:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,csv,zip}
```

- `bucket-name`: The S3 bucket name for file storage
- `endpoint`: Custom S3 endpoint URL (optional, for testing with localstack)
- `presigned-url-expiration`: Expiration time in seconds for presigned URLs
- `region`: S3-specific region (defaults to the global AWS region)
- `public-bucket`: Whether the bucket is publicly accessible
- `folders`: Predefined folder structure for organizing files
- `max-file-size`: Maximum allowed file size in bytes
- `allowed-file-types`: Comma-separated list of allowed file extensions

### SES Configuration

```yaml
aws:
  ses:
    from-email: ${AWS_SES_FROM_EMAIL:no-reply@example.com}
    reply-to-email: ${AWS_SES_REPLY_TO_EMAIL:}
    endpoint: ${AWS_SES_ENDPOINT:}
    region: ${AWS_SES_REGION:${AWS_REGION:us-east-1}}
    sandbox-mode: ${AWS_SES_SANDBOX_MODE:true}
    templates:
      welcome: ${AWS_SES_TEMPLATE_WELCOME:welcome-template}
      password-reset: ${AWS_SES_TEMPLATE_PASSWORD_RESET:password-reset-template}
      ticket-created: ${AWS_SES_TEMPLATE_TICKET_CREATED:ticket-created-template}
      ticket-updated: ${AWS_SES_TEMPLATE_TICKET_UPDATED:ticket-updated-template}
      ticket-resolved: ${AWS_SES_TEMPLATE_TICKET_RESOLVED:ticket-resolved-template}
```

- `from-email`: Email address to send emails from
- `reply-to-email`: Reply-to email address (optional)
- `endpoint`: Custom SES endpoint URL (optional, for testing with localstack)
- `region`: SES-specific region (defaults to the global AWS region)
- `sandbox-mode`: Whether SES is in sandbox mode (requires verified email addresses)
- `templates`: Predefined email templates for various notifications

### Parameter Store Configuration

```yaml
aws:
  ssm:
    parameter-path: ${AWS_SSM_PARAMETER_PATH:/support-ticket-system/}
    endpoint: ${AWS_SSM_ENDPOINT:}
    cache-ttl-seconds: ${AWS_SSM_CACHE_TTL_SECONDS:300}
    preload-parameters: ${AWS_SSM_PRELOAD_PARAMETERS:false}
    region: ${AWS_SSM_REGION:${AWS_REGION:us-east-1}}
    paths:
      secrets: ${AWS_SSM_PATH_SECRETS:/support-ticket-system/secrets/}
      config: ${AWS_SSM_PATH_CONFIG:/support-ticket-system/config/}
      api-keys: ${AWS_SSM_PATH_API_KEYS:/support-ticket-system/api-keys/}
      feature-flags: ${AWS_SSM_PATH_FEATURE_FLAGS:/support-ticket-system/feature-flags/}
```

- `parameter-path`: Base path for parameters in Parameter Store
- `endpoint`: Custom SSM endpoint URL (optional, for testing with localstack)
- `cache-ttl-seconds`: Time-to-live for parameter cache in seconds
- `preload-parameters`: Whether to preload parameters at startup
- `region`: SSM-specific region (defaults to the global AWS region)
- `paths`: Predefined parameter paths for organizing configuration

## WebSocket Configuration

The application uses WebSockets for real-time communication. The configuration is defined in `application.yml`:

### Basic WebSocket Configuration

```yaml
websocket:
  allowed-origins: ${WEBSOCKET_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://support.example.com}
  endpoint: ${WEBSOCKET_ENDPOINT:/ws}
  topic-prefix: ${WEBSOCKET_TOPIC_PREFIX:/topic}
  queue-prefix: ${WEBSOCKET_QUEUE_PREFIX:/queue}
  app-prefix: ${WEBSOCKET_APP_PREFIX:/app}
```

- `allowed-origins`: Comma-separated list of allowed origins for CORS
- `endpoint`: WebSocket endpoint path
- `topic-prefix`: Prefix for broadcast topics
- `queue-prefix`: Prefix for user-specific queues
- `app-prefix`: Prefix for client-to-server messages

### Message Configuration

```yaml
websocket:
  message:
    max-size: ${WEBSOCKET_MESSAGE_MAX_SIZE:65536}
    trace-enabled: ${WEBSOCKET_MESSAGE_TRACE_ENABLED:false}
    buffer-size-per-session: ${WEBSOCKET_MESSAGE_BUFFER_SIZE_PER_SESSION:1024}
  send-buffer-size: ${WEBSOCKET_SEND_BUFFER_SIZE:524288}
  send-time-limit: ${WEBSOCKET_SEND_TIME_LIMIT:15000}
```

- `max-size`: Maximum message size in bytes
- `trace-enabled`: Whether to enable message tracing for debugging
- `buffer-size-per-session`: Maximum number of messages per session
- `send-buffer-size`: Send buffer size in bytes
- `send-time-limit`: Send timeout in milliseconds

### STOMP Broker Relay Configuration

```yaml
websocket:
  broker-relay:
    enabled: ${WEBSOCKET_BROKER_RELAY_ENABLED:false}
    host: ${WEBSOCKET_BROKER_RELAY_HOST:localhost}
    port: ${WEBSOCKET_BROKER_RELAY_PORT:61613}
    username: ${WEBSOCKET_BROKER_RELAY_USERNAME:guest}
    password: ${WEBSOCKET_BROKER_RELAY_PASSWORD:guest}
    virtual-host: ${WEBSOCKET_BROKER_RELAY_VIRTUAL_HOST:/}
    heartbeat:
      client: ${WEBSOCKET_BROKER_RELAY_HEARTBEAT_CLIENT:10000}
      server: ${WEBSOCKET_BROKER_RELAY_HEARTBEAT_SERVER:10000}
```

- `enabled`: Whether to use an external STOMP broker (e.g., RabbitMQ, ActiveMQ)
- `host`: Broker host
- `port`: Broker port
- `username` and `password`: Broker credentials
- `virtual-host`: Broker virtual host
- `heartbeat`: Client and server heartbeat intervals in milliseconds

### Security Configuration

```yaml
websocket:
  security:
    require-authentication: ${WEBSOCKET_SECURITY_REQUIRE_AUTH:true}
    admin-topics-role: ${WEBSOCKET_SECURITY_ADMIN_TOPICS_ROLE:ROLE_ADMIN}
    support-topics-role: ${WEBSOCKET_SECURITY_SUPPORT_TOPICS_ROLE:ROLE_SUPPORT}
```

- `require-authentication`: Whether to require authentication for WebSocket connections
- `admin-topics-role`: Role required for administrative topics
- `support-topics-role`: Role required for support topics

### Endpoints Configuration

```yaml
websocket:
  endpoints:
    chat: ${WEBSOCKET_ENDPOINTS_CHAT:/chat}
    notifications: ${WEBSOCKET_ENDPOINTS_NOTIFICATIONS:/notifications}
    tickets: ${WEBSOCKET_ENDPOINTS_TICKETS:/tickets}
    admin: ${WEBSOCKET_ENDPOINTS_ADMIN:/admin}
```

- `chat`: Endpoint for chat functionality
- `notifications`: Endpoint for notifications
- `tickets`: Endpoint for ticket updates
- `admin`: Endpoint for administrative functions

### AI Chat Configuration

```yaml
websocket:
  ai:
    enabled: ${WEBSOCKET_AI_ENABLED:true}
    default-model: ${WEBSOCKET_AI_DEFAULT_MODEL:gpt-3.5-turbo}
    timeout: ${WEBSOCKET_AI_TIMEOUT:30000}
    max-tokens: ${WEBSOCKET_AI_MAX_TOKENS:1000}
    temperature: ${WEBSOCKET_AI_TEMPERATURE:0.7}
    models:
      - id: gpt-3.5-turbo
        name: GPT-3.5 Turbo
        provider: openai
        enabled: ${WEBSOCKET_AI_MODEL_GPT35_ENABLED:true}
      - id: gpt-4
        name: GPT-4
        provider: openai
        enabled: ${WEBSOCKET_AI_MODEL_GPT4_ENABLED:true}
      - id: claude-2
        name: Claude 2
        provider: anthropic
        enabled: ${WEBSOCKET_AI_MODEL_CLAUDE2_ENABLED:false}
```

- `enabled`: Whether AI chat is enabled
- `default-model`: Default AI model to use
- `timeout`: AI request timeout in milliseconds
- `max-tokens`: Maximum number of tokens for AI responses
- `temperature`: AI response temperature (randomness)
- `models`: Configuration for available AI models

## Environment Variables

### AWS Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | us-east-1 |
| `AWS_ACCESS_KEY` | AWS access key | - |
| `AWS_SECRET_KEY` | AWS secret key | - |
| `AWS_USE_DEFAULT_CREDENTIALS` | Use default credentials provider | true |
| `AWS_S3_BUCKET_NAME` | S3 bucket name | support-ticket-system-files |
| `AWS_S3_ENDPOINT` | Custom S3 endpoint URL | - |
| `AWS_S3_REGION` | S3-specific region | AWS_REGION |
| `AWS_S3_PUBLIC_BUCKET` | Whether the bucket is publicly accessible | false |
| `AWS_S3_PRESIGNED_URL_EXPIRATION` | Presigned URL expiration in seconds | 3600 |
| `AWS_S3_MAX_FILE_SIZE` | Maximum file size in bytes | 10485760 |
| `AWS_S3_ALLOWED_FILE_TYPES` | Allowed file extensions | jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,csv,zip |
| `AWS_SES_FROM_EMAIL` | Email address to send from | no-reply@example.com |
| `AWS_SES_REPLY_TO_EMAIL` | Reply-to email address | - |
| `AWS_SES_ENDPOINT` | Custom SES endpoint URL | - |
| `AWS_SES_REGION` | SES-specific region | AWS_REGION |
| `AWS_SES_SANDBOX_MODE` | Whether SES is in sandbox mode | true |
| `AWS_SSM_PARAMETER_PATH` | Base path for parameters | /support-ticket-system/ |
| `AWS_SSM_ENDPOINT` | Custom SSM endpoint URL | - |
| `AWS_SSM_REGION` | SSM-specific region | AWS_REGION |
| `AWS_SSM_CACHE_TTL_SECONDS` | Parameter cache TTL in seconds | 300 |
| `AWS_SSM_PRELOAD_PARAMETERS` | Preload parameters at startup | false |

### WebSocket Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WEBSOCKET_ALLOWED_ORIGINS` | Allowed origins for CORS | http://localhost:3000,http://localhost:5173,https://support.example.com |
| `WEBSOCKET_ENDPOINT` | WebSocket endpoint path | /ws |
| `WEBSOCKET_TOPIC_PREFIX` | Prefix for broadcast topics | /topic |
| `WEBSOCKET_QUEUE_PREFIX` | Prefix for user-specific queues | /queue |
| `WEBSOCKET_APP_PREFIX` | Prefix for client-to-server messages | /app |
| `WEBSOCKET_MESSAGE_MAX_SIZE` | Maximum message size in bytes | 65536 |
| `WEBSOCKET_MESSAGE_TRACE_ENABLED` | Enable message tracing | false |
| `WEBSOCKET_MESSAGE_BUFFER_SIZE_PER_SESSION` | Max messages per session | 1024 |
| `WEBSOCKET_SEND_BUFFER_SIZE` | Send buffer size in bytes | 524288 |
| `WEBSOCKET_SEND_TIME_LIMIT` | Send timeout in milliseconds | 15000 |
| `WEBSOCKET_BROKER_RELAY_ENABLED` | Use external STOMP broker | false |
| `WEBSOCKET_BROKER_RELAY_HOST` | Broker host | localhost |
| `WEBSOCKET_BROKER_RELAY_PORT` | Broker port | 61613 |
| `WEBSOCKET_BROKER_RELAY_USERNAME` | Broker username | guest |
| `WEBSOCKET_BROKER_RELAY_PASSWORD` | Broker password | guest |
| `WEBSOCKET_BROKER_RELAY_VIRTUAL_HOST` | Broker virtual host | / |
| `WEBSOCKET_SECURITY_REQUIRE_AUTH` | Require authentication | true |
| `WEBSOCKET_SECURITY_ADMIN_TOPICS_ROLE` | Admin topics role | ROLE_ADMIN |
| `WEBSOCKET_SECURITY_SUPPORT_TOPICS_ROLE` | Support topics role | ROLE_SUPPORT |
| `WEBSOCKET_AI_ENABLED` | Enable AI chat | true |
| `WEBSOCKET_AI_DEFAULT_MODEL` | Default AI model | gpt-3.5-turbo |
| `WEBSOCKET_AI_TIMEOUT` | AI request timeout in ms | 30000 |
| `WEBSOCKET_AI_MAX_TOKENS` | Max tokens for AI responses | 1000 |
| `WEBSOCKET_AI_TEMPERATURE` | AI response temperature | 0.7 |

## Using External Message Broker

For production environments, it's recommended to use an external message broker for WebSocket communication. This provides better scalability and reliability, especially in a clustered environment.

### RabbitMQ Configuration

To use RabbitMQ as the STOMP broker:

1. Set `websocket.broker-relay.enabled` to `true`
2. Configure the RabbitMQ connection details:
   ```yaml
   websocket:
     broker-relay:
       enabled: true
       host: rabbitmq.example.com
       port: 61613
       username: myuser
       password: mypassword
       virtual-host: /
   ```

3. Install the STOMP plugin for RabbitMQ:
   ```bash
   rabbitmq-plugins enable rabbitmq_stomp
   ```

### ActiveMQ Configuration

To use ActiveMQ as the STOMP broker:

1. Set `websocket.broker-relay.enabled` to `true`
2. Configure the ActiveMQ connection details:
   ```yaml
   websocket:
     broker-relay:
       enabled: true
       host: activemq.example.com
       port: 61613
       username: myuser
       password: mypassword
       virtual-host: /
   ```

## Testing with LocalStack

For local development and testing, you can use LocalStack to emulate AWS services:

1. Start LocalStack with the required services:
   ```bash
   docker run -d -p 4566:4566 -e SERVICES=s3,ses,ssm localstack/localstack
   ```

2. Configure the application to use LocalStack endpoints:
   ```yaml
   aws:
     region: us-east-1
     s3:
       endpoint: http://localhost:4566
     ses:
       endpoint: http://localhost:4566
     ssm:
       endpoint: http://localhost:4566
   ```

3. Create the S3 bucket in LocalStack:
   ```bash
   aws --endpoint-url=http://localhost:4566 s3 mb s3://support-ticket-system-files
   ```

4. Add test parameters to Parameter Store:
   ```bash
   aws --endpoint-url=http://localhost:4566 ssm put-parameter --name "/support-ticket-system/secrets/test-secret" --value "test-value" --type SecureString
   ```
