PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS minimum_cost_rounds (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    round_no INTEGER NOT NULL UNIQUE,
    n INTEGER NOT NULL CHECK (n BETWEEN 50 AND 100),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS minimum_cost_cost_matrices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    round_id INTEGER NOT NULL UNIQUE,
    matrix_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS minimum_cost_algorithm_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    round_id INTEGER NOT NULL,
    algorithm_name TEXT NOT NULL,
    algorithm_variant TEXT NOT NULL DEFAULT '',
    total_cost INTEGER NOT NULL,
    execution_time_ms REAL NOT NULL,
    assignment_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE,
    UNIQUE (round_id, algorithm_name, algorithm_variant)
);

CREATE TABLE IF NOT EXISTS minimum_cost_player_submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL,
    round_id INTEGER NOT NULL,
    submitted_cost INTEGER NOT NULL,
    assignment_json TEXT NOT NULL,
    is_optimal INTEGER NOT NULL CHECK (is_optimal IN (0, 1)),
    submission_hash TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE,
    UNIQUE (round_id, player_name, submission_hash)
);

CREATE INDEX IF NOT EXISTS idx_min_cost_results_round
ON minimum_cost_algorithm_results(round_id);

CREATE INDEX IF NOT EXISTS idx_min_cost_submissions_round
ON minimum_cost_player_submissions(round_id);

CREATE INDEX IF NOT EXISTS idx_min_cost_submissions_player
ON minimum_cost_player_submissions(player_name);