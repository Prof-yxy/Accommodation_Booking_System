#!/bin/bash

# =================================================================
# Camping Booking System - Installation Verification Script
# Verify that all components are properly installed
# =================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FAILED=0
PASSED=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ========== Functions ==========

print_header() {
    echo ""
    echo -e "${CYAN}===================================================================${NC}"
    echo -e "${CYAN}  Camping Booking System - Installation Verification${NC}"
    echo -e "${CYAN}===================================================================${NC}"
    echo ""
}

test_component() {
    local name=$1
    local command=$2
    local expected=$3
    
    echo -n "Testing $name... "
    
    if output=$($command 2>&1); then
        if [[ -z "$expected" ]] || [[ "$output" =~ $expected ]]; then
            echo -e "${GREEN}[PASS]${NC}"
            ((PASSED++))
            return 0
        else
            echo -e "${RED}[FAIL]${NC} - Expected: $expected"
            ((FAILED++))
            return 1
        fi
    else
        echo -e "${RED}[FAIL]${NC} - Command failed"
        ((FAILED++))
        return 1
    fi
}

test_service() {
    local name=$1
    local command=$2
    
    echo -n "Testing $name service... "
    
    if eval $command &>/dev/null; then
        echo -e "${GREEN}[PASS]${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${YELLOW}[WARN]${NC} - Service may not be running"
        return 1
    fi
}

test_directory() {
    local name=$1
    local path=$2
    
    echo -n "Checking $name... "
    
    if [ -d "$path" ]; then
        echo -e "${GREEN}[PASS]${NC} - $path"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}[FAIL]${NC} - $path not found"
        ((FAILED++))
        return 1
    fi
}

test_file() {
    local name=$1
    local file=$2
    
    echo -n "Checking $name... "
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}[PASS]${NC} - $file"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}[FAIL]${NC} - $file not found"
        ((FAILED++))
        return 1
    fi
}

# ========== Main Tests ==========

main() {
    print_header
    
    echo -e "${CYAN}[*] Tools Installation${NC}"
    echo "----"
    
    # Java
    test_component "Java JDK" "java -version" "version"
    test_component "Java Compiler" "javac -version" "javac"
    
    # Maven
    test_component "Maven" "mvn -version" "Apache Maven"
    
    # PostgreSQL
    test_component "PostgreSQL" "psql --version" "postgres"
    
    # Node.js
    test_component "Node.js" "node --version" "v"
    test_component "npm" "npm --version" ""
    
    # Git
    test_component "Git" "git --version" "git version"
    
    echo ""
    echo -e "${CYAN}[*] Services${NC}"
    echo "----"
    
    # PostgreSQL service
    if command -v systemctl &> /dev/null; then
        test_service "PostgreSQL" "systemctl is-active postgresql"
    elif command -v brew &> /dev/null; then
        test_service "PostgreSQL" "brew services list | grep postgresql"
    fi
    
    echo ""
    echo -e "${CYAN}[*] Project Structure${NC}"
    echo "----"
    
    # Backend
    test_directory "Backend source" "$SCRIPT_DIR/backend/src"
    test_directory "Backend target" "$SCRIPT_DIR/backend/target"
    test_file "pom.xml" "$SCRIPT_DIR/backend/pom.xml"
    
    # Frontend
    test_directory "Frontend source" "$SCRIPT_DIR/frontend/src"
    test_file "package.json" "$SCRIPT_DIR/frontend/package.json"
    
    # Database
    test_directory "SQL scripts" "$SCRIPT_DIR/sql"
    test_file "schema.sql" "$SCRIPT_DIR/sql/schema.sql"
    test_file "views_triggers.sql" "$SCRIPT_DIR/sql/views_triggers.sql"
    test_file "data.sql" "$SCRIPT_DIR/sql/data.sql"
    
    echo ""
    echo -e "${CYAN}[*] Documentation${NC}"
    echo "----"
    
    test_file "README" "$SCRIPT_DIR/README.md"
    test_file "QUICK_START" "$SCRIPT_DIR/QUICK_START.md"
    test_file "ENVIRONMENT_SETUP" "$SCRIPT_DIR/ENVIRONMENT_SETUP.md"
    test_file "PROJECT_STRUCTURE" "$SCRIPT_DIR/PROJECT_STRUCTURE.md"
    
    echo ""
    echo -e "${CYAN}[*] Scripts${NC}"
    echo "----"
    
    test_file "start.sh" "$SCRIPT_DIR/start.sh"
    test_file "setup-env-linux.sh" "$SCRIPT_DIR/setup-env-linux.sh"
    
    # Summary
    echo ""
    echo -e "${CYAN}===================================================================${NC}"
    
    TOTAL=$((PASSED + FAILED))
    PERCENTAGE=$((PASSED * 100 / TOTAL))
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}All tests passed! ✓${NC}"
        echo -e "${GREEN}Installation verification: SUCCESS${NC}"
    else
        echo -e "${YELLOW}Some tests failed!${NC}"
        echo -e "Passed: ${GREEN}$PASSED${NC} Failed: ${RED}$FAILED${NC} Total: $TOTAL"
        echo -e "Success rate: ${PERCENTAGE}%"
    fi
    
    echo -e "${CYAN}===================================================================${NC}"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}You are ready to start!${NC}"
        echo ""
        echo "Next steps:"
        echo "1. Run the application:"
        echo "   bash start.sh"
        echo ""
        echo "2. Open browser:"
        echo "   http://localhost:5173"
        echo ""
    else
        echo -e "${YELLOW}Please fix the failed tests before starting.${NC}"
        echo ""
        echo "Common issues:"
        echo "• PostgreSQL not running: sudo systemctl start postgresql"
        echo "• Tools not installed: Run setup-env-linux.sh or setup-env-mac.sh"
        echo "• Database not initialized: Check sql/ directory"
        echo ""
    fi
    
    return $FAILED
}

# Run verification
main "$@"
exit $?
