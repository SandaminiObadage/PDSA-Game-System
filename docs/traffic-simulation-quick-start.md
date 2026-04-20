# Traffic Simulation Game - Quick Start Guide

## 5-Minute Setup

### Prerequisites
- Java 17+
- Node.js 16+
- SQLite
- Git

### Step 1: Start the Backend

```bash
cd backend
mvn spring-boot:run
```

Wait for the message: `Started PdsaBackendApplication`

### Step 2: Start the Frontend

In a new terminal:
```bash
cd frontend
npm install
npm run dev
```

The app will open at `http://localhost:5173`

### Step 3: Play the Game

1. Click "Traffic Simulation" on the dashboard
2. Enter your name (e.g., "Alice")
3. Click "Generate New Round"
4. Study the traffic network graph
5. Enter your predicted maximum flow
6. Click "Submit Answer"
7. Compare your answer with the algorithms!

## Game Rules

### Objective
Predict the maximum number of vehicles/minute that can flow from source (A) to sink (T) in the given traffic network.

### How Maximum Flow Works
The maximum flow is limited by:
1. The capacities of roads
2. The weakest link in any path (bottleneck)
3. The ability to distribute flow across multiple paths

### Example
If the network is:
```
A → B (capacity 10) → T
A → C (capacity 5) → T
```

Maximum flow = 10 + 5 = 15 vehicles/minute

## What You'll Learn

### Maximum Flow Algorithms
1. **Ford-Fulkerson**: Classic approach using DFS
   - Pros: Easy to understand
   - Cons: Slower for complex networks

2. **Dinic's Algorithm**: Modern approach using level graphs
   - Pros: Much faster for dense graphs
   - Cons: More complex to implement

### Algorithm Efficiency
Compare execution times between algorithms:
- Small networks: Both are very fast
- Large networks: Dinic's is significantly faster

### Network Analysis
- Identify bottlenecks in the network
- Find multiple paths from source to sink
- Understand flow conservation (flow in = flow out)

## Leaderboard

Compete with other players on:
1. **Correct Answers**: Number of right predictions
2. **Algorithm Speed**: Average execution time

Hover over the leaderboard button to see rankings!

## Tips for Better Predictions

1. **Find All Paths**: Identify multiple routes from A to T
2. **Calculate Bottlenecks**: Each path is limited by its smallest capacity
3. **Sum the Paths**: Add maximum flow of all disjoint paths
4. **Use Algorithm Results**: Learn from the solutions shown

## Common Mistakes

❌ **Wrong**: Counting maximum capacity of single node
✓ **Right**: Following complete paths from A to T

❌ **Wrong**: Ignoring alternative routes
✓ **Right**: Finding all possible paths and summing them

❌ **Wrong**: Exceeding edge capacities
✓ **Right**: Respecting maximum capacity on each edge

## Keyboard Shortcuts

- `Enter` in name field: Start game
- `Enter` in answer field: Submit answer

## Troubleshooting

### "Cannot connect to server"
- Ensure backend is running on port 8080
- Check that frontend is on port 5173
- Look for error messages in browser console

### "Maximum flow is X but you said Y"
- Click "Try Again" to analyze another round
- Look at the algorithm solution to understand the bottleneck
- Try to identify missed paths

### Player name won't submit
- Name must be 1-50 characters
- No special characters in name
- Try a simple name like "Player1"

## Advanced Features

### Viewing Algorithm Steps (Coming Soon)
Click on algorithm results to see:
- Augmenting paths found
- Flow updates at each step
- Final residual graph

### Different Difficulty Levels (Coming Soon)
- Easy: 5 nodes, simple paths
- Medium: 9 nodes (current)
- Hard: 15+ nodes, complex topology

### Timed Challenges (Coming Soon)
Race against the clock to find maximum flow!

## Need Help?

### In-Game Help
- Hover over 💡 icons for hints
- Check algorithm comparison for solutions
- Review leaderboard for sample answers

### Documentation
- See `docs/traffic-simulation-documentation.md` for full details
- Check `docs/api/` for API specifications
- Review unit tests for algorithm behavior

### Ask Your Instructor
- Clarify maximum flow definition
- Discuss algorithm efficiency tradeoffs
- Review network flow concepts

## Next Steps

After mastering Traffic Simulation:
- Try other games: Knights Tour, Sixteen Queens, etc.
- Implement your own maximum flow algorithm
- Analyze more complex networks
- Study related topics: minimum cut, max-flow min-cut theorem

---

**Happy Gaming and Learning! 🚗**
