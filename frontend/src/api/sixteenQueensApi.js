import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/sixteen-queens',
  timeout: 300000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export async function solveSixteenQueens(payload) {
  const response = await client.post('/solve', payload);
  return response.data;
}

export async function fetchSixteenQueensSamples(roundId, limit = 8, viewerRole = 'PLAYER') {
  const query = roundId
    ? `/samples?roundId=${roundId}&limit=${limit}&viewerRole=${encodeURIComponent(viewerRole)}`
    : `/samples?limit=${limit}&viewerRole=${encodeURIComponent(viewerRole)}`;
  const response = await client.get(query);
  return response.data;
}

export async function closeSixteenQueensRound(roundId) {
  const query = roundId ? `/close-round?roundId=${roundId}` : '/close-round';
  const response = await client.post(query);
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

export async function fetchSixteenQueensLeaderboard(limit = 10, roundId) {
  const query = roundId ? `/leaderboard?limit=${limit}&roundId=${roundId}` : `/leaderboard?limit=${limit}`;
  const response = await client.get(query);
  return response.data;
}

export async function fetchSixteenQueensReport() {
  const response = await client.get('/report');
  return response.data;
}

export async function resetSixteenQueensRecognized(roundId) {
  const query = roundId ? `/reset-recognized?roundId=${roundId}` : '/reset-recognized';
  const response = await client.post(query);
  return response.data;
}
