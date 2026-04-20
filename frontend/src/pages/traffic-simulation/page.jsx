import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TrafficNetworkGraph from '../../features/traffic-simulation/TrafficNetworkGraph';
import AlgorithmComparison from '../../features/traffic-simulation/AlgorithmComparison';
import GameResult from '../../features/traffic-simulation/GameResult';
import Leaderboard from '../../features/traffic-simulation/Leaderboard';
import PlayerNameDialog from '../../features/traffic-simulation/PlayerNameDialog';
import {
  generateNewRound,
  submitAnswer,
  fetchLeaderboard
} from '../../api/trafficSimulationApi';

function TrafficSimulationPage() {
  const navigate = useNavigate();

  // Game state
  const [playerName, setPlayerName] = useState('');
  const [showNameDialog, setShowNameDialog] = useState(true);
  const [gameState, setGameState] = useState('waiting'); // waiting, round_generated, submitted, showing_result
  const [currentRound, setCurrentRound] = useState(null);
  const [playerAnswer, setPlayerAnswer] = useState('');
  const [submitResult, setSubmitResult] = useState(null);
  const [leaderboardData, setLeaderboardData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showLeaderboard, setShowLeaderboard] = useState(false);

  // Initialize game
  useEffect(() => {
    loadLeaderboard();
  }, []);

  const handlePlayerNameSubmit = (name) => {
    setPlayerName(name);
    setShowNameDialog(false);
    setGameState('waiting');
  };

  const loadLeaderboard = async () => {
    try {
      const data = await fetchLeaderboard(10);
      setLeaderboardData(data);
    } catch (err) {
      console.error('Error loading leaderboard:', err);
    }
  };

  const generateNewGameRound = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await generateNewRound();
      setCurrentRound(response);
      setPlayerAnswer('');
      setSubmitResult(null);
      setGameState('round_generated');
    } catch (err) {
      setError(err.message || 'Failed to generate new round');
      console.error('Error generating round:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async (e) => {
    e.preventDefault();

    if (!playerAnswer.trim()) {
      setError('Please enter your answer');
      return;
    }

    const answer = parseInt(playerAnswer);
    if (isNaN(answer) || answer < 0) {
      setError('Please enter a valid number');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await submitAnswer(playerName, answer, currentRound.roundId);
      console.log('Backend response received:', response);
      console.log('Response isCorrect:', response.isCorrect, 'Type:', typeof response.isCorrect);
      console.log('Response correctAnswer:', response.correctAnswer);
      setSubmitResult(response);
      setGameState('showing_result');
      
      // Reload leaderboard after submission
      setTimeout(() => {
        loadLeaderboard();
      }, 1000);
    } catch (err) {
      setError(err.message || 'Failed to submit answer');
      console.error('Error submitting answer:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePlayAgain = () => {
    setPlayerAnswer('');
    setSubmitResult(null);
    setCurrentRound(null);
    setGameState('waiting');
    generateNewGameRound();
  };

  const handleBackToDashboard = () => {
    navigate('/');
  };

  // Show name dialog
  if (showNameDialog) {
    return (
      <div className="traffic-simulation-page">
        <PlayerNameDialog
          onSubmit={handlePlayerNameSubmit}
          onCancel={handleBackToDashboard}
        />
      </div>
    );
  }

  return (
    <main className="shell">
      <section className="game-header">
        <div>
          <h1>🚗 Traffic Simulation Game</h1>
          <p className="subtitle">Find the maximum flow from source A to sink T</p>
          <p className="player-info">Player: <strong>{playerName}</strong></p>
        </div>
      </section>

      {error && <div className="error-banner">{error}</div>}

      <section className="game-content">
        {/* Welcome screen */}
        {gameState === 'waiting' && !currentRound && (
          <div className="welcome-section">
            <div className="welcome-card">
              <h2>Welcome to Traffic Simulation! 🚗</h2>
              <p>
                In this game, you'll analyze a traffic network and determine the maximum flow of vehicles
                that can travel from source node A to sink node T.
              </p>
              <p>
                Two algorithms will solve it for you:
              </p>
              <ul>
                <li><strong>Ford-Fulkerson:</strong> A classic max-flow algorithm using DFS</li>
                <li><strong>Dinic's Algorithm:</strong> A faster algorithm with better time complexity</li>
              </ul>
              <p>
                Your task is to correctly predict the maximum flow before looking at the solution!
              </p>
              
              <div className="game-instructions">
                <h3>How to Play:</h3>
                <ol>
                  <li>Click "Generate New Round" to create a random traffic network</li>
                  <li>Study the network graph showing nodes and road capacities</li>
                  <li>Calculate or estimate the maximum flow from A to T</li>
                  <li>Enter your answer and submit</li>
                  <li>See how your answer compares to the algorithms!</li>
                </ol>
              </div>

              <button
                onClick={generateNewGameRound}
                disabled={loading}
                className="btn-large btn-primary"
              >
                {loading ? 'Generating...' : 'Generate New Round'}
              </button>
            </div>
          </div>
        )}

        {/* Game round screen */}
        {gameState === 'round_generated' && currentRound && (
          <>
            <div className="game-round-section">
              <div className="network-display">
                <h2>Traffic Network</h2>
                <TrafficNetworkGraph
                  nodes={currentRound.networkData?.nodes || []}
                  edges={currentRound.networkData?.edges || []}
                  source={currentRound.networkData?.source || 'A'}
                  sink={currentRound.networkData?.sink || 'T'}
                />
              </div>
            </div>

            <div className="answer-section">
              <h2>Your Answer</h2>
              <form onSubmit={handleSubmitAnswer} className="answer-form">
                <div className="form-group">
                  <label htmlFor="maxflow">Maximum Flow (vehicles/minute):</label>
                  <input
                    type="number"
                    id="maxflow"
                    min="0"
                    value={playerAnswer}
                    onChange={(e) => {
                      setPlayerAnswer(e.target.value);
                      setError('');
                    }}
                    placeholder="Enter your answer..."
                    disabled={loading}
                    className="input-answer"
                  />
                </div>

                <div className="form-hint">
                  <p>💡 <strong>Hint:</strong> The maximum flow is limited by the smallest capacity along any path from A to T.</p>
                </div>

                <button
                  type="submit"
                  disabled={loading || !playerAnswer}
                  className="btn-large btn-primary"
                >
                  {loading ? 'Submitting...' : 'Submit Answer'}
                </button>
              </form>

              <button
                onClick={() => {
                  setGameState('waiting');
                  setCurrentRound(null);
                  setPlayerAnswer('');
                }}
                disabled={loading}
                className="btn-secondary"
              >
                ← Back to Menu
              </button>
            </div>

            {/* Leaderboard during game round */}
            <section className="leaderboard-section" style={{ marginTop: '16px' }}>
              <div className="leaderboard-header">
                <h2>🏆 Top Players</h2>
                <button
                  onClick={() => setShowLeaderboard(!showLeaderboard)}
                  className="btn-toggle"
                >
                  {showLeaderboard ? 'Hide' : 'Show'} Leaderboard
                </button>
              </div>

              {showLeaderboard && leaderboardData && (
                <Leaderboard
                  entries={leaderboardData.entries}
                  loading={false}
                  error={null}
                />
              )}
            </section>
          </>
        )}

        {/* Result screen */}
        {gameState === 'showing_result' && submitResult && currentRound && (
          <div className="result-section">
            <GameResult
              isCorrect={submitResult.isCorrect}
              correctAnswer={submitResult.correctAnswer}
              playerAnswer={parseInt(playerAnswer)}
              algorithmTime={submitResult.algorithmExecutionTimeMs}
              onPlayAgain={handlePlayAgain}
            />

            <div className="algorithm-section">
              <AlgorithmComparison
                fordFulkerson={currentRound.fordFulkersonResult}
                dinic={currentRound.dinicResult}
                maxFlow={submitResult.correctAnswer}
              />
            </div>
          </div>
        )}
      </section>

      {/* Leaderboard section - only show when not in game round */}
      {gameState !== 'round_generated' && (
        <section className="leaderboard-section">
          <div className="leaderboard-header">
            <h2>🏆 Top Players</h2>
            <button
              onClick={() => setShowLeaderboard(!showLeaderboard)}
              className="btn-toggle"
            >
              {showLeaderboard ? 'Hide' : 'Show'} Leaderboard
            </button>
          </div>

          {showLeaderboard && leaderboardData && (
            <Leaderboard
              entries={leaderboardData.entries}
              loading={false}
              error={null}
            />
          )}
        </section>
      )}

      {/* Back button */}
      <div className="footer-actions">
        <button onClick={handleBackToDashboard} className="btn-back">
          ← Back to Dashboard
        </button>
      </div>
    </main>
  );
}

export default TrafficSimulationPage;
