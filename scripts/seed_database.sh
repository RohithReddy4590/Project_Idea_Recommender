#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# seed_database.sh — Load seed data into the recommender database
# Usage: ./scripts/seed_database.sh [host] [port] [db] [user] [password]
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

DB_HOST="${1:-localhost}"
DB_PORT="${2:-3306}"
DB_NAME="${3:-recommender_db}"
DB_USER="${4:-root}"
DB_PASSWORD="${5:-root}"

SEED_DIR="$(dirname "$0")/../src/main/resources/db/data"

echo "──────────────────────────────────────────────────"
echo "  Project Idea Recommender — Database Seeder"
echo "  Host: ${DB_HOST}:${DB_PORT}  DB: ${DB_NAME}"
echo "──────────────────────────────────────────────────"

run_sql() {
  local file="$1"
  echo "→ Loading: $(basename "$file") ..."
  mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
        "${DB_NAME}" < "${file}"
  echo "  ✓ Done"
}

# Check mysql client is available
if ! command -v mysql &>/dev/null; then
  echo "✗ ERROR: 'mysql' client not found. Install mysql-client and retry."
  exit 1
fi

# Check connection
echo "→ Testing database connection..."
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
      -e "SELECT 1;" "${DB_NAME}" > /dev/null 2>&1 || {
  echo "✗ ERROR: Cannot connect to MySQL at ${DB_HOST}:${DB_PORT}. Check credentials."
  exit 1
}
echo "  ✓ Connected"

# Run seed files in order
run_sql "${SEED_DIR}/seed_skills.sql"
run_sql "${SEED_DIR}/seed_projects.sql"

echo ""
echo "──────────────────────────────────────────────────"
echo "  ✓ Seeding complete!"
echo "  Run the app and embeddings will be generated"
echo "  automatically by the scheduler at startup."
echo "──────────────────────────────────────────────────"
