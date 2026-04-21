import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Board from '../../features/knights-tour/components/Board';
import Controls from '../../features/knights-tour/components/Controls';
import {
  findNextMoveByAlgorithm,
  getLegalCandidates,
  getWarnsdorffCandidates,
  validateMoveList
} from '../../features/knights-tour/solver';

function toOutcome(status, message, knightId, moveCount) {
  return { status, message, knightId, moveCount };
}

function saveGameState(game) {
  localStorage.setItem('knightGame', JSON.stringify(game));
}

function Game() {
  const navigate = useNavigate();
  const [game, setGame] = useState(null);
  const [moves, setMoves] = useState([]);
  const [timer, setTimer] = useState(300);
  const [autoSpeed, setAutoSpeed] = useState(300);
  const [autoRunning, setAutoRunning] = useState(false);
  const [error, setError] = useState('');
  const [ended, setEnded] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('knightGame');
    if (!saved) {
      navigate('/games/knights-tour', { replace: true });
      return;
    }

    const parsed = JSON.parse(saved);
    setGame(parsed);
    setMoves(parsed.moves || []);
  }, [navigate]);

  useEffect(() => {
    if (!game) {
      return undefined;
    }

    if (ended) {
      return undefined;
    }

    if (timer <= 0) {
      handleGameEnd('DRAW', 'Game ended as a draw.');
      return undefined;
    }

    const timerId = setInterval(() => {
      setTimer((current) => current - 1);
    }, 1000);

    return () => clearInterval(timerId);
  }, [timer, game, ended]);

  const boardSize = game?.boardSize || 8;
  const current = moves[moves.length - 1];
  const visited = useMemo(() => new Set(moves), [moves]);
  const legalMoves = useMemo(() => {
    if (!current || !game) {
      return [];
    }
    return getLegalCandidates(current, visited, boardSize);
  }, [current, visited, boardSize, game]);

  const visibleCandidates = useMemo(() => {
    if (!game || !current) {
      return [];
    }

    if (game.algorithmType !== 'BACKTRACKING') {
      return getWarnsdorffCandidates(current, visited, boardSize);
    }

    // Backtracking hints are intentionally hidden to avoid running deep DFS on every re-render.
    return [];
  }, [game, current, boardSize, visited]);

  useEffect(() => {
    if (!game || !moves.length) {
      return;
    }

    if (moves.length === boardSize * boardSize) {
      handleGameEnd('WIN', 'Congratulations! Valid full knight tour.');
      return;
    }

    if (legalMoves.length === 0) {
      handleGameEnd('LOSE', 'No legal moves remain before covering all squares.');
    }
  }, [moves, game, boardSize, legalMoves.length]);

  useEffect(() => {
    if (!autoRunning || !game || ended) {
      return undefined;
    }

    const id = setInterval(() => {
      handleNextMove();
    }, autoSpeed);

    return () => clearInterval(id);
  }, [autoRunning, autoSpeed, game, moves, ended]);

  const updateMoves = (nextMoves) => {
    setMoves(nextMoves);
    if (game) {
      const nextGame = { ...game, moves: nextMoves };
      setGame(nextGame);
      saveGameState(nextGame);
    }
  };

  const isLegalMove = (targetKey) => {
    return legalMoves.some((candidate) => candidate.key === targetKey);
  };

  const handleSquareClick = (row, col) => {
    const key = `${row},${col}`;
    if (!isLegalMove(key)) {
      setError('That move is not legal from the current position.');
      return;
    }

    setError('');
    updateMoves([...moves, key]);
  };

  const handleNextMove = () => {
    try {
      validateMoveList(moves, boardSize);
      const next = findNextMoveByAlgorithm(game.algorithmType, moves, boardSize);
      if (!next) {
        setAutoRunning(false);
        return;
      }
      updateMoves([...moves, next]);
      setError('');
    } catch (solverError) {
      setError(solverError.message || 'Unable to generate next move.');
      setAutoRunning(false);
    }
  };

  const handleGameEnd = (status, message) => {
    if (!game || ended) {
      return;
    }

    setEnded(true);
    setAutoRunning(false);
    localStorage.setItem(
      'knightPendingOutcome',
      JSON.stringify(toOutcome(status, message, game.knightId, moves.length))
    );

    setTimeout(() => {
      navigate('/games/knights-tour/result');
    }, 1100);
  };

  const handleResetPath = () => {
    if (!moves.length) {
      return;
    }
    setEnded(false);
    updateMoves([moves[0]]);
    setError('');
  };

  const handleSubmit = () => {
    if (moves.length === boardSize * boardSize) {
      handleGameEnd('WIN', 'Congratulations! Valid full knight tour.');
      return;
    }

    if (legalMoves.length === 0) {
      handleGameEnd('LOSE', 'No legal moves remain before covering all squares.');
      return;
    }

    handleGameEnd('DRAW', 'Game ended as a draw.');
  };

  const handleQuit = () => {
    handleGameEnd('DRAW', 'Game ended as a draw.');
  };

  if (!game) {
    return null;
  }

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Knight&apos;s Tour</p>
          <h1>Board Play</h1>
          <p className="subtitle">
            Visit every square once using legal knight jumps. Use manual moves, next-move hints,
            or auto solve with your selected speed.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>
            {ended ? 'Game Ended' : 'In Progress'}
            <br />
            Algorithm: {game.algorithmType}
            <br />
            Timer: {timer}s
          </strong>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Board</h2>
          <p className="info-chip">Moves: {moves.length} | Legal Moves: {legalMoves.length}</p>

          <div className="kt-board-panel">
            <Board
              boardSize={boardSize}
              moves={moves}
              candidates={visibleCandidates}
              onSquareClick={handleSquareClick}
              disabled={ended}
            />
          </div>

          {error && <p className="kt-error">{error}</p>}
        </article>

        <article className="panel">
          <h2>Game Controls</h2>
          <div className="kt-controls-panel">
            <Controls
              autoSpeed={autoSpeed}
              onAutoSpeedChange={setAutoSpeed}
              onNextMove={handleNextMove}
              onToggleAuto={() => setAutoRunning((value) => !value)}
              onResetPath={handleResetPath}
              onSubmit={handleSubmit}
              onQuit={handleQuit}
              autoRunning={autoRunning}
              disabled={ended}
            />
          </div>
        </article>
      </section>
    </main>
  );
}

export default Game;
