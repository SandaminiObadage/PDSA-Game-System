PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_types (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_rounds (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_type_id INTEGER NOT NULL,
    round_no INTEGER NOT NULL,
    round_input_json TEXT NOT NULL,
    expected_output_json TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_type_id) REFERENCES game_types(id) ON DELETE RESTRICT,
    UNIQUE (game_type_id, round_no)
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_round_id INTEGER NOT NULL,
    algorithm_name TEXT NOT NULL,
    algorithm_variant TEXT,
    execution_time_ms REAL NOT NULL,
    result_json TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_round_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    answer_json TEXT NOT NULL,
    is_correct INTEGER NOT NULL CHECK (is_correct IN (0, 1)),
    submitted_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recognized_solutions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_type_id INTEGER NOT NULL,
    solution_hash TEXT NOT NULL,
    solution_json TEXT NOT NULL,
    recognized_by_player_id INTEGER,
    recognized_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    FOREIGN KEY (game_type_id) REFERENCES game_types(id) ON DELETE RESTRICT,
    FOREIGN KEY (recognized_by_player_id) REFERENCES players(id) ON DELETE SET NULL,
    UNIQUE (game_type_id, solution_hash)
);

CREATE TABLE IF NOT EXISTS game_algorithms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_type_id INTEGER NOT NULL,
    algorithm_name TEXT NOT NULL,
    description TEXT,
    is_enabled INTEGER NOT NULL DEFAULT 1 CHECK (is_enabled IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_type_id) REFERENCES game_types(id) ON DELETE CASCADE,
    UNIQUE (game_type_id, algorithm_name)
);

CREATE INDEX IF NOT EXISTS idx_game_rounds_game_type ON game_rounds(game_type_id);
CREATE INDEX IF NOT EXISTS idx_algorithm_runs_round ON algorithm_runs(game_round_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_algorithm_runs_unique
ON algorithm_runs(game_round_id, algorithm_name, IFNULL(algorithm_variant, ''));
CREATE INDEX IF NOT EXISTS idx_player_answers_round ON player_answers(game_round_id);
CREATE INDEX IF NOT EXISTS idx_player_answers_player ON player_answers(player_id);
CREATE INDEX IF NOT EXISTS idx_recognized_solutions_game_type ON recognized_solutions(game_type_id);

INSERT OR IGNORE INTO game_types (code, display_name) VALUES
('MINIMUM_COST', 'Minimum Cost'),
('SNAKE_LADDER', 'Snake and Ladder Game Problem'),
('TRAFFIC_SIMULATION', 'Traffic Simulation Problem'),
('KNIGHTS_TOUR', 'Knight''s Tour Problem'),
('SIXTEEN_QUEENS', 'Sixteen Queens'' Puzzle');
