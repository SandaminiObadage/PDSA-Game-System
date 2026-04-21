import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import SixteenQueensPage from './pages/sixteen-queens/page';
import TrafficSimulationPage from './pages/traffic-simulation/page';
import SnakeLadderPage from './pages/snake-ladder/page';
import GameNotAvailable from './pages/GameNotAvailable';
import MinimumCostPage from './pages/minimum-cost/page';
import KnightsTourHome from './pages/knights-tour/Home';
import KnightsTourGame from './pages/knights-tour/Game';
import KnightsTourResult from './pages/knights-tour/Result';
import KnightsTourLeaderboard from './pages/knights-tour/Leaderboard';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/games/sixteen-queens" element={<SixteenQueensPage />} />
        <Route path="/games/knights-tour" element={<KnightsTourHome />} />
        <Route path="/games/knights-tour/play" element={<KnightsTourGame />} />
        <Route path="/games/knights-tour/result" element={<KnightsTourResult />} />
        <Route path="/games/knights-tour/leaderboard" element={<KnightsTourLeaderboard />} />
        <Route path="/games/minimum-cost" element={<MinimumCostPage />} />
        <Route path="/games/traffic-simulation" element={<TrafficSimulationPage />} />
        <Route path="/games/snake-ladder" element={<SnakeLadderPage />} />
        <Route path="/games/:gameId" element={<GameNotAvailable />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
