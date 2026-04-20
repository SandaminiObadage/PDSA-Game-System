CREATE TABLE IF NOT EXISTS algorithm_solution_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_round_id INTEGER NOT NULL,
    algorithm_name TEXT NOT NULL,
    solution_hash TEXT NOT NULL,
    solution_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_round_id) REFERENCES game_rounds(id) ON DELETE CASCADE,
    UNIQUE (game_round_id, algorithm_name, solution_hash)
);

CREATE INDEX IF NOT EXISTS idx_algo_solution_answers_round
ON algorithm_solution_answers(game_round_id, algorithm_name);
