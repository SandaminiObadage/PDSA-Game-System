# Sixteen Queens API (Backtracking)

## 1. Run solve comparison (sequential vs threaded)

POST `/api/games/sixteen-queens/solve`

Request:
```json
{
  "boardSize": 16,
  "threadCount": 8,
  "solutionSampleLimit": 50,
  "persistSolutionLimit": 500
}
```

Response fields:
- `sequentialSolutionCount`
- `parallelSolutionCount`
- `sequentialTimeMs`
- `parallelTimeMs`
- `speedup`
- `sampleSolutions`
- `persistedSolutionCount`

`persistSolutionLimit` controls how many discovered solutions are saved as known answers in DB for this puzzle.

## 2. Submit player answer

POST `/api/games/sixteen-queens/submit`

Request:
```json
{
  "playerName": "Sandamini",
  "gameRoundId": 1,
  "boardSize": 16,
  "answer": "0,2,4,1,3,8,10,12,14,5,7,9,11,13,15,6"
}
```

For `answer`, each value is the column index for row 0..N-1.

Behavior:
- invalid solution -> incorrect
- valid but duplicate -> already recognized
- valid new -> accepted and recognized

`gameRoundId` can be taken from `/solve` response. If omitted, backend uses the latest Sixteen Queens round.

## 3. Get DB history

GET `/api/games/sixteen-queens/history?limit=10`

Returns:
- recent game rounds
- sequential and threaded timings
- recent player answers

Use this endpoint for database screenshots and report evidence.

## 4. Leaderboard

GET `/api/games/sixteen-queens/leaderboard?limit=10`

Returns ranked players by recognized unique solutions, then correct answers.

## 5. Report Summary

GET `/api/games/sixteen-queens/report`

Returns aggregate metrics:
- total rounds
- average sequential vs threaded times
- average speedup
- total known persisted solutions
- total/active recognized solutions
- answer statistics
