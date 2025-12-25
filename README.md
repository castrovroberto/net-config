# ⚡ NetConfig CPQ

A **Configure-Price-Quote (CPQ)** system for network hardware, built to demonstrate enterprise architecture patterns including microservices, polyglot persistence, event-driven design, and domain-driven business logic.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange)

## 🎯 Project Overview

NetConfig simulates a simplified CPQ system for a network hardware vendor, focusing on:

- **Configure (C)**: Build rack configurations with switches, PSUs, and accessories
- **Price (P)**: Calculate pricing with volume discounts, partner tiers, and support add-ons
- **Quote (Q)**: Generate immutable quotes with async PDF generation

### Architecture Highlights

| Aspect | Implementation |
|--------|----------------|
| **Polyglot Persistence** | MongoDB (flexible catalog & configs) + PostgreSQL (ACID quotes) |
| **Event-Driven** | RabbitMQ for async PDF generation |
| **Design Patterns** | Strategy (pricing), Chain of Responsibility (validation) |
| **API Documentation** | OpenAPI/Swagger for all services |
| **Observability** | Correlation IDs, structured logging |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                           Web UI (SPA)                              │
│                        http://localhost:8080                        │
└─────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Catalog     │         │ Configuration │         │   Pricing     │
│   Service     │◄────────│   Service     │────────►│   Service     │
│    :8080      │         │    :8081      │         │    :8082      │
└───────────────┘         └───────────────┘         └───────────────┘
        │                         │                         │
        ▼                         ▼                         │
┌───────────────┐         ┌───────────────┐                 │
│   MongoDB     │         │   MongoDB     │                 │
│  (Products)   │         │   (Configs)   │                 │
└───────────────┘         └───────────────┘                 │
                                                            │
                          ┌───────────────┐                 │
                          │    Quote      │◄────────────────┘
                          │   Service     │
                          │    :8083      │
                          └───────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼                           ▼
            ┌───────────────┐         ┌───────────────┐
            │  PostgreSQL   │         │   RabbitMQ    │
            │   (Quotes)    │         │  (PDF Queue)  │
            └───────────────┘         └───────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- curl & jq (for demo script)

### Start Everything

```bash
# 1. Start infrastructure (databases, message broker)
docker compose up -d

# 2. Build the project
./gradlew build -x test

# 3. Start all services (in separate terminals or background)
./scripts/start-services.sh

# Or start individually:
java -jar catalog-service/build/libs/catalog-service-0.0.1-SNAPSHOT.jar &
java -jar configuration-service/build/libs/configuration-service-0.0.1-SNAPSHOT.jar &
java -jar pricing-service/build/libs/pricing-service-0.0.1-SNAPSHOT.jar &
java -jar quote-service/build/libs/quote-service-0.0.1-SNAPSHOT.jar &
```

### Access Points

| Service | URL |
|---------|-----|
| **Web UI** | http://localhost:8080 |
| **Catalog API** | http://localhost:8080/swagger-ui.html |
| **Configuration API** | http://localhost:8081/swagger-ui.html |
| **Pricing API** | http://localhost:8082/swagger-ui.html |
| **Quote API** | http://localhost:8083/swagger-ui.html |
| Mongo Express | http://localhost:8180 |
| pgAdmin | http://localhost:8181 (admin@netconfig.local / admin123) |
| RabbitMQ | http://localhost:15672 (guest / guest) |

### Run Demo Script

```bash
./scripts/demo.sh
```

This walks through the complete CPQ flow with colorful output.

## 📦 Services

### Catalog Service (Port 8080)

Manages the product catalog with MongoDB's flexible schema.

**Product Types:**
- `RACK` - Server racks (42U, 24U, etc.)
- `SWITCH` - Network switches (Catalyst, Nexus)
- `PSU` - Power supply units
- `CABLE` - DAC cables, fiber
- `SFP_MODULE` - Transceiver modules

**API Examples:**

```bash
# List all products
curl http://localhost:8080/api/v1/products | jq

# Filter by type
curl http://localhost:8080/api/v1/products?type=SWITCH | jq

# Get specific product
curl http://localhost:8080/api/v1/products/sku/SW-CATALYST-9300-48 | jq
```

### Configuration Service (Port 8081)

Manages rack configurations and validates using Chain of Responsibility pattern.

**Validation Rules:**
1. `RackRequiredRule` - Must have a base rack
2. `ComponentExistsRule` - All components must exist in catalog
3. `MinimumPsuRule` - PSU required if powered devices present
4. `PowerBudgetRule` - Total power draw ≤ PSU capacity
5. `RackCapacityRule` - Total rack units ≤ rack size
6. `RedundantPsuRule` - Warning if single PSU (advisory)

**API Examples:**

```bash
# Create configuration
curl -X POST http://localhost:8081/api/v1/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Data Center Rack",
    "customerId": "acme-corp",
    "rackSku": "RACK-42U-STD"
  }'

# Add component
curl -X POST http://localhost:8081/api/v1/configurations/{id}/components \
  -H "Content-Type: application/json" \
  -d '{"productSku": "SW-CATALYST-9300-48", "quantity": 4}'

# Validate
curl -X POST http://localhost:8081/api/v1/configurations/{id}/validate | jq
```

### Pricing Service (Port 8082)

Calculates pricing using the Strategy pattern for composable rules.

**Pricing Strategies:**
1. `BasePriceStrategy` - Sum of component prices × quantities
2. `VolumeDiscountStrategy` - 10% off switches when count > 5
3. `BundleDiscountStrategy` - 5% off when rack utilization > 75%
4. `PartnerDiscountStrategy` - Tier-based: Standard (5%), Gold (10%), Platinum (15%)
5. `SupportAddOnStrategy` - Premium support adds 20% of subtotal

**API Examples:**

```bash
# Calculate pricing
curl -X POST http://localhost:8082/api/v1/pricing/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "configurationId": "YOUR_CONFIG_ID",
    "customerTier": "PARTNER",
    "options": {
      "include_support": true,
      "support_tier": "PREMIUM"
    }
  }' | jq
```

### Quote Service (Port 8083)

Creates immutable quotes with PostgreSQL and async PDF generation via RabbitMQ.

**Quote Lifecycle:**
1. `PENDING` - Quote created, PDF generating
2. `READY` - PDF ready for download
3. `ACCEPTED` - Customer accepted
4. `REJECTED` - Customer rejected
5. `EXPIRED` - Past 30-day validity

**API Examples:**

```bash
# Create quote
curl -X POST http://localhost:8083/api/v1/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "configurationId": "YOUR_CONFIG_ID",
    "customerName": "Acme Corporation",
    "customerEmail": "buyer@acme.com",
    "customerTier": "PARTNER",
    "includeSupport": true,
    "supportTier": "PREMIUM"
  }' | jq

# Get quote
curl http://localhost:8083/api/v1/quotes/{id} | jq

# Accept quote
curl -X POST http://localhost:8083/api/v1/quotes/{id}/accept | jq
```

## 🔍 Design Patterns

### Chain of Responsibility (Validation)

```java
// Rules are executed in order, collecting results
@Component
@Order(1)
public class RackRequiredRule implements ConfigurationRule {
    @Override
    public RuleResult evaluate(ValidationContext context) {
        if (context.getRack() == null) {
            return RuleResult.failure("RACK_REQUIRED", "Configuration must have a base rack");
        }
        return RuleResult.success("Rack present");
    }
}
```

### Strategy Pattern (Pricing)

```java
// Each strategy can modify the pricing result
@Component
@Order(2)
public class VolumeDiscountStrategy implements PricingStrategy {
    @Override
    public void apply(PricingContext context, PricingResult result) {
        if (context.getSwitchCount() > 5) {
            BigDecimal discount = result.getSwitchSubtotal()
                .multiply(new BigDecimal("0.10"));
            result.addOrderDiscount(discount, "Volume Discount - 10% off switches");
        }
    }
}
```

## 📊 Sample Data

On startup, the Catalog Service loads 15 sample products:

| SKU | Type | Name | Price |
|-----|------|------|-------|
| RACK-42U-STD | RACK | 42U Standard Rack | $2,500 |
| SW-CATALYST-9300-48 | SWITCH | Catalyst 9300-48P | $8,500 |
| SW-NEXUS-93180YC | SWITCH | Nexus 93180YC-FX | $18,000 |
| PSU-2000W-TITANIUM | PSU | 2000W Titanium PSU | $1,200 |
| ... | ... | ... | ... |

## 🔧 Development

### Project Structure

```
net-config/
├── common/                     # Shared DTOs, exceptions, filters
├── catalog-service/            # Product catalog (MongoDB)
├── configuration-service/      # Config & validation (MongoDB)
├── pricing-service/            # Pricing engine (stateless)
├── quote-service/              # Quote management (PostgreSQL + RabbitMQ)
├── docker/                     # Docker init scripts
├── scripts/                    # Utility scripts
│   ├── demo.sh                 # Full CPQ flow demo
│   ├── start-services.sh       # Start all services
│   └── stop-services.sh        # Stop all services
└── docker-compose.yml          # Infrastructure
```

### Run Tests

```bash
# All tests
./gradlew test

# Specific service
./gradlew :configuration-service:test
```

### Correlation ID Tracing

All requests include correlation IDs for distributed tracing:

```bash
# Pass your own correlation ID
curl -H "X-Correlation-ID: my-trace-123" http://localhost:8080/api/v1/products

# Response headers include the ID
< X-Correlation-ID: my-trace-123
```

Logs include correlation IDs:

```
2024-01-15 10:30:45.123 [http-nio-8080-exec-1] [abc-123-def] INFO ProductController - Fetching all products
```

## 📋 Complete CPQ Flow

```bash
# 1. Browse catalog
curl http://localhost:8080/api/v1/products?type=RACK

# 2. Create configuration
CONFIG_ID=$(curl -s -X POST http://localhost:8081/api/v1/configurations \
  -H "Content-Type: application/json" \
  -d '{"name": "Production Rack", "customerId": "acme", "rackSku": "RACK-42U-STD"}' \
  | jq -r '.data.id')

# 3. Add components
curl -X POST "http://localhost:8081/api/v1/configurations/$CONFIG_ID/components" \
  -H "Content-Type: application/json" \
  -d '{"productSku": "SW-CATALYST-9300-48", "quantity": 6}'

curl -X POST "http://localhost:8081/api/v1/configurations/$CONFIG_ID/components" \
  -H "Content-Type: application/json" \
  -d '{"productSku": "PSU-2000W-TITANIUM", "quantity": 2}'

# 4. Validate
curl -X POST "http://localhost:8081/api/v1/configurations/$CONFIG_ID/validate" | jq

# 5. Get pricing
curl -X POST http://localhost:8082/api/v1/pricing/calculate \
  -H "Content-Type: application/json" \
  -d "{\"configurationId\": \"$CONFIG_ID\", \"customerTier\": \"PARTNER\"}" | jq

# 6. Generate quote
QUOTE=$(curl -s -X POST http://localhost:8083/api/v1/quotes \
  -H "Content-Type: application/json" \
  -d "{\"configurationId\": \"$CONFIG_ID\", \"customerName\": \"Acme Corp\"}")
QUOTE_ID=$(echo $QUOTE | jq -r '.data.id')

# 7. Wait for PDF and accept
sleep 3
curl -X POST "http://localhost:8083/api/v1/quotes/$QUOTE_ID/accept" | jq
```

## 🎨 Web UI

The built-in web UI provides a visual interface for the complete CPQ flow:

1. Browse and filter products by type
2. Select a base rack and add components
3. Validate configuration with real-time feedback
4. Calculate pricing with discounts
5. Generate and accept quotes

Access at: **http://localhost:8080**

## 🛑 Stopping Services

```bash
# Stop services
./scripts/stop-services.sh

# Stop infrastructure
docker compose down

# Clean everything (including data)
docker compose down -v
```

## 📚 Learning Objectives

This project demonstrates:

1. **Microservices Architecture** - Independent, deployable services
2. **Polyglot Persistence** - Right database for each use case
3. **Event-Driven Design** - Async processing with message queues
4. **Design Patterns** - Strategy, Chain of Responsibility
5. **API Design** - RESTful APIs with OpenAPI documentation
6. **Observability** - Correlation IDs, structured logging
7. **Domain Logic** - Complex business rules in code

## 📄 License

MIT License - See [LICENSE](LICENSE) for details.
