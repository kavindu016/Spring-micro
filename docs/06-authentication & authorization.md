# Authentication & Authorization

The microservices use **Keycloak** for authentication and authorization. Keycloak acts as the Identity and Access Management (IAM) server and issues OAuth 2.0 access tokens.

## Keycloak Setup

Keycloak is running locally on:

```
http://localhost:8180
```

### Realm

A dedicated realm was created for the microservices:

```
spring-boot-micro
```

### Client

Inside the realm, an OAuth 2.0 client was created:

```
spring-client
```

The client is used to generate an OAuth 2.0 access token, which is then used to access the secured microservices.

## Authentication Flow

```
                    ┌──────────────────┐
                    │     Keycloak     │
                    │ localhost:8180   │
                    │                  │
                    │ Realm:           │
                    │ spring-boot-micro│
                    │                  │
                    │ Client:          │
                    │ spring-client    │
                    └────────┬─────────┘
                             │
                             │ OAuth 2.0 JWT
                             ▼
                    ┌──────────────────┐
                    │      Client      │
                    └────────┬─────────┘
                             │
                             │ Bearer Token
                             ▼
                    ┌──────────────────┐
                    │   API Gateway    │
                    └────────┬─────────┘
                             │
                             │ JWT Validation
                             ▼
                    ┌──────────────────┐
                    │ Protected APIs   │
                    └──────────────────┘
```

## Spring Security Configuration

The services are configured as OAuth 2.0 Resource Servers. Spring Security validates the JWT access token issued by Keycloak.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    // Eureka endpoints do not require authentication
                    .requestMatchers("/eureka/**").permitAll()

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> {})
            )
            .build();
}
```

## Endpoint Security

| Endpoint | Authentication |
|----------|-----------------|
| `/eureka/**` | Not required |
| All other endpoints | Required |

The `/eureka/**` endpoints are excluded from authentication because Eureka is used for internal service discovery.

All other endpoints require a valid JWT access token.

## OAuth 2.0 Access Token

The `spring-client` in Keycloak is used to generate an OAuth 2.0 access token.

The generated access token is a JWT and must be included in requests to protected endpoints.

Example:

```http
GET /api/products
Authorization: Bearer <access_token>
```

Spring Security validates the JWT before allowing access to the protected endpoint.

## Eureka Security

Eureka is used for service discovery. The microservices register themselves with Eureka and communicate with it internally.

The Eureka endpoints are excluded from authentication:

```java
.requestMatchers("/eureka/**").permitAll()
```

This allows the services to register with Eureka without requiring a JWT token.

## Production Security

In production, Eureka should not be exposed to the public internet.

The following approach should be used:

- Keep Eureka service-to-service communication inside the private network.
- Block the Eureka port (e.g., `8761`) from public/external access using a firewall or security group.
- Allow only internal services to communicate with Eureka.
- Keep `/eureka/**` as `permitAll()` if required for internal service registration.
- Do not expose the Eureka dashboard as a public endpoint.
- If the dashboard needs to be accessed manually, use an SSH tunnel through a machine inside the private network.

This provides network-level protection for Eureka instead of trying to add JWT authentication to the browser-facing Eureka dashboard.
