# e-Dhumeni API

A Spring Boot backend application for tracking farmer performances and making informed interventions.

## Overview

e-Dhumeni is a comprehensive system for managing contracted farmers, monitoring their performance, and providing support when needed. It includes:

- Spatial mapping of farmers by region
- Detailed farmer profiles with agricultural data
- Contract management and delivery tracking
- Support alerts for farmers in need of intervention
- Agricultural Extension Officer (AEO) assignment and management

## Technology Stack

- **Backend**: Kotlin + Spring Boot 3.2
- **Database**: PostgreSQL 15 with PostGIS extension for spatial data
- **Security**: JWT-based authentication
- **Documentation**: OpenAPI/Swagger
- **Containerization**: Docker & Docker Compose

## Project Structure

The project follows a clean architecture approach with the following layers:

- **Domain**: Core business logic and entities
- **Application**: Use cases, DTOs, and mappers
- **Infrastructure**: External concerns like security and persistence
- **Web**: Controllers and API endpoints

## Getting Started

### Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (for local development)
- PostgreSQL 15 with PostGIS extension (if not using Docker)

### Running with Docker

1. Clone the repository
2. From the project root, run:

```bash
docker-compose up
```

This will start both the PostgreSQL database and the Spring Boot application.

### Running Locally

1. Start a PostgreSQL database with PostGIS:

```bash
docker-compose up db
```

2. Run the application:

```bash
./gradlew bootRun
```

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/api/swagger-ui.html
```

## Key Features

### Spatial Mapping

- Interactive map showing contracted farmers by region
- Region-based filtering and statistics
- Spatial queries for nearby farmers

### Farmer Management

- Comprehensive farmer profiles with agricultural data
- Support alerts for farmers needing intervention
- Historical performance tracking

### Contract Management

- Multiple contract types
- Delivery tracking and completion monitoring
- Repayment status and risk assessment

### AEO Management

- Assignment of AEOs to regions and farmers
- Tracking AEO visits and interventions
- Performance metrics

## Database Migrations

The application uses Flyway for database migrations. All migration scripts are located in `src/main/resources/db/migration`.

## Security

The API is secured using JWT-based authentication. To access protected endpoints, a valid JWT token must be included in the Authorization header.

## Development Guidelines

- Follow Kotlin coding conventions
- Write unit tests for all services
- Document APIs with OpenAPI annotations
- Use meaningful commit messages
