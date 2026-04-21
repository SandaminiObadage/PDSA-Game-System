CREATE TABLE IF NOT EXISTS knight (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    board_size INTEGER NOT NULL,
    start_position VARCHAR(20) NOT NULL,
    algorithm_type VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knight_game_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    knight_id INTEGER NOT NULL,
    moves TEXT NOT NULL,
    is_win BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE RESTRICT,
    FOREIGN KEY (knight_id) REFERENCES knight(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_knight_game_results_player_id ON knight_game_results(player_id);
CREATE INDEX IF NOT EXISTS idx_knight_game_results_knight_id ON knight_game_results(knight_id);
