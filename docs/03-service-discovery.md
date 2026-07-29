## Service Discovery & Load Balancing

To improve scalability and remove hardcoded service URLs, the project uses **Netflix Eureka** for service discovery.

### Eureka Server

A new module named `discovery-server` was created under the `micro-parent` project.

The Eureka server acts as a service registry where all microservices register themselves during startup.

Registered services:
- `inventory-service`
- `order-service`
- `product-service`

### Service Registration

Each microservice is configured as a Eureka client and automatically registers with the discovery server.

This allows services to discover each other dynamically without requiring fixed hostnames or port numbers.

### Dynamic Service URLs

Previously, `order-service` communicated with `inventory-service` using a hardcoded URL such as:

```text
http://localhost:8082/api/inventory
```

After introducing Eureka, the service URL was updated to use the application name:

```text
http://inventory-service/api/inventory
```

Using the service name instead of a fixed host and port allows communication regardless of where the service instance is running.

### Multiple Inventory Service Instances

`inventory-service` is configured with:

```properties
server.port=0
```

This allows Spring Boot to assign a random available port whenever a new instance starts.

As a result, multiple instances of `inventory-service` can run simultaneously on the same machine without port conflicts.

### Client-Side Load Balancing

A `@LoadBalanced` `WebClient.Builder` is configured in `order-service`.

When a request is made to:

```text
http://inventory-service/api/inventory
```

Spring Cloud resolves the service name through Eureka and automatically selects one of the available `inventory-service` instances.

This provides client-side load balancing and enables requests to continue working even when multiple instances of the service are running.

Without the `@LoadBalanced` configuration, `WebClient` cannot resolve the service name (`inventory-service`) into a running instance, causing service-to-service communication to fail.