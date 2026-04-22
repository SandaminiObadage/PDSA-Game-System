import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/minimum-cost',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Get a new game matrix and costs
export async function getMinimumCostGame(taskCount) {
  const query = Number.isFinite(taskCount) ? `?tasks=${taskCount}` : '';
  const response = await client.get(`/game${query}`);
  return response.data;
}

// Submit a game result
export async function submitMinimumCostScore(payload) {
  const response = await client.post('/score', payload);
  return response.data;
}

// Get leaderboard (recent results)
export async function fetchMinimumCostLeaderboard(limit = 20) {
  const response = await client.get(`/leaderboard?limit=${limit}`);
  return response.data;
}

// Get all results
export async function fetchMinimumCostAllResults(limit = 100) {
  const response = await client.get(`/all-results?limit=${limit}`);
  return response.data;
}

// Get player history
export async function fetchMinimumCostPlayerHistory(playerName, limit = 10) {
  const response = await client.get(`/player-history?playerName=${encodeURIComponent(playerName)}&limit=${limit}`);
  return response.data;
}

// Health check
export async function checkMinimumCostBackend() {
  const response = await client.get('/health');
  return response.data;
}