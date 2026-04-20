/**
 * Component to display game result (win/lose/draw)
 */
function GameResult({ isCorrect, correctAnswer, playerAnswer, algorithmTime, onPlayAgain }) {
  console.log('GameResult component received:', {
    isCorrect,
    correctAnswer,
    playerAnswer,
    algorithmTime,
    isCorrectType: typeof isCorrect,
    correctAnswerType: typeof correctAnswer,
    playerAnswerType: typeof playerAnswer
  });

  if (isCorrect) {
    return (
      <div className="game-result win">
        <div className="result-icon">🎉</div>
        <h2>Correct Answer!</h2>
        <p className="result-message">
          You correctly identified the maximum flow as <strong>{correctAnswer}</strong> vehicles/min
        </p>
        <p className="algorithm-time">
          The algorithm took <strong>{algorithmTime.toFixed(2)} ms</strong> to compute
        </p>
        <button onClick={onPlayAgain} className="btn-play-again">
          Play Again
        </button>
      </div>
    );
  } else {
    return (
      <div className="game-result lose">
        <div className="result-icon">❌</div>
        <h2>Incorrect Answer</h2>
        <p className="result-message">
          You answered <strong>{playerAnswer}</strong>, but the correct answer is <strong>{correctAnswer}</strong>
        </p>
        <p className="result-diff">
          Difference: {Math.abs(playerAnswer - correctAnswer)} vehicles/min
        </p>
        <button onClick={onPlayAgain} className="btn-play-again">
          Try Again
        </button>
      </div>
    );
  }
}

export default GameResult;
