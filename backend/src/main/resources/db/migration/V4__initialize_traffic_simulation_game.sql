-- V4__initialize_traffic_simulation_game.sql
-- Initialize traffic simulation game type and algorithms

-- Insert traffic simulation game type if not exists
INSERT OR IGNORE INTO game_types (code, display_name, created_at)
VALUES ('TRAFFIC_SIMULATION', 'Traffic Simulation', CURRENT_TIMESTAMP);

-- Register algorithms for traffic simulation
INSERT OR IGNORE INTO game_algorithms (game_type_id, algorithm_name, description, is_enabled, created_at)
SELECT gt.id, 'Ford-Fulkerson', 'Ford-Fulkerson algorithm using DFS to find augmenting paths', 1, CURRENT_TIMESTAMP
FROM game_types gt
WHERE gt.code = 'TRAFFIC_SIMULATION'
AND NOT EXISTS (
    SELECT 1 FROM game_algorithms
    WHERE game_type_id = gt.id AND algorithm_name = 'Ford-Fulkerson'
);

INSERT OR IGNORE INTO game_algorithms (game_type_id, algorithm_name, description, is_enabled, created_at)
SELECT gt.id, 'Dinic''s Algorithm', 'Dinic''s algorithm using level graphs for faster max flow computation', 1, CURRENT_TIMESTAMP
FROM game_types gt
WHERE gt.code = 'TRAFFIC_SIMULATION'
AND NOT EXISTS (
    SELECT 1 FROM game_algorithms
    WHERE game_type_id = gt.id AND algorithm_name = 'Dinic''s Algorithm'
);
