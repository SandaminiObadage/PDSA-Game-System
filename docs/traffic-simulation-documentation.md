# Traffic Simulation Game - Implementation Guide

## Overview

The Traffic Simulation Game is an interactive learning application that teaches maximum flow algorithms. Players analyze a traffic network and predict the maximum flow from source to sink, then compare their answers with two different maximum flow algorithms: Ford-Fulkerson and Dinic's Algorithm.

## Project Structure

```
backend/
├── src/main/java/com/nibm/pdsa/games/traffic/
│   ├── algorithm/
│   │   ├── FordFulkersonAlgorithm.java      # Classic max-flow using DFS
│   │   └── DinicAlgorithm.java              # Optimized max-flow using level graphs
│   ├── controller/
│   │   └── TrafficController.java           # REST API endpoints
│   ├── dto/
│   │   ├── TrafficSolveRequest.java
│   │   ├── MaxFlowComparisonResponse.java
│   │   ├── SubmitTrafficAnswerRequest.java
│   │   ├── SubmitTrafficAnswerResponse.java
│   │   ├── TrafficLeaderboardEntry.java
│   │   └── TrafficLeaderboardResponse.java
│   ├── model/
│   │   ├── TrafficEdge.java                 # Graph edge representation
│   │   ├── TrafficGraph.java                # Graph data structure
│   │   ├── MaxFlowResult.java               # Algorithm result
│   │   └── TrafficGameRound.java            # Game round state
│   ├── repository/
│   │   └── TrafficRepository.java           # Database access layer
│   └── service/
│       └── TrafficService.java              # Business logic
├── src/test/java/com/nibm/pdsa/games/traffic/
│   └── algorithm/
│       └── MaxFlowAlgorithmsTest.java       # Unit tests
└── src/main/resources/db/migration/
    └── V4__initialize_traffic_simulation_game.sql

frontend/
├── src/pages/traffic-simulation/
│   └── page.jsx                             # Main game page
├── src/features/traffic-simulation/
│   ├── TrafficNetworkGraph.jsx              # Network visualization
│   ├── AlgorithmComparison.jsx              # Results display
│   ├── GameResult.jsx                       # Win/lose screen
│   ├── Leaderboard.jsx                      # Leaderboard component
│   └── PlayerNameDialog.jsx                 # Name entry dialog
└── src/api/
    └── trafficSimulationApi.js              # API client
```

## Game Network

The traffic network consists of 9 nodes:
- **Source**: A (start point)
- **Intermediate Nodes**: B, C, D, E, F, G, H
- **Sink**: T (end point)

### Road Connections (Directed Edges)

Each edge has a random capacity between 5-15 vehicles/minute:

```
A → B, A → C, A → D
B → E, B → F
C → E, C → F
D → F
E → G, E → H
F → H
G → T, H → T
```

## API Endpoints

### 1. Generate New Round
```
POST /api/games/traffic-simulation/new-round
Response: MaxFlowComparisonResponse
{
  "roundId": "1",
  "networkData": {
    "nodes": ["A", "B", "C", "D", "E", "F", "G", "H", "T"],
    "source": "A",
    "sink": "T",
    "edges": [
      {"from": "A", "to": "B", "capacity": 10},
      ...
    ]
  },
  "fordFulkersonResult": {
    "maxFlow": 25,
    "executionTimeMs": 1.5,
    "algorithmName": "Ford-Fulkerson"
  },
  "dinicResult": {
    "maxFlow": 25,
    "executionTimeMs": 0.8,
    "algorithmName": "Dinic's Algorithm"
  }
}
```

### 2. Submit Player Answer
```
POST /api/games/traffic-simulation/submit?roundId={roundId}
Request Body:
{
  "playerName": "John Doe",
  "playerAnswer": 25
}

Response: SubmitTrafficAnswerResponse
{
  "isCorrect": true,
  "correctAnswer": 25,
  "message": "Correct! 🎉",
  "algorithmExecutionTimeMs": 1.5
}
```

### 3. Get Leaderboard
```
GET /api/games/traffic-simulation/leaderboard?limit=10
Response: TrafficLeaderboardResponse
{
  "entries": [
    {
      "playerName": "Alice",
      "correctAnswers": 5,
      "averageExecutionTimeMs": 1.2,
      "lastPlayedAt": 1704067200000
    },
    ...
  ],
  "generatedAt": 1704153600000
}
```

### 4. Health Check
```
GET /api/games/traffic-simulation/health
Response:
{
  "status": "OK",
  "game": "Traffic Simulation"
}
```

## Maximum Flow Algorithms

### Ford-Fulkerson Algorithm
- **Approach**: Uses DFS to find augmenting paths
- **Time Complexity**: O(E * maxFlow) where E is number of edges
- **Best For**: Small graphs with small capacities
- **Implementation**: Iteratively finds paths with available capacity and updates residual graph

### Dinic's Algorithm
- **Approach**: Uses level graphs and blocking flows
- **Time Complexity**: O(V² * E) where V is number of vertices
- **Best For**: Dense graphs and graphs with large capacities
- **Implementation**: 
  1. Build level graph using BFS
  2. Find blocking flows using DFS from source to sink
  3. Repeat until no path exists

## Database Schema

### game_types
Stores information about game modules (includes TRAFFIC_SIMULATION)

### game_rounds
Stores each generated game round with random capacities

### algorithm_runs
Records execution time and results for each algorithm

### player_answers
Stores player submissions with correctness flag

### players
Stores player information and game history

### game_algorithms
Maps available algorithms to each game

## Running the Application

### Backend
```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`

### Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173`

### Running Tests
```bash
cd backend
mvn test
```

## Unit Tests

The `MaxFlowAlgorithmsTest` class includes tests for:

1. **Simple Linear Path**: A → B → C (bottleneck = 5)
2. **Complex Network**: Multiple paths with different capacities
3. **Game Network**: Full network with all edges
4. **Single Edge**: Direct connection
5. **No Path**: Disconnected nodes
6. **Parallel Paths**: Multiple routes to same node
7. **Execution Time**: Algorithm timing accuracy
8. **Algorithm Names**: Correct naming of algorithms
9. **Invalid Input**: Source/sink validation
10. **Flow Conservation**: Flow balance at nodes

Run tests with: `mvn test`

## Game Features

### Player Experience
1. **Player Name Entry**: Players enter their name before starting
2. **Network Visualization**: Interactive SVG graph showing traffic network
3. **Answer Submission**: Players input their predicted maximum flow
4. **Immediate Feedback**: See if answer is correct/incorrect
5. **Algorithm Comparison**: View both algorithms' results and execution times
6. **Leaderboard**: Competitive ranking by correct answers and speed

### Data Persistence
- Player names and answers stored in SQLite database
- Algorithm execution times recorded for analysis
- Leaderboard maintained with player rankings
- Complete game history available

### Educational Value
- Learn about maximum flow problem
- Compare different algorithmic approaches
- Understand time complexity differences
- Practice network analysis skills

## Validations & Exception Handling

### Input Validation
- Player name must be non-empty and < 50 characters
- Answer must be a valid non-negative integer
- Source and sink nodes must exist in graph

### Exception Handling
- Invalid graph state checked before solving
- Database errors caught and reported
- API errors returned with meaningful messages
- Network capacity validation enforced

## Performance Considerations

### Algorithm Efficiency
- Ford-Fulkerson: Simple but slower for complex networks
- Dinic's Algorithm: More efficient for competition graphs
- Both handle the game network (9 nodes) in < 5ms

### Database Optimization
- Indexes on game_round_id and player_id for fast queries
- Connection pooling for efficient database access
- Query results cached in memory where appropriate

### Frontend Performance
- SVG graph renders instantly for 9 nodes
- Lazy loading of leaderboard data
- Minimized CSS bundle with production builds

## Deployment

### Production Setup
1. Build backend: `mvn clean package`
2. Build frontend: `npm run build`
3. Configure database connection in `application.yml`
4. Deploy JAR file and static frontend files

### Configuration
Edit `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlite:./pdsa.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: validate
```

## Future Enhancements

1. **Visualization Enhancements**
   - Animate flow along paths
   - Show step-by-step algorithm execution
   - Interactive residual graph display

2. **Algorithm Extensions**
   - Push-relabel algorithm
   - Successive shortest paths
   - Minimum cost maximum flow

3. **Game Features**
   - Difficulty levels (different network sizes)
   - Timed challenges
   - Cooperative multiplayer modes
   - Performance statistics and trends

4. **Educational**
   - Algorithm explanation with animations
   - Step-by-step solver guide
   - Comparison with other max-flow problems

## Troubleshooting

### Backend Issues
- **Port 8080 already in use**: Change port in `application.yml`
- **Database locked**: Ensure only one instance is running
- **Tests fail**: Check SQLite installation

### Frontend Issues
- **API not found**: Ensure backend is running on port 8080
- **Graph not rendering**: Check browser console for errors
- **Styles not loading**: Clear browser cache

## Support

For issues or questions about the Traffic Simulation Game, refer to:
- Backend code comments for algorithm details
- API endpoint documentation above
- Unit tests for algorithm behavior
- Component comments in frontend code
