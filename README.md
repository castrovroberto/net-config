# NetConfig - Mini CPQ Engine

A simplified Configure, Price, Quote (CPQ) system for network hardware. This project demonstrates polyglot persistence, event-driven architecture, and design patterns in a Spring Boot microservices context.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         NetConfig CPQ                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     │
│  │   Catalog    │     │Configuration │     │   Pricing    │     │
│  │   Service    │────▶│   Service    │────▶│   Engine     │     │
│  │  (MongoDB)   │     │  (MongoDB)   │     │  (Strategy)  │     │
│  │  :8080       │     │  :8081       │     │  :8082       │     │
│  └──────────────┘     └──────────────┘     └──────────────┘     │
│                              │                    │             │
│                              │                    ▼             │
│                              │           ┌──────────────┐       │
│                              └──────────▶│    Quote     │       │
│                                          │   Service    │       │
│                                          │ (PostgreSQL) │       │
│                                          │  :8083       │       │
│                                          └──────────────┘       │
│                                                 │               │
│                                                 ▼               │
│                                          ┌──────────────┐       │
│                                          │  RabbitMQ    │       │
│                                          │   Events     │       │
│                                          └──────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

## 📋 Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| **catalog-service** | 8080 | MongoDB | Product catalog with flexible schema |
| **configuration-service** | 8081 | MongoDB | Rack configuration management |
| **pricing-service** | 8082 | - | Pricing engine with Strategy Pattern |
| **quote-service** | 8083 | PostgreSQL | Quote generation with ACID compliance |

## 🛠️ Tech Stack

- **Java 21** - Latest LTS with records, virtual threads
- **Spring Boot 3.4** - Framework
- **MongoDB** - Product catalog (flexible schema)
- **PostgreSQL** - Quotes (ACID compliance)
- **RabbitMQ** - Async event processing
- **Gradle** - Build tool with Kotlin DSL

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.x (or use the wrapper)

### 1. Start Infrastructure

```bash
# Start MongoDB, PostgreSQL, RabbitMQ and admin UIs
docker-compose up -d

# Verify services are healthy
docker-compose ps
```

**Admin UIs:**
- Mongo Express: http://localhost:8081
- pgAdmin: http://localhost:8082 (admin@netconfig.local / admin123)
- RabbitMQ Management: http://localhost:15672 (netconfig / netconfig123)

### 2. Build the Project

```bash
# Build all modules
./gradlew build

# Skip tests for faster build
./gradlew build -x test
```

### 3. Run Services

Run each service in a separate terminal:

```bash
# Terminal 1: Catalog Service
./gradlew :catalog-service:bootRun

# Terminal 2: Configuration Service
./gradlew :configuration-service:bootRun

# Terminal 3: Pricing Service
./gradlew :pricing-service:bootRun

# Terminal 4: Quote Service
./gradlew :quote-service:bootRun
```

### 4. Verify Services

```bash
# Check health endpoints
curl http://localhost:8080/actuator/health  # Catalog
curl http://localhost:8081/actuator/health  # Configuration
curl http://localhost:8082/actuator/health  # Pricing
curl http://localhost:8083/actuator/health  # Quote
```

## 📚 API Examples

### Catalog Service (MongoDB)

```bash
# List all products
curl http://localhost:8080/api/v1/products

# Get products by type
curl http://localhost:8080/api/v1/products?type=SWITCH

# Get product by SKU
curl http://localhost:8080/api/v1/products/sku/SW-CATALYST-9300-24

# Find switches with minimum ports
curl http://localhost:8080/api/v1/products/switches/min-ports/24
```

## 🗃️ Sample Data

The catalog service loads sample products on startup (dev profile):

| Type | SKU | Name | Price |
|------|-----|------|-------|
| RACK | RACK-42U-STD | 42U Standard Server Rack | $2,499.99 |
| SWITCH | SW-CATALYST-9300-24 | Catalyst 9300 24-Port | $4,599.99 |
| SWITCH | SW-NEXUS-9336C | Nexus 9336C-FX2 | $24,999.99 |
| PSU | PSU-2000W-TITANIUM | 2000W Titanium PDU | $1,299.99 |

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :catalog-service:test

# Run with test containers (integration tests)
./gradlew integrationTest
```

## 📁 Project Structure

```
netconfig/
├── common/                     # Shared DTOs and utilities
│   └── src/main/java/
│       └── com/netconfig/common/
│           ├── dto/            # ApiResponse, ProductDto, ValidationResult
│           ├── event/          # QuoteRequestedEvent, QuoteReadyEvent
│           └── exception/      # Custom exceptions
│
├── catalog-service/            # Product catalog (MongoDB)
│   └── src/main/java/
│       └── com/netconfig/catalog/
│           ├── domain/         # Product, ProductType
│           ├── repository/     # ProductRepository
│           ├── service/        # ProductService
│           ├── controller/     # ProductController
│           └── config/         # DataLoader
│
├── configuration-service/      # Rack configuration
│   └── src/main/java/
│       └── com/netconfig/configuration/
│           └── domain/         # RackConfiguration, ConfigurationItem
│
├── pricing-service/            # Pricing engine
│   └── src/main/java/
│       └── com/netconfig/pricing/
│           ├── domain/         # PricingContext, PricingResult
│           └── strategy/       # PricingStrategy interface
│
├── quote-service/              # Quote generation (PostgreSQL + RabbitMQ)
│   └── src/main/java/
│       └── com/netconfig/quote/
│           ├── domain/         # Quote, QuoteLineItem
│           ├── repository/     # QuoteRepository
│           ├── config/         # RabbitMQConfig
│           └── messaging/      # EventPublisher, EventListener
│
├── docker-compose.yml          # Infrastructure
├── build.gradle.kts            # Root build file
└── settings.gradle.kts         # Module definitions
```

## 🎯 Implementation Phases

- [x] **Phase 0**: Project scaffolding & infrastructure
- [ ] **Phase 1**: Product Catalog (MongoDB) - REST API complete
- [ ] **Phase 2**: Configuration & Validation Rules
- [ ] **Phase 3**: Pricing Engine with Strategy Pattern
- [ ] **Phase 4**: Quote Service with async PDF generation
- [ ] **Phase 5**: Integration & Polish

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | dev | Active profile |
| `MONGODB_URI` | mongodb://localhost:27017 | MongoDB connection |
| `POSTGRES_URL` | jdbc:postgresql://localhost:5432/quotes | PostgreSQL connection |
| `RABBITMQ_HOST` | localhost | RabbitMQ host |

### Docker Compose Services

| Service | Port | Credentials |
|---------|------|-------------|
| MongoDB | 27017 | netconfig / netconfig123 |
| PostgreSQL | 5432 | netconfig / netconfig123 |
| RabbitMQ | 5672, 15672 | netconfig / netconfig123 |
| Mongo Express | 8081 | - |
| pgAdmin | 8082 | admin@netconfig.local / admin123 |

## 📖 Design Patterns Used

1. **Strategy Pattern** - Pricing strategies (VolumeDiscount, PartnerDiscount)
2. **Chain of Responsibility** - Configuration validation rules
3. **Event-Driven** - Quote generation with RabbitMQ
4. **Repository Pattern** - Data access layer
5. **DTO Pattern** - API response wrapping

## 📝 License

MIT License - See [LICENSE](LICENSE) for details.
