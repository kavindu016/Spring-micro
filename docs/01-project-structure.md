## Project Structure

This repository is organized as a multi-module Maven project. The root `micro-parent` module manages and groups all microservices and shared infrastructure.

### Microservices

| Module | Description |
|--------|-------------|
| **api-gateway** | Single entry point that routes client requests to the appropriate service |
| **discovery-server** | Service registry (Eureka) for service discovery and registration |
| **product-service** | Manages product data and catalog operations |
| **inventory-service** | Tracks stock levels and product availability |
| **order-service** | Handles order creation and processing |
| **notification-service** | Sends notifications for order events |

### Infrastructure & Configuration

| Folder / File | Purpose |
|---------------|---------|
| `grafana/` | Grafana configuration and dashboards |
| `prometheus/` | Prometheus metrics scraping configuration |
| `realms/` | Keycloak realm configuration for authentication |
| `mongo-data/` | Persistent MongoDB data volume |
| `docker-compose.yml` | Runs the full stack (services + infrastructure) with Docker |
| `pom.xml` | Parent POM that manages modules and shared dependencies |

### Directory Tree

```
Spring boot Microservice
├── docs/
├── images/
├── grafana-dashboard.json
├── README.md
└── Micro-parent/
    ├── api-gateway/
    ├── discovery-server/
    ├── inventory-service/
    ├── notification-service/
    ├── order-service/
    ├── product-service/
    ├── grafana/
    ├── prometheus/
    ├── realms/
    ├── mongo-data/
    ├── docker-compose.yml
    └── pom.xml
```