# PDSA Game System

Monorepo scaffold for:
- frontend (React)
- backend (Java Spring Boot)
- database (SQL scripts)

## Implemented Module: Sixteen Queens (Backtracking)

Backend includes a working Sixteen Queens game module with:
- Sequential bitmask backtracking solver
- Threaded (parallel) bitmask backtracking solver
- Timing comparison between both approaches
- Player answer validation and duplicate-recognition behavior
- Unit tests

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

## Run Tests

```bash
cd backend
mvn test
```

## API Endpoints

### 1) Compare sequential vs threaded

`POST /api/games/sixteen-queens/solve`

Sample body:

```json
{
	"boardSize": 16,
	"threadCount": 8,
	"solutionSampleLimit": 50
}
```

### 2) Submit player answer

`POST /api/games/sixteen-queens/submit`

Sample body:

```json
{
	"playerName": "Sandamini",
	"boardSize": 16,
	"answer": "0,2,4,1,3,8,10,12,14,5,7,9,11,13,15,6"
}
```

Answer format is comma-separated column indexes for each row from row `0` to row `N-1`.
