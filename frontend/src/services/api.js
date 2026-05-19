import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const recommendationService = {
  getRecommendations: async (data) => {
    const response = await api.post('/recommendations', data);
    return response.data;
  },
};

export default api;
