import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/minimum-cost',
  timeout: 300000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export async function startMinimumCostRound() {
  const response = await client.post('/game/start');
  return response.data;
}

export async function fetchMinimumCostRound(roundId) {
  const response = await client.get(`/game/${roundId}`);
  return response.data;
}

export async function submitMinimumCostSolution(payload) {
  const response = await client.post('/submit', payload);
  return response.data;
}

export async function fetchMinimumCostLeaderboard(limit = 10, roundId) {
  const query = roundId ? `/leaderboard?limit=${limit}&roundId=${roundId}` : `/leaderboard?limit=${limit}`;
  const response = await client.get(query);
  return response.data;
}

export async function fetchMinimumCostHistory(limit = 10) {
  const response = await client.get(`/history?limit=${limit}`);
  return response.data;
}

export async function fetchMinimumCostReport() {
  const response = await client.get('/report');
  return response.data;
}