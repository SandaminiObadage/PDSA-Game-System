# Traffic Simulation Game - API Reference

## Base URL
```
http://localhost:8080/api/games/traffic-simulation
```

## Authentication
Currently no authentication required. All endpoints are public.

---

## Endpoints

### 1. Generate New Game Round

**Endpoint**: `POST /new-round`

**Description**: Generates a new traffic network with random edge capacities and computes the maximum flow using both algorithms.

**Request**:
```
POST /api/games/traffic-simulation/new-round
Content-Type: application/json
```

No request body required.

**Response** (200 OK):
```json
{
  "roundId": "1",
  "networkData": {
    "nodes": ["A", "B", "C", "D", "E", "F", "G", "H", "T"],
    "source": "A",
    "sink": "T",
    "edges": [
      {
        "from": "A",
        "to": "B",
        "capacity": 12
      },
      {
        "from": "A",
        "to": "C",
        "capacity": 8
      },
      // ... more edges
    ]
  },
  "fordFulkersonResult": {
    "maxFlow": 24,
    "executionTimeMs": 1.25,
    "algorithmName": "Ford-Fulkerson"
  },
  "dinicResult": {
    "maxFlow": 24,
    "executionTimeMs": 0.87,
    "algorithmName": "Dinic's Algorithm"
  },
  "createdAt": 1704067200000
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "error": "Failed to generate new round",
  "message": "Detailed error message"
}
```

**Notes**:
- `roundId` is a unique identifier for this game round
- Edge capacities are randomly generated between 5-15 vehicles/minute
- Both algorithms should produce the same `maxFlow` value
- `executionTimeMs` shows the speed difference between algorithms

---

### 2. Submit Player Answer

**Endpoint**: `POST /submit`

**Description**: Validates player's answer and saves it to the database.

**Request**:
```
POST /api/games/traffic-simulation/submit?roundId=1
Content-Type: application/json

{
  "playerName": "Alice Johnson",
  "playerAnswer": 24
}
```

**Query Parameters**:
- `roundId` (required): The ID of the game round to submit for

**Request Body**:
```json
{
  "playerName": "string (1-50 chars)",
  "playerAnswer": "integer (>= 0)"
}
```

**Response** (200 OK):
```json
{
  "isCorrect": true,
  "correctAnswer": 24,
  "message": "Correct! 🎉",
  "algorithmExecutionTimeMs": 1.25
}
```

**Response** (200 OK - Incorrect Answer):
```json
{
  "isCorrect": false,
  "correctAnswer": 24,
  "message": "Incorrect. The correct answer is 24",
  "algorithmExecutionTimeMs": 1.25
}
```

**Error Response** (400 Bad Request):
```json
{
  "error": "Invalid request",
  "message": "Player name cannot be blank"
}
```

**Error Response** (404 Not Found):
```json
{
  "error": "Round not found",
  "message": "The specified game round does not exist"
}
```

**Validation Rules**:
- `playerName`: Must be 1-50 characters, non-empty
- `playerAnswer`: Must be non-negative integer
- `roundId`: Must reference an existing game round

**Notes**:
- Player is created if they don't exist
- Answer is recorded in database with correctness flag
- Algorithm execution time is from Ford-Fulkerson algorithm
- Same player can submit multiple times (all attempts recorded)

---

### 3. Get Leaderboard

**Endpoint**: `GET /leaderboard`

**Description**: Retrieves top players ranked by correct answers and average algorithm execution time.

**Request**:
```
GET /api/games/traffic-simulation/leaderboard?limit=10
```

**Query Parameters**:
- `limit` (optional, default=10): Number of top players to return

**Response** (200 OK):
```json
{
  "entries": [
    {
      "playerName": "Alice",
      "correctAnswers": 8,
      "averageExecutionTimeMs": 1.15,
      "lastPlayedAt": 1704153600000
    },
    {
      "playerName": "Bob",
      "correctAnswers": 7,
      "averageExecutionTimeMs": 1.42,
      "lastPlayedAt": 1704067200000
    },
    // ... more entries up to limit
  ],
  "generatedAt": 1704240000000
}
```

**Response Fields**:
- `playerName`: Player's name
- `correctAnswers`: Total number of correct submissions
- `averageExecutionTimeMs`: Average algorithm execution time across all attempts
- `lastPlayedAt`: Timestamp of last submission (milliseconds since epoch)

**Error Response** (500 Internal Server Error):
```json
{
  "error": "Failed to fetch leaderboard",
  "message": "Database query error"
}
```

**Sorting Order**:
1. Primary: `correctAnswers` (descending)
2. Secondary: `averageExecutionTimeMs` (ascending)

**Notes**:
- Leaderboard only includes players with at least one submission
- Times are in milliseconds
- Timestamps are Unix epoch in milliseconds

---

### 4. Health Check

**Endpoint**: `GET /health`

**Description**: Basic health check endpoint to verify API is running.

**Request**:
```
GET /api/games/traffic-simulation/health
```

**Response** (200 OK):
```json
{
  "status": "OK",
  "game": "Traffic Simulation"
}
```

**Error Response** (503 Service Unavailable):
```json
{
  "status": "ERROR",
  "message": "Service unavailable"
}
```

**Notes**:
- Use this to verify backend is running
- No authentication required

---

## Data Models

### TrafficSolveRequest
```json
{
  "source": "A",
  "sink": "T",
  "threadCount": 1
}
```

### MaxFlowComparisonResponse
```json
{
  "roundId": "1",
  "networkData": { /* ... */ },
  "fordFulkersonResult": { /* ... */ },
  "dinicResult": { /* ... */ },
  "createdAt": 1704067200000
}
```

### SubmitTrafficAnswerRequest
```json
{
  "playerName": "Alice",
  "playerAnswer": 24
}
```

### SubmitTrafficAnswerResponse
```json
{
  "isCorrect": true,
  "correctAnswer": 24,
  "message": "Correct! 🎉",
  "algorithmExecutionTimeMs": 1.25
}
```

### TrafficLeaderboardEntry
```json
{
  "playerName": "Alice",
  "correctAnswers": 8,
  "averageExecutionTimeMs": 1.15,
  "lastPlayedAt": 1704153600000
}
```

### TrafficLeaderboardResponse
```json
{
  "entries": [ /* array of TrafficLeaderboardEntry */ ],
  "generatedAt": 1704240000000
}
```

---

## Error Codes

| Status | Error | Cause |
|--------|-------|-------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid input parameters |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server-side error |
| 503 | Service Unavailable | Service is down |

---

## Common Usage Patterns

### Workflow 1: Play a New Round
```
1. POST /new-round → Get roundId and network data
2. Display network to player
3. Wait for player input
4. POST /submit?roundId={roundId} with player answer
5. Display result to player
```

### Workflow 2: Show Leaderboard
```
1. GET /leaderboard?limit=10
2. Display top 10 players
3. Optional: Call periodically to refresh
```

### Workflow 3: Monitor Service Health
```
1. GET /health
2. If error, show offline message
3. Retry connection after delay
```

---

## Rate Limiting

Currently no rate limiting is implemented. Production deployment should add:
- Per-IP rate limiting
- Per-player submission limits
- Database connection pooling

---

## CORS Configuration

For frontend hosted on different domain, add CORS headers:
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

Current configuration assumes frontend and backend on same host.

---

## Versioning

**Current Version**: 1.0

**API Stability**: Stable

Future changes will use URL versioning: `/api/v2/games/traffic-simulation/...`

---

## Examples

### cURL - Generate New Round
```bash
curl -X POST http://localhost:8080/api/games/traffic-simulation/new-round \
  -H "Content-Type: application/json"
```

### cURL - Submit Answer
```bash
curl -X POST "http://localhost:8080/api/games/traffic-simulation/submit?roundId=1" \
  -H "Content-Type: application/json" \
  -d '{"playerName": "Alice", "playerAnswer": 24}'
```

### cURL - Get Leaderboard
```bash
curl http://localhost:8080/api/games/traffic-simulation/leaderboard?limit=10
```

### JavaScript Fetch - Generate Round
```javascript
const response = await fetch('http://localhost:8080/api/games/traffic-simulation/new-round', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
});
const data = await response.json();
console.log(data.roundId);
```

### JavaScript Fetch - Submit Answer
```javascript
const response = await fetch(`http://localhost:8080/api/games/traffic-simulation/submit?roundId=1`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    playerName: 'Alice',
    playerAnswer: 24
  })
});
const result = await response.json();
console.log(result.isCorrect ? 'Correct!' : 'Wrong');
```

---

## Support

For API issues, check:
- Server logs: `backend/logs/`
- Database: `pdsa.db`
- Test file: `backend/src/test/java/com/nibm/pdsa/games/traffic/`
