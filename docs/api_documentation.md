# API Documentation

Base URL: `http://localhost:8080/api`

All endpoints return JSON. Error responses follow the format:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Student not found: 99"
}
```

---

## Agent Endpoints

### `POST /agent/chat`
Send a conversational message to the AI agent within a session.

**Request Body:**
```json
{
  "sessionId": "session-1-1700000000",
  "studentId": 1,
  "message": "Which project should I build first?"
}
```

**Response:**
```json
{
  "sessionId": "session-1-1700000000",
  "response": "Based on your Java and Spring Boot skills, I recommend starting with the Finance Tracker API...",
  "interactionCount": 2
}
```

---

### `GET /agent/session/{sessionId}`
Get the current state of an agent session.

**Response:**
```json
{
  "sessionId": "session-1-1700000000",
  "interactionCount": 3,
  "messageCount": 6,
  "hasProfile": true
}
```

---

### `DELETE /agent/session/{sessionId}`
Clear an agent session and free memory.

**Response:** `200 OK` `{ "message": "Session cleared: session-1-1700000000" }`

---

### `GET /agent/health`
Health check endpoint.

**Response:** `{ "status": "UP", "agent": "Project Idea Recommender Agent" }`

---

## Profile Endpoints

### `POST /profile/create`
Create a new student profile.

**Request Body:**
```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "careerGoal": "Senior Backend Engineer",
  "domainInterests": "Backend Development, DevOps",
  "experienceLevel": "INTERMEDIATE",
  "skills": [
    { "skillName": "Java", "proficiencyLevel": "ADVANCED", "yearsExperience": 3.0 },
    { "skillName": "Spring Boot", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0 },
    { "skillName": "MySQL", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0 }
  ]
}
```

**Response:** `201 Created` — Returns the created Student entity.

---

### `GET /profile/{id}`
Get a student profile by ID (includes skills).

**Response:** Student entity with `studentSkills` populated.

---

### `PUT /profile/{id}`
Update an existing student profile.

---

### `GET /profile`
List all student profiles.

---

### `DELETE /profile/{id}`
Delete a student profile (cascades to skills and history).

---

## Recommendation Endpoints

### `POST /recommendations`
Generate personalized project recommendations using the full AI agent pipeline.

**Request Body:**
```json
{
  "studentId": 1,
  "sessionId": "session-1-1700000000",
  "maxResults": 10,
  "preferredDomains": ["Backend Development", "AI/ML"]
}
```

**Response:** Array of `RecommendationResponse` objects:
```json
[
  {
    "project": {
      "id": 5,
      "title": "AI Resume Analyzer",
      "description": "...",
      "domain": "AI/ML",
      "difficulty": "INTERMEDIATE",
      "techStack": "Java, Spring Boot, React, OpenAI API, MySQL",
      "skillsRequired": "Java, Spring Boot, React, OpenAI API, REST API",
      "estimatedHours": 100
    },
    "finalScore": 0.87,
    "skillMatchScore": 0.80,
    "semanticScore": 0.90,
    "llmScore": 0.91,
    "readinessScore": 0.80,
    "explanation": "With your Java and Spring Boot skills, you're well-equipped...",
    "skillGaps": ["React", "OpenAI API"],
    "learningPath": ["React", "OpenAI API"],
    "sessionId": "session-1-1700000000",
    "matchPercentage": 80
  }
]
```

---

### `GET /recommendations/history/{studentId}`
Get recommendation history for a student, ordered by date descending.

---

### `POST /recommendations/feedback/{historyId}`
Submit feedback on a recommendation.

**Request Body:**
```json
{ "feedback": "LIKED" }
```

Feedback values: `LIKED`, `DISLIKED`, `NEUTRAL`, `NOT_REVIEWED`

---

### `GET /recommendations/skill-gap/{studentId}/{projectId}`
Get detailed skill gap analysis between a student and a specific project.

**Response:**
```json
{
  "projectId": 5,
  "projectTitle": "AI Resume Analyzer",
  "missingSkills": ["React", "OpenAI API"],
  "learningPath": ["React", "OpenAI API"],
  "readinessScore": 0.60,
  "readyNow": false,
  "readinessPercentage": 60
}
```

---

## Experience Level Values
- `BEGINNER` — 0–1 year
- `INTERMEDIATE` — 1–3 years
- `ADVANCED` — 3+ years

## Difficulty Values
- `BEGINNER`, `INTERMEDIATE`, `ADVANCED`

## Proficiency Level Values
- `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`
