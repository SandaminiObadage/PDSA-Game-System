import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchMinimumCostHistory,
  fetchMinimumCostLeaderboard,
  fetchMinimumCostReport,
  startMinimumCostRound,
  submitMinimumCostSolution
} from '../../api/minimumCostApi';
import MinimumCostHud from '../../features/minimum-cost/components/MinimumCostHud';
import MatrixHeatmap from '../../features/minimum-cost/components/MatrixHeatmap';
import AlgorithmDuelCard from '../../features/minimum-cost/components/AlgorithmDuelCard';
import SubmissionPanel from '../../features/minimum-cost/components/SubmissionPanel';
import LeaderboardTable from '../../features/minimum-cost/components/LeaderboardTable';
import HistoryTimeline from '../../features/minimum-cost/components/HistoryTimeline';

const TABS = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'leaderboard', label: 'Leaderboard' },
  { id: 'history', label: 'History' }
];

const defaultForm = {
  playerName: '',
  submittedCost: '',
  assignmentInput: ''
};

function formatMs(value) {
  return Number(value || 0).toLocaleString();
}

function formatCost(value) {
  return Number(value || 0).toLocaleString();
}

function previewMatrix(matrix) {
  if (!Array.isArray(matrix)) {
    return [];
  }

  return matrix.slice(0, 12).map((row) => row.slice(0, 12));
}

function parseAssignmentInput(rawInput) {
  if (!rawInput || !rawInput.trim()) {
    return null;
  }

  const trimmed = rawInput.trim();
  if (trimmed.startsWith('[')) {
    return JSON.parse(trimmed);
  }

  return trimmed.split(/[\s,]+/).filter(Boolean).map((value) => Number(value));
}

function MinimumCostPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('dashboard');
  const [round, setRound] = useState(null);
  const [leaderboard, setLeaderboard] = useState(null);
  const [history, setHistory] = useState(null);
  const [report, setReport] = useState(null);
  const [form, setForm] = useState(defaultForm);
  const [status, setStatus] = useState('Ready');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [streak, setStreak] = useState(0);
  const [bestStreak, setBestStreak] = useState(0);
  const [lastSubmission, setLastSubmission] = useState(null);

  const hungarian = round?.hungarian;
  const alternative = round?.alternative;

  const refreshPanels = async (roundId) => {
    const [leaderboardData, historyData, reportData] = await Promise.all([
      fetchMinimumCostLeaderboard(10, roundId),
      fetchMinimumCostHistory(10),
      fetchMinimumCostReport()
    ]);

    setLeaderboard(leaderboardData);
    setHistory(historyData);
    setReport(reportData);
  };

  useEffect(() => {
    refreshPanels().catch((requestError) => {
      setError(requestError?.response?.data?.message || requestError.message || 'Unable to load minimum cost panels.');
    });
  }, []);

  const handleStartRound = async () => {
    setLoading(true);
    setError('');
    setStatus('Generating round and running solvers...');

    try {
      const data = await startMinimumCostRound();
      setRound(data);
      setForm((current) => ({
        ...current,
        submittedCost: ''
      }));
      setStatus(`Round ${data.roundNo} ready with ${data.n} employees/tasks.`);
      await refreshPanels(data.roundId);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Round generation failed';
      setError(message);
      setStatus('Round generation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!round?.roundId) {
      setError('Start a round before submitting a solution.');
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Submitting solution...');

    try {
      const payload = {
        playerName: form.playerName.trim(),
        roundId: round.roundId,
        submittedCost: Number(form.submittedCost)
      };

      let parsedAssignment = null;
      try {
        parsedAssignment = parseAssignmentInput(form.assignmentInput);
      } catch {
        setError('Assignment format is invalid. Use JSON array or comma-separated numbers.');
        setStatus('Submit failed');
        setLoading(false);
        return;
      }

      if (parsedAssignment) {
        payload.assignment = parsedAssignment;
      }

      const data = await submitMinimumCostSolution(payload);
      setLastSubmission(data);
      if (data.correct) {
        setStreak((current) => {
          const next = current + 1;
          setBestStreak((best) => Math.max(best, next));
          return next;
        });
      } else {
        setStreak(0);
      }
      setStatus(data.message || 'Submission processed.');
      await refreshPanels(round.roundId);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Submit failed';
      setError(message);
      setStatus('Submit failed');
    } finally {
      setLoading(false);
    }
  };

  const canSubmit = form.playerName.trim() !== '' && form.submittedCost !== '';

  return (
    <main className="shell minimum-cost-page">
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Minimum Cost Assignment Game</p>
          <h1>Assignment Optimization Lab</h1>
          <p className="subtitle">
            Generate a random assignment round, compare Hungarian against a greedy local optimizer, and record submissions against the optimal cost.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{status}</strong>
        </div>
      </section>

      <MinimumCostHud
        round={round}
        hungarian={hungarian}
        alternative={alternative}
        status={status}
        streak={streak}
        bestStreak={bestStreak}
        loading={loading}
        onStartRound={handleStartRound}
        onRefresh={() => refreshPanels(round?.roundId).catch(() => undefined)}
      />

      <section className="tab-bar" aria-label="Minimum cost views">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            className={activeTab === tab.id ? 'tab-button active' : 'tab-button'}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </section>

      {error ? <div className="error-banner">{error}</div> : null}

      {activeTab === 'dashboard' ? (
        <>
          <section className="grid-layout">
            <MatrixHeatmap matrix={round?.matrix} />
            <SubmissionPanel
              form={form}
              onFormChange={(field, value) => setForm((current) => ({ ...current, [field]: value }))}
              onSubmit={handleSubmit}
              loading={loading}
              canSubmit={canSubmit}
              roundId={round?.roundId}
              lastSubmission={lastSubmission}
            />
          </section>

          <section className="grid-layout">
            <AlgorithmDuelCard title="Hungarian Prime" result={hungarian} opponent={alternative} />
            <AlgorithmDuelCard title="Greedy Local Strike" result={alternative} opponent={hungarian} />
          </section>
        </>
      ) : null}

      {activeTab === 'leaderboard' ? (
        <LeaderboardTable leaderboard={leaderboard} />
      ) : null}

      {activeTab === 'history' ? (
        <HistoryTimeline history={history} report={report} />
      ) : null}
    </main>
  );
}

export default MinimumCostPage;