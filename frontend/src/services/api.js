import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 60000,
});

// ── Profile API ────────────────────────────────────────────────────────────────

export const createProfile = (profileData) =>
  api.post('/profile/create', profileData).then((r) => r.data);

export const getProfile = (id) =>
  api.get(`/profile/${id}`).then((r) => r.data);

export const updateProfile = (id, profileData) =>
  api.put(`/profile/${id}`, profileData).then((r) => r.data);

export const listProfiles = () =>
  api.get('/profile').then((r) => r.data);

// ── Recommendation API ────────────────────────────────────────────────────────

export const getRecommendations = (payload) =>
  api.post('/recommendations', payload).then((r) => r.data);

export const getRecommendationHistory = (studentId) =>
  api.get(`/recommendations/history/${studentId}`).then((r) => r.data);

export const submitFeedback = (historyId, feedback) =>
  api.post(`/recommendations/feedback/${historyId}`, { feedback }).then((r) => r.data);

export const getSkillGap = (studentId, projectId) =>
  api.get(`/recommendations/skill-gap/${studentId}/${projectId}`).then((r) => r.data);

// ── Agent Chat API ────────────────────────────────────────────────────────────

export const sendChatMessage = (sessionId, studentId, message) =>
  api.post('/agent/chat', { sessionId, studentId, message }).then((r) => r.data);

export const clearSession = (sessionId) =>
  api.delete(`/agent/session/${sessionId}`).then((r) => r.data);

export const agentHealth = () =>
  api.get('/agent/health').then((r) => r.data);

export default api;
