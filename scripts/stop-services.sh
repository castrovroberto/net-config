#!/bin/bash

# NetConfig - Stop All Services
# This script stops all microservices and optionally Docker containers

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo ""
echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC}  ${BOLD}⚡ NetConfig - Stopping Services${NC}                             ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to stop a service
stop_service() {
    local name=$1
    local pidfile="$PROJECT_ROOT/logs/$name.pid"
    
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid"
            echo -e "  ${GREEN}✓${NC} Stopped $name (PID: $pid)"
        else
            echo -e "  ${YELLOW}⚠${NC} $name was not running"
        fi
        rm -f "$pidfile"
    else
        echo -e "  ${YELLOW}⚠${NC} No PID file for $name"
    fi
}

echo -e "${YELLOW}▶ Stopping services...${NC}"
echo ""

stop_service "catalog-service"
stop_service "configuration-service"
stop_service "pricing-service"
stop_service "quote-service"

echo ""

# Also kill any Java processes on the service ports (fallback)
for port in 8080 8081 8082 8083; do
    pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        kill $pid 2>/dev/null || true
        echo -e "  ${GREEN}✓${NC} Killed process on port $port"
    fi
done

echo ""

# Ask about Docker containers
read -p "Stop Docker containers too? (y/N) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo -e "${YELLOW}▶ Stopping Docker containers...${NC}"
    cd "$PROJECT_ROOT"
    docker compose down
    echo -e "${GREEN}✓ Docker containers stopped${NC}"
fi

echo ""
echo -e "${GREEN}✓ All services stopped${NC}"
echo ""

