import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/sixteen-queens',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export async function solveSixteenQueens(payload) {
  const response = await client.post('/solve', payload);
  return response.data;
}

export async function submitSixteenQueens(payload) {
  const response = await client.post('/submit', payload);
  return response.data;
}

export async function fetchSixteenQueensHistory(limit = 10) {
  const response = await client.get(`/history?limit=${limit}`);
  return response.data;
}

export async function fetchSixteenQueensLeaderboard(limit = 10) {
  const response = await client.get(`/leaderboard?limit=${limit}`);
  return response.data;
}

export async function fetchSixteenQueensReport() {
  const response = await client.get('/report');
  return response.data;
}
