import { useMemo, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';




/* ─── main page ─────────────────────────────────────────────────── */
function MinimumCostPage() {
  const navigate = useNavigate();

  const [gameForm, setGameForm] = useState();
  const [status, setStatus] = useState('Ready');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  /* game state */
  const [currentRoundId, setCurrentRoundId] = useState(null);
  const [costMatrix, setCostMatrix] = useState([]);
  const [hungResult, setHungResult] = useState(null);   // { assignment, cost, timeMs }
  const [bbResult, setBbResult] = useState(null);
  const [chosenAlgo, setChosenAlgo] = useState('hungarian');
  const [answerFromGrid, setAnswerFromGrid] = useState('');

  /* history / leaderboard */
  const [historyResult, setHistoryResult] = useState(null);
  const [leaderboardResult, setLeaderboardResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);

  /* unit tests */
  const [unitResults, setUnitResults] = useState(null);

  /* ── render ── */
  return (
    <main className="shell">
      {/* ── hero ── */}
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Optimization challenge</p>
          <h1>Minimum Cost Task Assignment</h1>
          <p className="subtitle">
            Assign N tasks to N employees so the total cost is minimized. Each round generates
            a fresh N×N cost matrix ($20–$200 per cell). Two algorithms compete: Hungarian method
            (O(n³)) and Branch &amp; Bound with row-minimum pruning.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{status}</strong>
        </div>
      </section>


      {error && <p className="error-banner">{error}</p>}
    </main>
  );
}

export default MinimumCostPage;