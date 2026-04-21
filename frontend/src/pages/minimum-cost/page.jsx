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

  // MATRIX
  const generateMatrix = (n) => {
    return Array.from({ length: n }, () =>
      Array.from({ length: n }, () =>
        Math.floor(Math.random() * 90) + 10
      )
    );
  };

  // GREEDY
  const greedy = (m) => {
    let used = new Set();
    let cost = 0;

    for (let i = 0; i < m.length; i++) {
      let min = Infinity, idx = -1;
      for (let j = 0; j < m.length; j++) {
        if (!used.has(j) && m[i][j] < min) {
          min = m[i][j];
          idx = j;
        }
      }
      used.add(idx);
      cost += min;
    }
    return cost;
  };

  // PERMUTE
  const permute = (arr) => {
    if (arr.length === 1) return [arr];
    let res = [];
    arr.forEach((v, i) => {
      const rest = [...arr.slice(0, i), ...arr.slice(i + 1)];
      permute(rest).forEach((p) => res.push([v, ...p]));
    });
    return res;
  };

  // OPTIMAL
  const optimal = (m) => {
    const perms = permute([...Array(m.length).keys()]);
    let min = Infinity;

    perms.forEach((p) => {
      let cost = 0;
      for (let i = 0; i < m.length; i++) {
        cost += m[i][p[i]];
      }
      if (cost < min) min = cost;
    });

    return min;
  };

  // NEW GAME
  const newGame = () => {
    getMinimumCostGame()
      .then((data) => {
        const m = data.matrix;
        const opt = data.hungarianCost;
        const g = data.greedyCost;

        const opts = [opt, g, opt + 20].sort(() => Math.random() - 0.5);

        setMatrix(m);
        setCorrect(opt);
        setGreedyCost(g);
        setOptions(opts);
        setSelected(null);
        setMessage("");
        setTime(10);
      })
      .catch(() => {
        // Fallback to local generation if backend fails
        const m = generateMatrix(4);
        const opt = optimal(m);
        const g = greedy(m);

        const opts = [opt, g, opt + 20].sort(() => Math.random() - 0.5);

        setMatrix(m);
        setCorrect(opt);
        setGreedyCost(g);
        setOptions(opts);
        setSelected(null);
        setMessage("");
        setTime(10);
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
          <p>Beat the algorithm. Find the lowest cost!</p>
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
              <h3>Cost Matrix</h3>
              <table>
                <tbody>
                  {matrix.map((row, i) => (
                    <tr key={i}>
                      {row.map((v, j) => (
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
              </div>

              <div className="card result alt">
                <h4>Greedy</h4>
                <h2>${greedyCost}</h2>
              </div>

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
          <h1>🏆 Recent Results</h1>
          <div className="card" style={{ maxHeight: "400px", overflowY: "auto" }}>
            {leaderboard.length > 0 ? (
              <table style={{ width: "100%", textAlign: "left", fontSize: "14px" }}>
                <thead>
                  <tr style={{ borderBottom: "2px solid #333" }}>
                    <th>Player</th>
                    <th>Optimal Cost</th>
                    <th>Selected Cost</th>
                    <th>Time Left</th>
                    <th>Result</th>
                  </tr>
                </thead>
                <tbody>
                  {leaderboard.map((result, i) => (
                    <tr key={result.id} style={{ borderBottom: "1px solid #ddd", padding: "8px" }}>
                      <td>{result.playerName}</td>
                      <td>${result.correctCost}</td>
                      <td>${result.selectedCost}</td>
                      <td>{result.timeRemaining}s</td>
                      <td>{result.isCorrect ? "✅ Correct" : "❌ Wrong"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No results yet! Be the first to play. 🚀</p>
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