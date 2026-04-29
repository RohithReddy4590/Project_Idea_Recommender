# Project Idea Recommender — Frontend

React 18 dashboard for the AI Project Recommender Agent.

## Pages
- **Dashboard** — Create your student profile and trigger recommendations
- **Recommendations** — View ranked project cards with AI explanations and skill gap analysis
- **AI Chat** — Conversational interface to ask the agent follow-up questions

## Development

```bash
npm install
npm start       # http://localhost:3000
npm run build   # production build
```

The app proxies `/api/*` to `http://localhost:8080` during development (see `package.json`).
