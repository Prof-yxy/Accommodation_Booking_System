#!/bin/bash

# =================================================================
# Camping Booking System - Linux Environment Setup Script
# This script automatically configures all required tools
# Supports: Ubuntu/Debian, CentOS/RHEL
# =================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DISTRO=""
PKG_MANAGER=""

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
    echo -e "${CYAN}  Camping Booking System - Linux Environment Setup${NC}"
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

detect_distro() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO=$ID
    fi
    
    case $DISTRO in
        ubuntu|debian)
            PKG_MANAGER="apt"
            ;;
        centos|rhel|fedora)
            PKG_MANAGER="yum"
            ;;
        *)
            print_status "Unknown Linux distribution. Please install tools manually." "error"
            exit 1
            ;;
    esac
    
    print_status "Detected: $DISTRO using $PKG_MANAGER" "success"
}

check_sudo() {
    if [ "$EUID" -ne 0 ]; then
        print_status "This script requires sudo privileges" "error"
        echo "Please run: sudo bash $0"
        exit 1
    fi
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

# ========== Installation Functions ==========

install_java_ubuntu() {
    print_status "Installing OpenJDK 11..." "warning"
    apt-get update
    apt-get install -y openjdk-11-jdk openjdk-11-jdk-headless
    print_status "Java installed" "success"
}

install_java_centos() {
    print_status "Installing OpenJDK 11..." "warning"
    yum install -y java-11-openjdk java-11-openjdk-devel
    print_status "Java installed" "success"
}

install_maven_ubuntu() {
    print_status "Installing Maven..." "warning"
    apt-get install -y maven
    print_status "Maven installed" "success"
}

install_maven_centos() {
    print_status "Installing Maven..." "warning"
    yum install -y maven
    print_status "Maven installed" "success"
}

install_postgresql_ubuntu() {
    print_status "Installing PostgreSQL..." "warning"
    apt-get install -y postgresql postgresql-contrib
    print_status "PostgreSQL installed" "success"
    
    # Start service
    systemctl start postgresql
    systemctl enable postgresql
}

install_postgresql_centos() {
    print_status "Installing PostgreSQL..." "warning"
    yum install -y postgresql-server postgresql-contrib
    print_status "PostgreSQL installed" "success"
    
    # Initialize database (CentOS)
    if ! [ -d /var/lib/pgsql/data/base ]; then
        print_status "Initializing PostgreSQL database..." "info"
        postgresql-setup initdb
    fi
    
    # Start service
    systemctl start postgresql
    systemctl enable postgresql
}

install_nodejs_ubuntu() {
    print_status "Installing Node.js and npm..." "warning"
    apt-get install -y nodejs npm
    print_status "Node.js and npm installed" "success"
}

install_nodejs_centos() {
    print_status "Installing Node.js and npm..." "warning"
    yum install -y nodejs npm
    print_status "Node.js and npm installed" "success"
}

install_git_ubuntu() {
    print_status "Installing Git..." "warning"
    apt-get install -y git
    print_status "Git installed" "success"
}

install_git_centos() {
    print_status "Installing Git..." "warning"
    yum install -y git
    print_status "Git installed" "success"
}

# ========== Main Execution ==========

main() {
    print_header
    
    # Check sudo
    check_sudo
    
    # Detect distro
    detect_distro
    echo ""
    
    # ===== Check and install tools =====
    
    # Java
    echo -e "${YELLOW}[1/6] Checking Java JDK 11...${NC}"
    if check_command java; then
        print_status "Java $(get_version java)" "success"
    else
        if [ "$PKG_MANAGER" = "apt" ]; then
            install_java_ubuntu
        else
            install_java_centos
        fi
    fi
    echo ""
    
    # Maven
    echo -e "${YELLOW}[2/6] Checking Maven...${NC}"
    if check_command mvn; then
        print_status "Maven $(get_version mvn)" "success"
    else
        if [ "$PKG_MANAGER" = "apt" ]; then
            install_maven_ubuntu
        else
            install_maven_centos
        fi
    fi
    echo ""
    
    # PostgreSQL
    echo -e "${YELLOW}[3/6] Checking PostgreSQL...${NC}"
    if check_command psql; then
        print_status "PostgreSQL $(get_version psql)" "success"
    else
        if [ "$PKG_MANAGER" = "apt" ]; then
            install_postgresql_ubuntu
        else
            install_postgresql_centos
        fi
    fi
    echo ""
    
    # Node.js
    echo -e "${YELLOW}[4/6] Checking Node.js...${NC}"
    if check_command node; then
        print_status "Node.js $(get_version node)" "success"
    else
        if [ "$PKG_MANAGER" = "apt" ]; then
            install_nodejs_ubuntu
        else
            install_nodejs_centos
        fi
    fi
    echo ""
    
    # npm
    echo -e "${YELLOW}[5/6] Checking npm...${NC}"
    if check_command npm; then
        print_status "npm $(get_version npm)" "success"
    else
        print_status "npm not found. Please reinstall Node.js" "error"
        exit 1
    fi
    echo ""
    
    # Git
    echo -e "${YELLOW}[6/6] Checking Git (optional)...${NC}"
    if check_command git; then
        print_status "Git $(get_version git)" "success"
    else
        print_status "Git not installed (optional)" "warning"
        if [ "$PKG_MANAGER" = "apt" ]; then
            install_git_ubuntu
        else
            install_git_centos
        fi
    fi
    echo ""
    
    # Initialize database
    echo -e "${YELLOW}Initializing database...${NC}"
    if check_command psql; then
        cd "$SCRIPT_DIR"
        
        # Switch to postgres user to create database
        sudo -u postgres psql -f sql/schema.sql
        sudo -u postgres psql -d camping_db -f sql/views_triggers.sql
        sudo -u postgres psql -d camping_db -f sql/data.sql
        
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
    echo -e "${YELLOW}For detailed configuration, see:${NC}"
    echo "  ENVIRONMENT_SETUP.md"
    echo ""
}

# Run main
main "$@"
