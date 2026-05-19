# Project Idea Recommender System

An AI-powered platform that recommends personalized portfolio projects to students based on their skills, career goals, and experience level.

## Features
- **Hybrid Scoring Engine**: Combines rule-based filtering, semantic search (OpenAI Embeddings), and LLM reasoning (GPT-4o-mini).
- **Skill Gap Analysis**: Identifies missing skills for each recommended project.
- **Explainable Recommendations**: Provides natural language reasons for each match.
- **Premium UI**: Modern, responsive React frontend with glassmorphism and animations.

## Tech Stack
- **Backend**: Java 17, Spring Boot, MySQL
- **Frontend**: React, Tailwind CSS, Framer Motion
- **AI**: OpenAI GPT-4o-mini, text-embedding-3-small
- **Deployment**: Docker, Docker Compose

## Setup Instructions

### Prerequisites
- Docker and Docker Compose
- OpenAI API Key

### Running with Docker
1. Clone the repository.
2. Update the `.env` file with your `OPENAI_API_KEY`.
3. Run the following command from the root directory:
   ```bash
   docker-compose -f docker/docker-compose.yml up --build
   ```
4. Access the frontend at `http://localhost:80` and backend at `http://localhost:8080`.

### Local Development
**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Architecture
The project follows a Hexagonal Architecture (Ports and Adapters) to ensure separation of concerns and testability.
- `core`: Domain models and business logic ports.
- `infrastructure`: Concrete implementations of persistence, AI adapters, and services.
- `api`: REST controllers and DTOs.
