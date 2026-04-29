# Agent Architecture

## Overview

The Project Idea Recommender Agent is a hybrid AI system that combines rule-based filtering, semantic vector search, and LLM reasoning to recommend personalized portfolio projects to students.

---

## Architecture Layers

```
┌──────────────────────────────────────────────────────────────┐
│                         React Frontend                        │
│         ProfileForm │ ProjectCard │ AgentChat │ Dashboard     │
└──────────────────────────┬───────────────────────────────────┘
                           │ REST API (JSON)
┌──────────────────────────▼───────────────────────────────────┐
│                       API Layer (Spring Boot)                 │
│     AgentController │ ProfileController │ RecommendationCtrl  │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                      Agent Orchestrator                       │
│   Coordinates the full reasoning pipeline step by step        │
└───┬──────────┬───────────┬──────────┬───────────┬────────────┘
    │          │           │          │           │
    ▼          ▼           ▼          ▼           ▼
Profile   Project      SkillGap  Explanation  Agent
Analyzer  Matcher    Identifier  Generator    Tools
    │          │                               │
    │    ┌─────┴──────────┐           ┌────────┴─────┐
    │    │  Rule Filter   │           │ Rank Projects│
    │    │  Domain Filter │           │ Search Proj. │
    │    │  Diff. Filter  │           └──────────────┘
    │    └────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────────┐
│                       Memory Layer                            │
│       ProfileMemory │ InteractionHistory │ VectorMemory       │
└──────────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────────┐
│                      Services Layer                           │
│   LLMService (OpenAI) │ FilterService │ RankingService        │
│   EmbeddingFacadeService                                      │
└──────────────────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────────────────────────┐
│                     Knowledge Base                            │
│   ProjectRepository │ SkillRepository │ VectorIndexService    │
│   MySQL Database    │ In-Memory Vector Index                  │
└──────────────────────────────────────────────────────────────┘
```

---

## Recommendation Pipeline (Step-by-Step)

```
Student Profile Input
        │
        ▼
[Step 1] Profile Analyzer
  → Extracts skill names, experience level, domain focus
  → LLM generates career direction insights
  → Stores in ProfileMemory
        │
        ▼
[Step 2] Rule-Based Filtering
  → Filter by difficulty (BEGINNER/INTERMEDIATE/ADVANCED)
  → Filter by domain interests
  → Removes incompatible projects early
        │
        ▼
[Step 3] Semantic Vector Search (VectorIndexService)
  → Build query text from profile
  → Compute cosine similarity against all project embeddings
  → Returns top-K semantically relevant projects
        │
        ▼
[Step 4] Project Matching (ProjectMatcher)
  → Merges rule-filtered + semantic search results
  → Computes skill match scores for each candidate
        │
        ▼
[Step 5] Skill Gap Identification (SkillGapIdentifier)
  → Compare required skills vs student skills
  → Builds ordered learning path using SkillTaxonomyService
        │
        ▼
[Step 6] LLM Ranking (RankingService + LLMService)
  → Sends top candidates to GPT with student profile
  → GPT scores each project (skill alignment, learning value, portfolio impact)
  → Combined with rule-based and semantic scores (weighted average)
        │
        ▼
[Step 7] Explanation Generation (ExplanationGenerator)
  → LLM generates personalized, motivating explanation per project
  → Falls back to template explanation if LLM fails
        │
        ▼
[Step 8] Final Response
  → Returns ranked list with scores, explanations, skill gaps, learning paths
  → Persisted to RecommendationHistory
  → Recorded in StateManager and InteractionHistory
```

---

## Hybrid Scoring Formula

```
finalScore = (skillMatchScore × 0.40)
           + (semanticScore    × 0.30)
           + (llmScore         × 0.30)
```

All three dimensions contribute to the final ranking, ensuring both objective skill matching and subjective career-relevance are captured.

---

## Memory System

| Component           | Type       | Purpose                                        |
|---------------------|------------|------------------------------------------------|
| ProfileMemory       | In-memory  | Caches analyzed profiles across sessions       |
| InteractionHistory  | In-memory  | Tracks interaction count per student           |
| VectorMemory        | In-memory  | Stores arbitrary embedding lookups             |
| StateManager        | In-memory  | Per-session agent state and message history    |
| RecommendationHistory | MySQL    | Persists all recommendations and feedback      |

---

## Scheduled Tasks

| Task                          | Schedule          | Purpose                            |
|-------------------------------|-------------------|------------------------------------|
| EmbeddingUpdateScheduler      | Daily at 02:00 AM | Generate missing project embeddings |
| VectorIndex Reload            | Daily at 03:00 AM | Refresh in-memory vector index     |
| RecommendationCleanupScheduler| Weekly (Sunday)   | Delete old history records         |
| Session Cleanup               | Every 1 hour      | Remove stale in-memory sessions    |
| Profile Memory Eviction       | Daily at 03:30 AM | Evict old cached profiles          |
