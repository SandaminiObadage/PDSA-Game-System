import { useNavigate, useParams } from 'react-router-dom';

function GameNotAvailable() {
  const navigate = useNavigate();
  const { gameId } = useParams();

  const gameNames = {
    'sixteen-queens': 'Sixteen Queens\' Puzzle',
    'knights-tour': 'Knights Tour',
    'minimum-cost': 'Minimum Cost Path',
    'snake-ladder': 'Snake & Ladder',
    'traffic-simulation': 'Traffic Simulation'
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button
            className="back-button"
            onClick={() => navigate('/')}
            title="Back to dashboard"
          >
            ← Dashboard
          </button>
          <p className="eyebrow">Game Status</p>
          <h1>{gameNames[gameId] || 'Game Not Found'}</h1>
          <p className="subtitle">
            This game is part of the PDSA Game System, but the frontend hasn't been implemented yet.
            The backend API is ready when you're ready to build the UI!
          </p>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Coming Soon</h2>
          <p>
            This game will allow you to:
          </p>
          <ul>
            <li>Solve algorithmic challenges</li>
            <li>Submit your solutions</li>
            <li>Compete on the leaderboard</li>
            <li>View detailed reports</li>
          </ul>
          <p>
            Check back soon for the full implementation!
          </p>
          <button onClick={() => navigate('/')}>Return to Dashboard</button>
        </article>

        <article className="panel">
          <h2>Backend Ready</h2>
          <p>
            The game backend is already implemented and waiting for a frontend UI.
            You can build the interface using:
          </p>
          <ul>
            <li>React with responsive component architecture</li>
            <li>Interactive elements for gameplay</li>
            <li>Real-time feedback and validation</li>
            <li>Integration with the REST API</li>
          </ul>
        </article>
      </section>
    </main>
  );
}

export default GameNotAvailable;
