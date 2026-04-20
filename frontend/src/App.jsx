import { useMemo, useState } from 'react';
import {
  fetchSixteenQueensHistory,
  fetchSixteenQueensLeaderboard,
  fetchSixteenQueensReport,
  resetSixteenQueensRecognized,
  solveSixteenQueens,
  submitSixteenQueens
} from './api/sixteenQueensApi';

const defaultSolveForm = {
  boardSize: 16,
  threadCount: 8,
  solutionSampleLimit: 12,
  persistSolutionLimit: 200
};

const defaultSubmitForm = {
  playerName: '',
  gameRoundId: '',
  boardSize: 16,
  answer: ''
};

function App() {
  const [solveForm, setSolveForm] = useState(defaultSolveForm);
  const [submitForm, setSubmitForm] = useState(defaultSubmitForm);
  const [solveResult, setSolveResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);
  const [historyResult, setHistoryResult] = useState(null);
  const [leaderboardResult, setLeaderboardResult] = useState(null);
  const [reportResult, setReportResult] = useState(null);
  const [status, setStatus] = useState('Ready');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const canSubmit = useMemo(() => submitForm.playerName.trim() && submitForm.answer.trim(), [submitForm]);

  const handleSolve = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    setStatus('Running solve round...');
    try {
      const data = await solveSixteenQueens({
        boardSize: Number(solveForm.boardSize),
        threadCount: Number(solveForm.threadCount),
        solutionSampleLimit: Number(solveForm.solutionSampleLimit),
        persistSolutionLimit: Number(solveForm.persistSolutionLimit)
      });
      setSolveResult(data);
      setSubmitForm((current) => ({
        ...current,
        boardSize: Number(solveForm.boardSize),
        gameRoundId: data.gameRoundId || current.gameRoundId
      }));
      setStatus('Solve completed and saved to SQLite.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Solve failed';
      setError(message);
      setStatus('Solve failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    setStatus('Submitting answer...');
    try {
      const payload = {
        playerName: submitForm.playerName.trim(),
        boardSize: Number(submitForm.boardSize),
        answer: submitForm.answer.trim()
      };

      if (submitForm.gameRoundId !== '') {
        payload.gameRoundId = Number(submitForm.gameRoundId);
      }

      const data = await submitSixteenQueens(payload);
      setSubmitResult(data);
      setStatus('Submission stored in SQLite.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Submit failed';
      setError(message);
      setStatus('Submit failed');
    } finally {
      setLoading(false);
    }
  };

  const loadHistory = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading history from SQLite...');
    try {
      const data = await fetchSixteenQueensHistory(10);
      setHistoryResult(data);
      setStatus('History loaded.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'History load failed';
      setError(message);
      setStatus('History failed');
    } finally {
      setLoading(false);
    }
  };

  const loadLeaderboard = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading leaderboard...');
    try {
      const selectedRoundId = submitForm.gameRoundId !== ''
        ? Number(submitForm.gameRoundId)
        : (solveResult?.gameRoundId || undefined);
      const data = await fetchSixteenQueensLeaderboard(10, selectedRoundId);
      setLeaderboardResult(data);
      setStatus(`Leaderboard loaded${data?.roundId ? ` for round ${data.roundId}` : ''}.`);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Leaderboard load failed';
      setError(message);
      setStatus('Leaderboard failed');
    } finally {
      setLoading(false);
    }
  };

  const handleResetRecognized = async () => {
    const selectedRoundId = submitForm.gameRoundId !== ''
      ? Number(submitForm.gameRoundId)
      : (solveResult?.gameRoundId || undefined);

    if (!window.confirm(`Reset recognized solutions${selectedRoundId ? ` for round ${selectedRoundId}` : ''}?`)) {
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Resetting recognized solutions...');
    try {
      const data = await resetSixteenQueensRecognized(selectedRoundId);
      setStatus(data?.message || 'Recognized solutions reset.');
      setSubmitResult(null);
      setLeaderboardResult(null);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Reset failed';
      setError(message);
      setStatus('Reset failed');
    } finally {
      setLoading(false);
    }
  };

  const loadReport = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading report summary...');
    try {
      const data = await fetchSixteenQueensReport();
      setReportResult(data);
      setStatus('Report loaded.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Report load failed';
      setError(message);
      setStatus('Report failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <p className="eyebrow">PDSA Game System</p>
          <h1>Sixteen Queens Frontend</h1>
          <p className="subtitle">
            React UI connected to Spring Boot + SQLite for solve, submit, history, leaderboard, and report.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{status}</strong>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Run Solver</h2>
          <form onSubmit={handleSolve} className="form-grid">
            <label>
              Board Size
              <input
                type="number"
                min="8"
                max="16"
                value={solveForm.boardSize}
                onChange={(e) => setSolveForm({ ...solveForm, boardSize: e.target.value })}
              />
            </label>
            <label>
              Thread Count
              <input
                type="number"
                min="1"
                max="64"
                value={solveForm.threadCount}
                onChange={(e) => setSolveForm({ ...solveForm, threadCount: e.target.value })}
              />
            </label>
            <label>
              Sample Limit
              <input
                type="number"
                min="1"
                max="500"
                value={solveForm.solutionSampleLimit}
                onChange={(e) => setSolveForm({ ...solveForm, solutionSampleLimit: e.target.value })}
              />
            </label>
            <label>
              Persist Top N
              <input
                type="number"
                min="0"
                max="5000"
                value={solveForm.persistSolutionLimit}
                onChange={(e) => setSolveForm({ ...solveForm, persistSolutionLimit: e.target.value })}
              />
            </label>
            <button type="submit" disabled={loading}>Solve & Save</button>
          </form>
          {solveResult && <ResultBox title="Solve Result" data={solveResult} />}
        </article>

        <article className="panel">
          <h2>Submit Answer</h2>
          <form onSubmit={handleSubmit} className="form-grid">
            <label>
              Player Name
              <input
                value={submitForm.playerName}
                onChange={(e) => setSubmitForm({ ...submitForm, playerName: e.target.value })}
                placeholder="Sandamini"
              />
            </label>
            <label>
              Game Round ID
              <input
                type="number"
                value={submitForm.gameRoundId}
                onChange={(e) => setSubmitForm({ ...submitForm, gameRoundId: e.target.value })}
                placeholder="optional"
              />
            </label>
            <label>
              Board Size
              <input
                type="number"
                min="8"
                max="16"
                value={submitForm.boardSize}
                onChange={(e) => setSubmitForm({ ...submitForm, boardSize: e.target.value })}
              />
            </label>
            <label className="full-width">
              Answer
              <input
                value={submitForm.answer}
                onChange={(e) => setSubmitForm({ ...submitForm, answer: e.target.value })}
                placeholder="0,2,4,1,3,8,10,12,14,5,7,9,11,13,15,6"
              />
            </label>
            <button type="submit" disabled={loading || !canSubmit}>Submit</button>
          </form>
          {submitResult && <ResultBox title="Submission Result" data={submitResult} />}
        </article>
      </section>

      <section className="actions-row">
        <button onClick={loadHistory} disabled={loading}>Load History</button>
        <button onClick={loadLeaderboard} disabled={loading}>Load Leaderboard</button>
        <button onClick={loadReport} disabled={loading}>Load Report</button>
        <button onClick={handleResetRecognized} disabled={loading}>Reset Recognized</button>
      </section>

      <section className="grid-layout three-cols">
        <article className="panel">
          <h2>History</h2>
          <DataList data={historyResult} emptyLabel="No history loaded yet." />
        </article>
        <article className="panel">
          <h2>Leaderboard</h2>
          <DataList data={leaderboardResult} emptyLabel="No leaderboard loaded yet." />
        </article>
        <article className="panel">
          <h2>Report</h2>
          <DataList data={reportResult} emptyLabel="No report loaded yet." />
        </article>
      </section>

      {error && <p className="error-banner">{error}</p>}
    </main>
  );
}

function ResultBox({ title, data }) {
  return (
    <div className="result-box">
      <h3>{title}</h3>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}

function DataList({ data, emptyLabel }) {
  if (!data) {
    return <p className="empty-state">{emptyLabel}</p>;
  }

  return <pre className="data-block">{JSON.stringify(data, null, 2)}</pre>;
}

export default App;
