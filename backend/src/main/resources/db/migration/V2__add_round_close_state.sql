ALTER TABLE game_rounds ADD COLUMN is_closed INTEGER NOT NULL DEFAULT 0 CHECK (is_closed IN (0, 1));
ALTER TABLE game_rounds ADD COLUMN closed_at TEXT;

CREATE INDEX IF NOT EXISTS idx_game_rounds_closed ON game_rounds(game_type_id, is_closed);
