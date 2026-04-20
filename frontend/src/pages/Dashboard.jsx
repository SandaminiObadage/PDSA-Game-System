import { useNavigate } from 'react-router-dom';

const GAMES = [
  {
    id: 'sixteen-queens',
    name: 'Sixteen Queens',
    description: 'Place 16 queens on a 16x16 board so they don\'t attack each other. Uses backtracking algorithm.',
    icon: '👑',
    color: '#3b82f6'
  },
  {
    id: 'knights-tour',
    name: 'Knights Tour',
    description: 'Find the path for a chess knight to visit every square on the board exactly once.',
    icon: '🐴',
    color: '#8b5cf6'
  },
  {
    id: 'minimum-cost',
    name: 'Minimum Cost Path',
    description: 'Find the path from top-left to bottom-right with minimum cost using dynamic programming.',
    icon: '💰',
    color: '#ec4899'
  },
  {
    id: 'snake-ladder',
    name: 'Snake & Ladder',
    description: 'Race to the finish! Navigate the board avoiding snakes and climbing ladders.',
    icon: '🎲',
    color: '#f59e0b'
  },
  {
    id: 'traffic-simulation',
    name: 'Traffic Simulation',
    description: 'Simulate and optimize traffic flow patterns using algorithms.',
    icon: '🚗',
    color: '#10b981'
  }
];

function Dashboard() {
  const navigate = useNavigate();

  const handleGameClick = (gameId) => {
    navigate(`/games/${gameId}`);
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <p className="eyebrow">PDSA Game System</p>
          <h1>Game Dashboard</h1>
          <p className="subtitle">
            Choose a game to play! Study algorithm design, competitive programming, and data structures.
          </p>
        </div>
      </section>

      <section className="games-grid">
        {GAMES.map((game) => (
          <button
            key={game.id}
            className="game-card"
            onClick={() => handleGameClick(game.id)}
            style={{ '--card-color': game.color }}
          >
            <div className="game-card-header">
              <span className="game-icon">{game.icon}</span>
              <h2>{game.name}</h2>
            </div>
            <p className="game-description">{game.description}</p>
            <div className="game-footer">
              <span className="play-button">Play →</span>
            </div>
          </button>
        ))}
      </section>
    </main>
  );
}

export default Dashboard;
