CREATE TABLE IF NOT EXISTS knight_tour_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    board_size INTEGER NOT NULL,
    start_position VARCHAR(20) NOT NULL,
    algorithm_type VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knight_game_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    game_round_id INTEGER NOT NULL,
    moves TEXT NOT NULL,
    is_win BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE RESTRICT,
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_knight_game_results_player_id ON knight_game_results(player_id);
CREATE INDEX IF NOT EXISTS idx_knight_game_results_round_id ON knight_game_results(game_round_id);
