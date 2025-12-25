#!/bin/bash

# NetConfig - Start All Services
# This script starts all microservices in the correct order

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo ""
echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC}  ${BOLD}⚡ NetConfig - Starting Services${NC}                             ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Start Docker containers
echo -e "${YELLOW}▶ Starting Docker containers...${NC}"
cd "$PROJECT_ROOT"
docker compose up -d

echo ""
echo -e "${GREEN}✓ Docker containers started${NC}"
echo ""

# Wait for databases to be ready
echo -e "${YELLOW}▶ Waiting for databases...${NC}"
sleep 5

# Build the project
echo -e "${YELLOW}▶ Building services...${NC}"
./gradlew build -x test --quiet

echo ""
echo -e "${GREEN}✓ Build complete${NC}"
echo ""

# Create logs directory
mkdir -p "$PROJECT_ROOT/logs"

# Start each service
echo -e "${YELLOW}▶ Starting Catalog Service (8080)...${NC}"
nohup java -jar "$PROJECT_ROOT/catalog-service/build/libs/catalog-service-0.0.1-SNAPSHOT.jar" \
    > "$PROJECT_ROOT/logs/catalog-service.log" 2>&1 &
echo $! > "$PROJECT_ROOT/logs/catalog-service.pid"
sleep 3

echo -e "${YELLOW}▶ Starting Configuration Service (8081)...${NC}"
nohup java -jar "$PROJECT_ROOT/configuration-service/build/libs/configuration-service-0.0.1-SNAPSHOT.jar" \
    > "$PROJECT_ROOT/logs/configuration-service.log" 2>&1 &
echo $! > "$PROJECT_ROOT/logs/configuration-service.pid"
sleep 3

echo -e "${YELLOW}▶ Starting Pricing Service (8082)...${NC}"
nohup java -jar "$PROJECT_ROOT/pricing-service/build/libs/pricing-service-0.0.1-SNAPSHOT.jar" \
    > "$PROJECT_ROOT/logs/pricing-service.log" 2>&1 &
echo $! > "$PROJECT_ROOT/logs/pricing-service.pid"
sleep 3

echo -e "${YELLOW}▶ Starting Quote Service (8083)...${NC}"
nohup java -jar "$PROJECT_ROOT/quote-service/build/libs/quote-service-0.0.1-SNAPSHOT.jar" \
    > "$PROJECT_ROOT/logs/quote-service.log" 2>&1 &
echo $! > "$PROJECT_ROOT/logs/quote-service.pid"
sleep 3

echo ""
echo -e "${GREEN}✓ All services started!${NC}"
echo ""

# Health check
echo -e "${YELLOW}▶ Checking service health...${NC}"
echo ""

check_health() {
    local name=$1
    local port=$2
    for i in {1..10}; do
        if curl -s "http://localhost:$port/actuator/health" | grep -q "UP"; then
            echo -e "  ${GREEN}✓${NC} $name is healthy"
            return 0
        fi
        sleep 1
    done
    echo -e "  ${YELLOW}⚠${NC} $name is starting..."
    return 0
}

check_health "Catalog Service" 8080
check_health "Configuration Service" 8081
check_health "Pricing Service" 8082
check_health "Quote Service" 8083

echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Services are running at:"
echo "  • Web UI:              http://localhost:8080"
echo "  • Catalog API:         http://localhost:8080/swagger-ui.html"
echo "  • Configuration API:   http://localhost:8081/swagger-ui.html"
echo "  • Pricing API:         http://localhost:8082/swagger-ui.html"
echo "  • Quote API:           http://localhost:8083/swagger-ui.html"
echo ""
echo "Admin UIs:"
echo "  • Mongo Express:       http://localhost:8180"
echo "  • pgAdmin:             http://localhost:8181"
echo "  • RabbitMQ:            http://localhost:15672 (guest/guest)"
echo ""
echo "Logs: $PROJECT_ROOT/logs/"
echo ""
echo "To stop: ./scripts/stop-services.sh"
echo ""

