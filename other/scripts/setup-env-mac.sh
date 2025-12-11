#!/bin/bash

# =================================================================
# Camping Booking System - macOS Environment Setup Script
# This script automatically configures all required tools using Homebrew
# =================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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
    echo -e "${CYAN}  Camping Booking System - macOS Environment Setup${NC}"
    echo -e "${CYAN}===================================================================${NC}"
    echo ""
}

print_status() {
    local message=$1
    local status=$2
    
    case $status in
        success)
            echo -e "${GREEN}[✓]${NC} $message"
            ;;
        error)
            echo -e "${RED}[✗]${NC} $message"
            ;;
        warning)
            echo -e "${YELLOW}[!]${NC} $message"
            ;;
        *)
            echo -e "${CYAN}[*]${NC} $message"
            ;;
    esac
}

check_command() {
    if command -v $1 &> /dev/null; then
        return 0
    else
        return 1
    fi
}

get_version() {
    if check_command $1; then
        $1 --version 2>&1 | head -n 1
    else
        echo "Not installed"
    fi
}

# Install Homebrew if needed
install_homebrew() {
    if ! check_command brew; then
        print_status "Installing Homebrew..." "warning"
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        print_status "Homebrew installed" "success"
    else
        print_status "Homebrew already installed" "success"
    fi
}

# ========== Main Execution ==========

main() {
    print_header
    
    # Install Homebrew first
    echo -e "${YELLOW}[0/6] Checking Homebrew...${NC}"
    install_homebrew
    echo ""
    
    # Update Homebrew
    print_status "Updating Homebrew..." "info"
    brew update
    echo ""
    
    # Java
    echo -e "${YELLOW}[1/6] Checking Java JDK 11...${NC}"
    if check_command java; then
        print_status "Java $(get_version java)" "success"
    else
        print_status "Installing OpenJDK 11..." "warning"
        brew install openjdk@11
        
        # Link Java
        sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
        
        print_status "Java installed" "success"
    fi
    echo ""
    
    # Maven
    echo -e "${YELLOW}[2/6] Checking Maven...${NC}"
    if check_command mvn; then
        print_status "Maven $(get_version mvn)" "success"
    else
        print_status "Installing Maven..." "warning"
        brew install maven
        print_status "Maven installed" "success"
    fi
    echo ""
    
    # PostgreSQL
    echo -e "${YELLOW}[3/6] Checking PostgreSQL...${NC}"
    if check_command psql; then
        print_status "PostgreSQL $(get_version psql)" "success"
    else
        print_status "Installing PostgreSQL..." "warning"
        brew install postgresql
        
        # Start PostgreSQL
        print_status "Starting PostgreSQL service..." "info"
        brew services start postgresql
        
        # Wait for PostgreSQL to start
        sleep 5
        
        print_status "PostgreSQL installed" "success"
    fi
    echo ""
    
    # Node.js
    echo -e "${YELLOW}[4/6] Checking Node.js...${NC}"
    if check_command node; then
        print_status "Node.js $(get_version node)" "success"
    else
        print_status "Installing Node.js 18..." "warning"
        brew install node@18
        
        # Link Node.js
        brew link node@18
        
        print_status "Node.js installed" "success"
    fi
    echo ""
    
    # npm
    echo -e "${YELLOW}[5/6] Checking npm...${NC}"
    if check_command npm; then
        print_status "npm $(get_version npm)" "success"
    else
        print_status "npm not found. Reinstalling Node.js..." "error"
        brew reinstall node@18
    fi
    echo ""
    
    # Git
    echo -e "${YELLOW}[6/6] Checking Git (optional)...${NC}"
    if check_command git; then
        print_status "Git $(get_version git)" "success"
    else
        print_status "Installing Git..." "warning"
        brew install git
        print_status "Git installed" "success"
    fi
    echo ""
    
    # Initialize database
    echo -e "${YELLOW}Initializing database...${NC}"
    if check_command psql; then
        cd "$SCRIPT_DIR"
        
        # Create database
        createdb camping_db 2>/dev/null || true
        
        # Load SQL files
        psql camping_db -f sql/schema.sql
        psql camping_db -f sql/views_triggers.sql
        psql camping_db -f sql/data.sql
        
        print_status "Database initialized" "success"
    else
        print_status "PostgreSQL not available. Skip database init." "warning"
    fi
    echo ""
    
    # Summary
    echo -e "${CYAN}===================================================================${NC}"
    echo -e "${GREEN}Setup completed successfully!${NC}"
    echo -e "${CYAN}===================================================================${NC}"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Run: bash start.sh"
    echo ""
    echo -e "${YELLOW}Default database credentials:${NC}"
    echo "  Username: $(whoami)"
    echo "  Database: camping_db"
    echo ""
    echo -e "${YELLOW}For detailed configuration, see:${NC}"
    echo "  ENVIRONMENT_SETUP.md"
    echo ""
}

# Run main
main "$@"
