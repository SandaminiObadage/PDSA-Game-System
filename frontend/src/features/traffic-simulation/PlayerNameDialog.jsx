import React, { useState } from 'react';

/**
 * Player name entry dialog
 */
function PlayerNameDialog({ onSubmit, onCancel }) {
  const [playerName, setPlayerName] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!playerName.trim()) {
      setError('Please enter your name');
      return;
    }

    if (playerName.length > 50) {
      setError('Name must be less than 50 characters');
      return;
    }

    onSubmit(playerName.trim());
  };

  return (
    <div className="dialog-overlay">
      <div className="dialog-content">
        <h2>Enter Your Name</h2>
        <p>Before you start playing, let us know who you are!</p>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              placeholder="Your name"
              value={playerName}
              onChange={(e) => {
                setPlayerName(e.target.value);
                setError('');
              }}
              maxLength="50"
              autoFocus
              className="player-name-input"
            />
            {error && <p className="error-message">{error}</p>}
          </div>

          <div className="dialog-actions">
            <button type="submit" className="btn-primary">
              Start Game
            </button>
            <button type="button" onClick={onCancel} className="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PlayerNameDialog;
