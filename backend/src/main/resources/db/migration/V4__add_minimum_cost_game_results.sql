CREATE TABLE IF NOT EXISTS game_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL,
    correct_cost INTEGER NOT NULL,
    selected_cost INTEGER NOT NULL,
    time_remaining INTEGER NOT NULL,
    is_correct INTEGER NOT NULL CHECK (is_correct IN (0, 1)),
    timestamp INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_game_results_timestamp
ON game_results(timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_game_results_player_name
ON game_results(player_name);
