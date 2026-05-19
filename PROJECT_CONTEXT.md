# Project Idea Recommender System - Comprehensive Context

This document provides a complete overview of the "Project Idea Recommender System" codebase, architecture, and technology stack. It is designed to give LLMs (like ChatGPT) full context of the project.

## 1. Project Overview
The **Project Idea Recommender System** is an AI-powered platform designed to recommend personalized portfolio projects to students. It takes a student's skills, career goals, and experience level, and provides curated project recommendations with dynamic explanations and skill gap analysis. It features a premium modern UI with dynamic animations.

## 2. Technology Stack
*   **Backend**: Java 17, Spring Boot, Spring Data JPA, REST API.
*   **Frontend**: React 19, Vite, Tailwind CSS v4, Framer Motion, Lucide React, Axios. 
*   **Database**: MySQL (accessed via Hibernate/JPA).
*   **AI Integration**: Google Gemini API (recently migrated from OpenAI).
*   **Deployment**: Docker and Docker Compose.

## 3. System Architecture
The backend rigorously follows **Hexagonal Architecture (Ports and Adapters)** to decouple core business logic from external dependencies (DB, AI APIs, REST controllers).
*   `core/`: Contains Domain models (`Project`, `Student`, `LlmAnalysisResponse`) and inbound/outbound ports (interfaces like `OpenAIServicePort`, `EmbeddingServicePort`).
*   `infrastructure/`: Concrete implementations of adapters (e.g., `GeminiAdapter`, `EmbeddingAdapter`) and persistence (database) logic.
*   `api/`: REST controllers managing HTTP requests and DTOs mapping.

## 4. AI & Recommendation Pipeline
The core feature is a **Hybrid Scoring Engine** that combines multiple strategies to find the perfect project match:
1.  **Rule-Based Filtering**: The backend initially filters the project database (50+ curated projects) based on baseline compatibility (e.g., difficulty, experience level).
2.  **Semantic Matching**: Uses Google Gemini's `gemini-embedding-2` to convert the user's career goals and project descriptions into 768-dimensional vector embeddings, calculating cosine similarity.
3.  **Hybrid Scoring Math**:
    *   **40% Weight**: Skill match (Jaccard similarity between student skills and required project skills).
    *   **30% Weight**: Semantic similarity (Cosine similarity from embeddings).
    *   **30% Weight**: LLM evaluation score.
4.  **LLM Reasoning**: Uses `gemini-2.5-flash` via the `GeminiAdapter`. The LLM scores the top candidate projects (0-10), generates a natural language explanation detailing why the project fits the student's profile, and identifies missing skills (Skill Gap Analysis).
5.  **Dynamic Generation**: If static projects are insufficient, the system leverages `gemini-2.5-flash` to dynamically generate brand new, unique project ideas formatted directly as JSON.

## 5. Directory Structure
*   `backend/`: The Spring Boot application.
    *   `src/main/java/com/projectrecommender/`: Root Java package.
    *   `src/main/resources/application.yml`: Application configuration (defines DB credentials and `${GEMINI_API_KEY}`).
*   `frontend/`: The React application built with Vite.
    *   `package.json`: Contains Vite, Tailwind, and Framer Motion dependencies.
*   `docs/`: Contains architecture documentation.
*   `docker/`: Contains `docker-compose.yml` for local containerized deployment.
*   `.env`: Environment variables including API keys.

## 6. Recent Migrations (Important Context)
The project originally utilized OpenAI (`gpt-4o-mini` and `text-embedding-3-small`). **It was recently migrated entirely to Google Gemini**. 
*   **LLM Service**: Now uses `gemini-2.5-flash:generateContent` inside `GeminiAdapter.java`.
*   **Embeddings Service**: Now uses `gemini-embedding-2:embedContent` inside `EmbeddingAdapter.java`.
*(Note: Some legacy documentation files like `README.md` and `ARCHITECTURE.md` might still reference OpenAI, but the codebase execution is 100% migrated to Gemini).*

## 7. How to Run Locally
**Docker Setup**:
Ensure Docker is running and your `.env` file contains `GEMINI_API_KEY`.
```bash
docker-compose -f docker/docker-compose.yml up --build
```
*   Frontend available at: `http://localhost:80`
*   Backend available at: `http://localhost:8080`

**Standalone Development**:
*   **Backend**: `cd backend && mvn spring-boot:run`
*   **Frontend**: `cd frontend && npm install && npm run dev`
