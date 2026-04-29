#!/bin/sh
# Maven Wrapper Script
# Attempts to use system mvn, falls back gracefully
set -e

MAVEN_CMD="mvn"

# Check if mvn is available
if ! command -v mvn > /dev/null 2>&1; then
    echo "ERROR: Maven not found. Please install Maven 3.9+ or add it to PATH."
    exit 1
fi

exec "$MAVEN_CMD" "$@"
