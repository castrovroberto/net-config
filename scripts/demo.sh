#!/bin/bash

# NetConfig CPQ Demo Script
# This script demonstrates the full Configure-Price-Quote flow

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# API endpoints
CATALOG_URL="http://localhost:8080/api/v1"
CONFIG_URL="http://localhost:8081/api/v1"
PRICING_URL="http://localhost:8082/api/v1"
QUOTE_URL="http://localhost:8083/api/v1"

echo ""
echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC}  ${BOLD}⚡ NetConfig CPQ - Demo Script${NC}                              ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  Configure-Price-Quote Flow for Network Hardware            ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to check if a service is running
check_service() {
    local name=$1
    local url=$2
    if curl -s "$url/actuator/health" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} $name is running"
        return 0
    else
        echo -e "  ${RED}✗${NC} $name is not running"
        return 1
    fi
}

# Check all services
echo -e "${YELLOW}▶ Checking service health...${NC}"
echo ""
check_service "Catalog Service (8080)" "http://localhost:8080" || exit 1
check_service "Configuration Service (8081)" "http://localhost:8081" || exit 1
check_service "Pricing Service (8082)" "http://localhost:8082" || exit 1
check_service "Quote Service (8083)" "http://localhost:8083" || exit 1
echo ""
echo -e "${GREEN}All services are running!${NC}"
echo ""

# Step 1: List products
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}📦 STEP 1: View Available Products${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Fetching product catalog..."
curl -s "$CATALOG_URL/products" | jq -r '.data[] | "\(.type | @sh) \(.sku | @sh) \(.name | @sh) $\(.basePrice)"' | head -10
echo ""
read -p "Press Enter to continue..."
echo ""

# Step 2: Create configuration
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}⚙️  STEP 2: Create Rack Configuration${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Creating a new configuration with RACK-42U-STD..."
CONFIG_RESPONSE=$(curl -s -X POST "$CONFIG_URL/configurations" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Demo Data Center Rack",
        "customerId": "demo-customer",
        "rackSku": "RACK-42U-STD"
    }')
CONFIG_ID=$(echo $CONFIG_RESPONSE | jq -r '.data.id')
echo -e "Configuration ID: ${CYAN}$CONFIG_ID${NC}"
echo ""

# Add components
echo "Adding 6x Catalyst 9300-48 switches..."
curl -s -X POST "$CONFIG_URL/configurations/$CONFIG_ID/components" \
    -H "Content-Type: application/json" \
    -d '{"productSku": "SW-CATALYST-9300-48", "quantity": 6}' > /dev/null

echo "Adding 2x 2000W Titanium PSUs..."
curl -s -X POST "$CONFIG_URL/configurations/$CONFIG_ID/components" \
    -H "Content-Type: application/json" \
    -d '{"productSku": "PSU-2000W-TITANIUM", "quantity": 2}' > /dev/null

echo "Adding 10x 10G DAC Cables..."
curl -s -X POST "$CONFIG_URL/configurations/$CONFIG_ID/components" \
    -H "Content-Type: application/json" \
    -d '{"productSku": "CBL-DAC-10G-3M", "quantity": 10}' > /dev/null

echo ""
echo -e "${GREEN}✓ Configuration created with components${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# Step 3: Validate configuration
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}✓ STEP 3: Validate Configuration${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Running validation rules (Power Budget, Rack Capacity, etc.)..."
VALIDATION_RESPONSE=$(curl -s -X POST "$CONFIG_URL/configurations/$CONFIG_ID/validate")
IS_VALID=$(echo $VALIDATION_RESPONSE | jq -r '.data.valid')
POWER_DRAW=$(echo $VALIDATION_RESPONSE | jq -r '.data.totalPowerDrawWatts')
POWER_CAPACITY=$(echo $VALIDATION_RESPONSE | jq -r '.data.totalPsuCapacityWatts')
RACK_USED=$(echo $VALIDATION_RESPONSE | jq -r '.data.totalRackUnitsUsed')
RACK_CAPACITY=$(echo $VALIDATION_RESPONSE | jq -r '.data.rackCapacityUnits')

echo ""
if [ "$IS_VALID" = "true" ]; then
    echo -e "${GREEN}✓ Configuration is VALID${NC}"
else
    echo -e "${RED}✗ Configuration is INVALID${NC}"
    echo $VALIDATION_RESPONSE | jq -r '.data.allErrors[]'
fi
echo ""
echo "  Power: ${POWER_DRAW}W / ${POWER_CAPACITY}W"
echo "  Rack:  ${RACK_USED}U / ${RACK_CAPACITY}U"
echo ""
read -p "Press Enter to continue..."
echo ""

# Step 4: Calculate pricing
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}💰 STEP 4: Calculate Pricing${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Calculating price with Partner tier and Premium Support..."
PRICING_RESPONSE=$(curl -s -X POST "$PRICING_URL/pricing/calculate" \
    -H "Content-Type: application/json" \
    -d "{
        \"configurationId\": \"$CONFIG_ID\",
        \"customerTier\": \"PARTNER\",
        \"options\": {
            \"include_support\": true,
            \"support_tier\": \"PREMIUM\"
        }
    }")

SUBTOTAL=$(echo $PRICING_RESPONSE | jq -r '.data.subtotal')
DISCOUNT=$(echo $PRICING_RESPONSE | jq -r '.data.totalDiscount')
SUPPORT=$(echo $PRICING_RESPONSE | jq -r '.data.serviceAddOn')
TOTAL=$(echo $PRICING_RESPONSE | jq -r '.data.grandTotal')

echo ""
echo "  Subtotal:        \$$SUBTOTAL"
echo -e "  Total Discount:  ${GREEN}-\$$DISCOUNT${NC}"
echo "  Premium Support: +\$$SUPPORT"
echo "  ─────────────────────────"
echo -e "  ${BOLD}Grand Total:     \$$TOTAL${NC}"
echo ""
echo "Applied discounts:"
echo $PRICING_RESPONSE | jq -r '.data.discountDescriptions[]' | sed 's/^/  • /'
echo ""
read -p "Press Enter to continue..."
echo ""

# Step 5: Generate quote
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}📄 STEP 5: Generate Quote${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Creating quote (triggers async PDF generation)..."
QUOTE_RESPONSE=$(curl -s -X POST "$QUOTE_URL/quotes" \
    -H "Content-Type: application/json" \
    -d "{
        \"configurationId\": \"$CONFIG_ID\",
        \"customerName\": \"Acme Corporation\",
        \"customerEmail\": \"buyer@acme.com\",
        \"customerTier\": \"PARTNER\",
        \"includeSupport\": true,
        \"supportTier\": \"PREMIUM\"
    }")

QUOTE_ID=$(echo $QUOTE_RESPONSE | jq -r '.data.id')
QUOTE_NUMBER=$(echo $QUOTE_RESPONSE | jq -r '.data.quoteNumber')
QUOTE_STATUS=$(echo $QUOTE_RESPONSE | jq -r '.data.status')

echo ""
echo -e "Quote Number: ${CYAN}$QUOTE_NUMBER${NC}"
echo "Quote ID: $QUOTE_ID"
echo "Status: $QUOTE_STATUS"
echo ""

# Wait for PDF generation
echo "Waiting for PDF generation..."
for i in {1..5}; do
    sleep 1
    QUOTE_RESPONSE=$(curl -s "$QUOTE_URL/quotes/$QUOTE_ID")
    QUOTE_STATUS=$(echo $QUOTE_RESPONSE | jq -r '.data.status')
    echo "  Status: $QUOTE_STATUS"
    if [ "$QUOTE_STATUS" = "READY" ]; then
        break
    fi
done
echo ""
echo -e "${GREEN}✓ Quote is ready!${NC}"
echo ""
read -p "Press Enter to continue..."
echo ""

# Step 6: Accept quote
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}✓ STEP 6: Accept Quote${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Accepting the quote..."
ACCEPT_RESPONSE=$(curl -s -X POST "$QUOTE_URL/quotes/$QUOTE_ID/accept")
FINAL_STATUS=$(echo $ACCEPT_RESPONSE | jq -r '.data.status')
echo ""
echo -e "Final Status: ${GREEN}$FINAL_STATUS${NC}"
echo ""

# Summary
echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC}  ${BOLD}🎉 CPQ Flow Complete!${NC}                                       ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Summary:"
echo "  • Configuration: $CONFIG_ID"
echo "  • Quote Number: $QUOTE_NUMBER"
echo "  • Total Value: \$$TOTAL"
echo "  • Status: $FINAL_STATUS"
echo ""
echo "API Documentation:"
echo "  • Catalog:       http://localhost:8080/swagger-ui.html"
echo "  • Configuration: http://localhost:8081/swagger-ui.html"
echo "  • Pricing:       http://localhost:8082/swagger-ui.html"
echo "  • Quote:         http://localhost:8083/swagger-ui.html"
echo ""
echo "Web UI: http://localhost:8080"
echo ""

