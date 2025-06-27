# Coupon Service

A Spring Boot microservice for managing coupons with Redis cluster caching and TiDB database.

## Prerequisites

- **Java 21** or higher
- **Docker & Docker Compose**
- **Maven** (or use included Maven wrapper)

## Quick Start

### 1. Start Infrastructure Services

```bash
# Start all required services (TiDB, Redis Cluster, Prometheus, Grafana)
docker-compose up -d
```

This will start:

- **TiDB**: Database server on port `4000`
- **Redis Cluster**: 6 nodes on ports `7001-7006`
- **Prometheus**: Monitoring on port `9090`
- **Grafana**: Dashboard on port `3000` (admin/123456)

### 2. Run the Application

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or with installed Maven:

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

## Environment Profiles

- **Development** (default): `application-dev.yml`
- **Production**: `application-prod.yml`

To run with production profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## API Endpoints

The service provides coupon management APIs:

- **GET** `/api/coupons` - Get all coupons
- **GET** `/api/coupons/available` - Get available coupons
- **GET** `/api/coupons/{code}` - Get coupon by code
- **POST** `/api/coupons/{code}/apply` - Apply coupon

Admin endpoints:

- **POST** `/api/admin/coupons` - Create coupon
- **PUT** `/api/admin/coupons/{id}` - Update coupon

## Monitoring

- **Application Metrics**: http://localhost:8080/actuator/prometheus
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/123456)

## Testing

Run unit tests:

```bash
./mvnw test
```

Load testing with k6:

```bash
# Make sure k6 is installed
k6 run k6/apply-voucher-test.js
k6 run k6/get-large-data-test.js
```

## Redis Tools

Connect to Redis cluster:

```bash
make redis-connect
```

## Stopping Services

```bash
# Stop the application: Ctrl+C

# Stop infrastructure services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Architecture

- **Framework**: Spring Boot 3.3.12
- **Database**: TiDB (MySQL compatible)
- **Cache**: Redis Cluster
- **Monitoring**: Prometheus + Grafana
- **Build Tool**: Maven
