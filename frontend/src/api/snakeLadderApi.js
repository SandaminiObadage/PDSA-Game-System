import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api/games/snake-ladder',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export async function solveSnakeLadder(payload) {
  const response = await client.post('/solve', payload);
  return response.data;
}

export async function submitSnakeLadder(payload) {
  const response = await client.post('/submit', payload);
  return response.data;
}