
### Request Flow

When a client sends a request to a microservice, the API Gateway uses Eureka service discovery to locate the appropriate service instance.

For example, a product request follows this flow:

```text
Client
   ↓
Keycloak
   ↓
Login / Authenticate
   ↓
Access Token (JWT)
   ↓
API Gateway
   ↓
Validate JWT Token
   ↓
/api/product/**
   ↓
lb://product-service
   ↓
Eureka Server
   ↓
Find registered product-service instance
   ↓
http://<product-service-host>:<port>
   ↓
Product Service

```

````

                    ┌──────────────┐
                    │   Keycloak   │
                    │   :8180      │
                    └──────┬───────┘
                           │
                    Authenticate
                           │
                      JWT Token
                           ↓
┌──────────┐       ┌──────────────┐
│  Client  │──────→│ API Gateway  │
└──────────┘       └──────┬───────┘
                          │
                   Validate JWT
                          │
                   /api/product/**
                          │
                          ↓
                  lb://product-service
                          │
                          ↓
                  ┌──────────────┐
                  │    Eureka    │
                  │    Server    │
                  └──────┬───────┘
                         │
              Find product-service
                    instance
                         │
                         ↓
              ┌──────────────────┐
              │ Product Service  │
              │   <host>:<port>  │
              └──────────────────┘