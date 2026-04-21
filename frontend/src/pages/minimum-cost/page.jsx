import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import "./MinimumCost.css";
import {
  getMinimumCostGame,
  submitMinimumCostScore,
  fetchMinimumCostLeaderboard,
  checkMinimumCostBackend
} from "../../api/minimumcostApi";

function MinimumCost() {
  const navigate = useNavigate();
  const [step, setStep] = useState("home");
  const [name, setName] = useState("");
  const [score, setScore] = useState(0);

  const [matrix, setMatrix] = useState([]);
  const [correct, setCorrect] = useState(0);
  const [greedyCost, setGreedyCost] = useState(0);
  const [options, setOptions] = useState([]);

  const [selected, setSelected] = useState(null);
  const [message, setMessage] = useState("");

  const [time, setTime] = useState(10);
  const [round, setRound] = useState(1);
  const [streak, setStreak] = useState(0);
  const [useRandomTaskCount, setUseRandomTaskCount] = useState(true);
  const [taskCountInput, setTaskCountInput] = useState(50);
  const [taskCountUsed, setTaskCountUsed] = useState(0);
  const [roundId, setRoundId] = useState(null);
  const [greedyTimeMs, setGreedyTimeMs] = useState(0);
  const [hungarianTimeMs, setHungarianTimeMs] = useState(0);

  const [backendStatus, setBackendStatus] = useState("Checking...");
  const [leaderboard, setLeaderboard] = useState([]);

  // 🔊 SAFE SOUND FUNCTION (NO ERROR)
  const playSound = (file) => {
    try {
      const audio = new Audio(file);
      audio.play().catch(() => {});
    } catch (e) {
      console.log("Sound error");
    }
  };

  // FETCH BACKEND STATUS
  useEffect(() => {
    checkMinimumCostBackend()
      .then((data) => {
        setBackendStatus(`${data.status} (${data.game})`);
      })
      .catch(() => setBackendStatus("Backend not reachable"));
    
    // Fetch leaderboard on mount
    fetchLeaderboard();
  }, []);

  // SAVE SCORE
  const saveScore = () => {
    fetchLeaderboard();
    alert("Game results saved to database! 🎉");
  };

  const fetchLeaderboard = () => {
    fetchMinimumCostLeaderboard(20)
      .then((data) => setLeaderboard(data.results || []))
      .catch(() => {});
  };

  const endGame = () => {
    setStep("gameover");
  };

  const handleBackToDashboard = () => {
    navigate('/');
  };

  // NEW GAME
  const newGame = () => {
    const requestedTaskCount = useRandomTaskCount ? undefined : taskCountInput;
    getMinimumCostGame(requestedTaskCount)
      .then((data) => {
        const m = data.matrix;
        const opt = data.hungarianCost;
        const g = data.greedyCost;
        const distractor = opt + Math.floor(Math.random() * 50) + 25;
        const opts = Array.from(new Set([opt, g, distractor])).sort(() => Math.random() - 0.5);

        setMatrix(m);
        setCorrect(opt);
        setGreedyCost(g);
        setTaskCountUsed(data.taskCount || data.matrixSize || m.length);
        setRoundId(data.roundId || null);
        setGreedyTimeMs(data.greedyExecutionTimeMs || 0);
        setHungarianTimeMs(data.hungarianExecutionTimeMs || 0);
        setOptions(opts);
        setSelected(null);
        setMessage("");
        setTime(10);
      })
      .catch(() => {
        setMessage("Backend unavailable. Could not generate a round.");
      });
  };

  const startGame = () => {
    newGame();
    setStep("game");
  };

  const nextRound = () => {
    setRound((r) => r + 1);
    newGame();
  };

  // TIMER
  useEffect(() => {
    if (step !== "game") return;

    if (time === 0) {
      setMessage("⏱️ Time's up!");
      setStreak(0);
      setTimeout(() => nextRound(), 1500);
      return;
    }

    const t = setTimeout(() => setTime(time - 1), 1000);
    return () => clearTimeout(t);
  }, [time, step]);

  // CHECK
  const check = (val) => {
    setSelected(val);

    const isCorrect = val === correct;
    
    if (isCorrect) {
      setMessage("✅ Correct!");
      setScore((s) => s + 10);
      setStreak((s) => s + 1);
      playSound("/correct.mp3");
    } else {
      setMessage(`❌ Wrong! Correct: ${correct}`);
      setStreak(0);
      playSound("/wrong.mp3");
    }

    // Save result to database using the API
    const gameResult = {
      playerName: name,
      correctCost: correct,
      selectedCost: val,
      timeRemaining: time,
      isCorrect: isCorrect
    };

    submitMinimumCostScore(gameResult)
      .then((response) => {
        console.log("Result saved:", response.message);
      })
      .catch((err) => console.log("Failed to save result:", err));

    setTimeout(() => nextRound(), 1500);
  };

  return (
    <div className="MinimumCost">

      {/* HOME */}
      {step === "home" && (
        <div className="home">
          <h1>🎮 Minimum Cost Game</h1>
          <p>Assign N tasks to N employees with minimum total cost.</p>
          <p>Backend: {backendStatus}</p>
          <button onClick={() => setStep("modal")}>Start Game</button>
          <button onClick={() => setStep("leaderboard")} style={{ marginLeft: "10px" }}>
            View Leaderboard
          </button>
        </div>
      )}

      {/* MODAL */}
      {step === "modal" && (
        <div className="modal">
          <div className="modalBox">
            <h2>Enter your name</h2>
            <input 
              value={name}
              onChange={(e) => setName(e.target.value)} 
              placeholder="Your name"
            />
            <h3 style={{ marginTop: "14px" }}>Number of Tasks (50 to 100)</h3>
            <label style={{ display: "block", marginBottom: "8px" }}>
              <input
                type="checkbox"
                checked={useRandomTaskCount}
                onChange={(e) => setUseRandomTaskCount(e.target.checked)}
                style={{ marginRight: "8px" }}
              />
              Random every round (50 to 100)
            </label>
            <input
              type="number"
              min="50"
              max="100"
              value={taskCountInput}
              onChange={(e) => setTaskCountInput(Math.max(50, Math.min(100, Number(e.target.value) || 50)))}
              placeholder="Task count"
              disabled={useRandomTaskCount}
            />
            <div style={{ marginTop: "12px", textAlign: "left", fontSize: "14px" }}>
              <h3 style={{ margin: "0 0 8px 0" }}>How to Play</h3>
              <p style={{ margin: "4px 0" }}>1. One task per employee.</p>
              <p style={{ margin: "4px 0" }}>2. Each task can be used once only.</p>
              <p style={{ margin: "4px 0" }}>3. Pick the lowest total cost before time ends.</p>
              <p style={{ margin: "6px 0 0 0", opacity: 0.85 }}>
                Tip: Hungarian is optimal; Greedy is faster but may cost more.
              </p>
            </div>
            <button onClick={startGame} disabled={!name.trim()}>
              Play
            </button>
          </div>
        </div>
      )}

      {/* GAME */}
      {step === "game" && (
        <div className="game">

          <div className="header">
            <span>👤 {name}</span>
            <span>⭐ {score}</span>
            <span>🔥 {streak}</span>
            <span>🎯 Round {round}</span>
            <span>🧮 Tasks {taskCountUsed || taskCountInput}</span>
            <span>⏱️ {time}s</span>
            <button onClick={endGame} style={{ padding: "5px 10px", cursor: "pointer" }}>
              End Game
            </button>
          </div>

          {/* TIMER BAR */}
          <div className="timerBar">
            <div
              className="timerFill"
              style={{ width: `${(time / 10) * 100}%` }}
            ></div>
          </div>

          {message && <div className="message">{message}</div>}

          <div className="content">

            {/* MATRIX */}
            <div className="card matrix">
              <h3>Cost Matrix ({taskCountUsed || matrix.length}x{taskCountUsed || matrix.length})</h3>
              <p style={{ marginTop: "0", fontSize: "13px", opacity: 0.85 }}>
                Showing first 10 rows x 10 columns for readability.
              </p>
              <table>
                <tbody>
                  {matrix.slice(0, 10).map((row, i) => (
                    <tr key={i}>
                      {row.slice(0, 10).map((v, j) => (
                        <td key={j}>{v}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* SIDE */}
            <div className="side">

              <div className="card result">
                <h4>Optimal</h4>
                <h2>${correct}</h2>
                <small>Hungarian: {hungarianTimeMs.toFixed(2)} ms</small>
              </div>

              <div className="card result alt">
                <h4>Greedy</h4>
                <h2>${greedyCost}</h2>
                <small>Greedy: {greedyTimeMs.toFixed(2)} ms</small>
              </div>

              {roundId && (
                <div className="card result alt">
                  <h4>Round ID</h4>
                  <h2>#{roundId}</h2>
                </div>
              )}

              <div className="card">
                <h4>Select Answer</h4>

                {options.map((o, i) => {
                  let cls = "answerBtn";

                  if (selected !== null) {
                    if (o === correct) cls += " correct";
                    else if (o === selected) cls += " wrong";
                  }

                  return (
                    <button
                      key={i}
                      className={cls}
                      onClick={() => check(o)}
                      disabled={selected !== null}
                    >
                      ${o}
                    </button>
                  );
                })}

              </div>

            </div>

          </div>

        </div>
      )}

      {/* GAME OVER */}
      {step === "gameover" && (
        <div className="home">
          <h1>🎮 Game Over!</h1>
          <div className="card">
            <h2>Final Score: ⭐ {score}</h2>
            <p>Rounds Completed: 🎯 {round}</p>
            <p>Highest Streak: 🔥 {streak}</p>
            <button onClick={saveScore}>Save Score</button>
            <button onClick={() => setStep("leaderboard")} style={{ marginLeft: "10px" }}>
              View Leaderboard
            </button>
            <button onClick={() => setStep("home")} style={{ marginLeft: "10px" }}>
              Back to Home
            </button>
          </div>
        </div>
      )}

      {/* LEADERBOARD */}
      {step === "leaderboard" && (
        <div className="home">
          <h1>🏆 Leaderboard</h1>
          <div className="card" style={{ maxHeight: "400px", overflowY: "auto" }}>
            {leaderboard.length > 0 ? (
              <table style={{ width: "100%", textAlign: "left", fontSize: "14px" }}>
                <thead>
                  <tr style={{ borderBottom: "2px solid #333" }}>
                    <th>Rank</th>
                    <th>Player</th>
                    <th>Score</th>
                    <th>Correct</th>
                    <th>Games</th>
                    <th>Accuracy</th>
                    <th>Avg Time Left</th>
                  </tr>
                </thead>
                <tbody>
                  {leaderboard.map((result, i) => (
                    <tr key={`${result.playerName}-${i}`} style={{ borderBottom: "1px solid #ddd", padding: "8px" }}>
                      <td>#{result.rank ?? i + 1}</td>
                      <td>{result.playerName}</td>
                      <td>{result.totalScore ?? 0}</td>
                      <td>{result.correctAnswers ?? 0}</td>
                      <td>{result.gamesPlayed ?? 0}</td>
                      <td>{result.accuracy ?? "0.0%"}</td>
                      <td>{result.averageTimeRemaining ?? "0.0"}s</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No leaderboard data yet. Play a few rounds to create rankings. 🚀</p>
            )}
          </div>
          <button onClick={() => setStep("home")} style={{ marginTop: "20px" }}>
            Back to Home
          </button>
        </div>
      )}

      <div className="footerActions">
        <button className="btnBackDashboard" onClick={handleBackToDashboard}>
          ← Back to Dashboard
        </button>
      </div>

    </div>
  );
}

export default MinimumCost;