import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/game',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export async function startKnightGame(payload) {
  const response = await client.post('/start', payload);
  return response.data;
}

export async function validateKnightGame(payload) {
  const response = await client.post('/validate', payload);
  return response.data;
}

export async function fetchKnightLeaderboard() {
  const response = await client.get('/leaderboard');
  return response.data;
}
