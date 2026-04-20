// Traffic Simulation Game API endpoints

const API_BASE_URL = 'http://localhost:8080/api/games/traffic-simulation';

/**
 * Generate a new game round
 */
export async function generateNewRound() {
  const response = await fetch(`${API_BASE_URL}/new-round`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error(`Failed to generate new round: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Submit player answer
 */
export async function submitAnswer(playerName, playerAnswer, roundId) {
  const response = await fetch(`${API_BASE_URL}/submit?roundId=${roundId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      playerName,
      playerAnswer
    })
  });

  if (!response.ok) {
    throw new Error(`Failed to submit answer: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Get leaderboard
 */
export async function fetchLeaderboard(limit = 10) {
  const response = await fetch(`${API_BASE_URL}/leaderboard?limit=${limit}`);

  if (!response.ok) {
    throw new Error(`Failed to fetch leaderboard: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Health check
 */
export async function healthCheck() {
  const response = await fetch(`${API_BASE_URL}/health`);
  return response.ok;
}
