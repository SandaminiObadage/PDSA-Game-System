import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import SixteenQueensPage from './pages/sixteen-queens/page';
import GameNotAvailable from './pages/GameNotAvailable';
import MinimumCostPage from './pages/minimum-cost/page';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/games/sixteen-queens" element={<SixteenQueensPage />} />
        <Route path="/games/minimum-cost" element={<MinimumCostPage />} />
        <Route path="/games/:gameId" element={<GameNotAvailable />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
