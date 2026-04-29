# Deployment Guide

## Prerequisites

- Docker 24+ and Docker Compose v2
- An OpenAI API key (get one at https://platform.openai.com/api-keys)
- Port 3000 and 8080 free on your machine

---

## Quick Start (Docker Compose)

### 1. Clone the project
```bash
git clone <repo-url>
cd project-idea-recommender-agent
```

### 2. Configure environment
```bash
cp .env .env.local    # optional: edit for custom values
```

Edit `.env` and set your `OPENAI_API_KEY`:
```
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
DB_PASSWORD=mysecurepassword
```

### 3. Start all services
```bash
cd docker
docker-compose up --build
```

This starts:
| Service   | URL                          |
|-----------|------------------------------|
| Frontend  | http://localhost:3000        |
| Backend   | http://localhost:8080/api    |
| MySQL     | localhost:3306               |

### 4. Seed the database
The seed SQL files are auto-loaded by MySQL on first startup via `docker-entrypoint-initdb.d`.

To manually re-seed:
```bash
chmod +x scripts/seed_database.sh
./scripts/seed_database.sh localhost 3306 recommender_db root rootpassword
```

### 5. Verify the backend is running
```bash
curl http://localhost:8080/api/agent/health
# Expected: {"status":"UP","agent":"Project Idea Recommender Agent"}
```

---

## Local Development (Without Docker)

### Backend (Spring Boot)

**Requirements:** JDK 17, Maven 3.9+, MySQL 8

```bash
# 1. Create database
mysql -u root -p -e "CREATE DATABASE recommender_db;"

# 2. Configure application-dev.yml with your MySQL credentials

# 3. Set your OpenAI API key
export OPENAI_API_KEY=sk-proj-xxxxxxx

# 4. Run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend (React)

**Requirements:** Node 20+

```bash
cd frontend
npm install
npm start
# Opens http://localhost:3000
```

---

## Environment Variables Reference

| Variable        | Required | Default           | Description                  |
|-----------------|----------|-------------------|------------------------------|
| OPENAI_API_KEY  | YES      | —                 | Your OpenAI API key          |
| DB_HOST         | No       | localhost         | MySQL host                   |
| DB_PORT         | No       | 3306              | MySQL port                   |
| DB_NAME         | No       | recommender_db    | Database name                |
| DB_USER         | No       | root              | Database user                |
| DB_PASSWORD     | No       | root              | Database password            |
| SPRING_PROFILE  | No       | dev               | `dev` or `prod`              |

---

## Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=FilterServiceTest

# Skip tests during build
./mvnw package -DskipTests
```

---

## Evaluating Recommendation Quality

```bash
# Make sure the app is running, then:
python3 scripts/evaluate_recommendations.py --base-url http://localhost:8080/api
```

This creates 3 test profiles and measures recommendation count, scores, and response time.

---

## Stopping Services

```bash
cd docker
docker-compose down          # stop containers
docker-compose down -v       # stop + delete MySQL data volume
```
