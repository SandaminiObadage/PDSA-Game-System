import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/sixteen-queens',
  timeout: 300000,
  headers: {
    'Content-Type': 'application/json'
  }
});

async function requestWithRetry(factory, retries = 1) {
  let attempt = 0;
  let lastError;

  while (attempt <= retries) {
    try {
      return await factory();
    } catch (error) {
      lastError = error;
      const shouldRetry = !error?.response || error?.code === 'ECONNABORTED';
      if (!shouldRetry || attempt === retries) {
        throw error;
      }
    }
    attempt += 1;
  }

  throw lastError;
}

export async function solveSixteenQueens(payload) {
  const response = await requestWithRetry(() => client.post('/solve', payload), 1);
  return response.data;
}

export async function fetchSixteenQueensSamples(roundId, limit = 8, viewerRole = 'PLAYER') {
  const query = roundId
    ? `/samples?roundId=${roundId}&limit=${limit}&viewerRole=${encodeURIComponent(viewerRole)}`
    : `/samples?limit=${limit}&viewerRole=${encodeURIComponent(viewerRole)}`;
  const response = await requestWithRetry(() => client.get(query), 1);
  return response.data;
}

export async function closeSixteenQueensRound(roundId) {
  const query = roundId ? `/close-round?roundId=${roundId}` : '/close-round';
  const response = await requestWithRetry(() => client.post(query), 1);
  return response.data;
}

export async function submitSixteenQueens(payload) {
  const response = await requestWithRetry(() => client.post('/submit', payload), 1);
  return response.data;
}

export async function fetchSixteenQueensHistory(limit = 10) {
  const response = await requestWithRetry(() => client.get(`/history?limit=${limit}`), 1);
  return response.data;
}

export async function fetchSixteenQueensLeaderboard(limit = 10, roundId, scope = 'CURRENT') {
  const params = new URLSearchParams();
  params.set('limit', String(limit));

  if (roundId) {
    params.set('roundId', String(roundId));
  }

  if (scope) {
    params.set('scope', scope);
  }

  const query = `/leaderboard?${params.toString()}`;
  const response = await requestWithRetry(() => client.get(query), 1);
  return response.data;
}

export async function fetchSixteenQueensReport() {
  const response = await requestWithRetry(() => client.get('/report'), 1);
  return response.data;
}

export async function resetSixteenQueensRecognized(roundId) {
  const query = roundId ? `/reset-recognized?roundId=${roundId}` : '/reset-recognized';
  const response = await requestWithRetry(() => client.post(query), 1);
  return response.data;
}
