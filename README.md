# Project Idea Recommender Agent

An AI agent that recommends personalized portfolio projects to students based on their skills, interests, and career goals.

```
  Student Profile
        │
        ▼
  ┌─────────────┐     ┌──────────────┐     ┌──────────────┐
  │   Profile   │────▶│ Rule-Based   │────▶│   Semantic   │
  │  Analyzer   │     │  Filtering   │     │ Vector Search│
  └─────────────┘     └──────────────┘     └──────────────┘
                                                   │
        ┌──────────────────────────────────────────┘
        ▼
  ┌─────────────┐     ┌──────────────┐     ┌──────────────┐
  │  Skill Gap  │────▶│ LLM Ranking  │────▶│ Explanation  │
  │ Identifier  │     │   (GPT-4o)   │     │  Generator   │
  └─────────────┘     └──────────────┘     └──────────────┘
                                                   │
                                                   ▼
                                     Ranked Recommendations
                                  with Explanations & Skill Gaps
```

---

## Features

- **AI Agent Pipeline** — Orchestrated reasoning with memory, tools, and LLM integration
- **Hybrid Recommendations** — Combines rule-based filtering + semantic search + LLM ranking
- **Skill Gap Analysis** — Identifies missing skills and builds a learning path per project
- **Personalized Explanations** — LLM generates motivating, student-specific explanations
- **AI Chat** — Conversational interface to ask follow-up questions
- **30+ Seed Projects** — Spanning Backend, Frontend, AI/ML, DevOps, Security, System Design
- **Docker Ready** — One-command deployment with `docker-compose up`

---

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17, Spring Boot 3.2            |
| AI          | OpenAI GPT-4o-mini, text-embedding-3-small |
| Database    | MySQL 8.0                           |
| Migration   | Flyway                              |
| Frontend    | React 18, React Router              |
| Deployment  | Docker, Docker Compose, Nginx       |
| Build       | Maven 3.9                           |

---

## Quick Start

```bash
# 1. Configure your OpenAI API key
echo "OPENAI_API_KEY=sk-proj-your-key-here" > .env

# 2. Start all services
cd docker && docker-compose up --build

# 3. Open the app
open http://localhost:3000
```

See [docs/deployment_guide.md](docs/deployment_guide.md) for full setup instructions.

---

## Project Structure

```
project-idea-recommender-agent/
├── src/main/java/com/projectrecommender/
│   ├── agent/
│   │   ├── core/          # AgentOrchestrator, StateManager, AgentTools
│   │   ├── reasoning/     # ProfileAnalyzer, ProjectMatcher, SkillGapIdentifier, ExplanationGenerator
│   │   ├── memory/        # ProfileMemory, InteractionHistory, VectorMemory
│   │   └── prompts/       # LLM prompt files
│   ├── knowledgebase/
│   │   ├── entity/        # JPA entities (Student, Project, Skill, ...)
│   │   ├── repository/    # Spring Data JPA repositories
│   │   ├── vector/        # EmbeddingService, VectorIndexService
│   │   └── service/       # ProjectKnowledgeService, SkillTaxonomyService
│   ├── api/
│   │   ├── controller/    # AgentController, ProfileController, RecommendationController
│   │   ├── dto/           # Request/Response DTOs
│   │   └── exception/     # GlobalExceptionHandler
│   ├── services/          # LLMService, FilterService, RankingService, RecommendationService
│   ├── config/            # OpenAIConfig, AgentConfig, SecurityConfig, WebConfig
│   └── scheduler/         # EmbeddingUpdateScheduler, RecommendationCleanupScheduler
├── src/main/resources/
│   ├── prompts/           # agent_system_prompt.txt, project_ranking_prompt.txt, explanation_prompt.txt
│   └── db/                # Flyway migrations + seed data (30+ projects)
├── frontend/              # React app (ProfileForm, ProjectCard, AgentChat, Dashboard)
├── docker/                # Dockerfile, docker-compose.yml
├── docs/                  # Architecture, API docs, Deployment guide
└── scripts/               # seed_database.sh, generate_embeddings.py, evaluate_recommendations.py
```

---

## How the Agent Works

1. **Profile Analysis** — The agent extracts skills, infers domain focus, and uses GPT to understand career direction.
2. **Rule Filtering** — Projects are filtered by difficulty and domain compatibility.
3. **Semantic Search** — The student's profile is embedded and compared against all project embeddings using cosine similarity.
4. **Project Matching** — Rule-filtered and semantic results are merged into a candidate pool.
5. **Skill Gap Detection** — For each candidate, missing skills are identified and ordered into a learning path.
6. **LLM Ranking** — GPT scores each project on skill alignment, learning value, and portfolio impact.
7. **Hybrid Score** — Final score = 40% skill match + 30% semantic + 30% LLM score.
8. **Explanation** — GPT generates a personal, motivating explanation for each top recommendation.

---

## Documentation

- [Agent Architecture](docs/agent_architecture.md)
- [API Documentation](docs/api_documentation.md)
- [Deployment Guide](docs/deployment_guide.md)

---

## License

MIT License — See [LICENSE](LICENSE) file.
