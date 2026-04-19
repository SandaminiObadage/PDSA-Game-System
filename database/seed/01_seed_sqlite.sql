INSERT OR IGNORE INTO game_types (code, display_name) VALUES
('MINIMUM_COST', 'Minimum Cost'),
('SNAKE_LADDER', 'Snake and Ladder Game Problem'),
('TRAFFIC_SIMULATION', 'Traffic Simulation Problem'),
('KNIGHTS_TOUR', 'Knight''s Tour Problem'),
('SIXTEEN_QUEENS', 'Sixteen Queens'' Puzzle');

INSERT OR IGNORE INTO game_algorithms (game_type_id, algorithm_name, description)
SELECT gt.id, alg.algorithm_name, alg.description
FROM game_types gt
JOIN (
    SELECT 'MINIMUM_COST' AS code, 'HUNGARIAN' AS algorithm_name, 'Optimal assignment via Hungarian method' AS description
    UNION ALL SELECT 'MINIMUM_COST', 'BRANCH_AND_BOUND', 'Search-based optimal assignment'
    UNION ALL SELECT 'SNAKE_LADDER', 'BFS_SHORTEST_PATH', 'Graph shortest throws using BFS'
    UNION ALL SELECT 'SNAKE_LADDER', 'DP_RELAXATION', 'Dynamic programming style relaxation'
    UNION ALL SELECT 'TRAFFIC_SIMULATION', 'EDMONDS_KARP', 'Max flow using BFS augmenting paths'
    UNION ALL SELECT 'TRAFFIC_SIMULATION', 'DINIC', 'Max flow using level graph and blocking flow'
    UNION ALL SELECT 'KNIGHTS_TOUR', 'WARNSDORFF_HEURISTIC', 'Greedy heuristic for knight path'
    UNION ALL SELECT 'KNIGHTS_TOUR', 'BACKTRACKING', 'Recursive or iterative backtracking'
    UNION ALL SELECT 'SIXTEEN_QUEENS', 'SEQUENTIAL_BACKTRACKING', 'Single-threaded solution enumeration'
    UNION ALL SELECT 'SIXTEEN_QUEENS', 'THREADED_SEARCH', 'Parallel/threaded solution enumeration'
) alg ON alg.code = gt.code;
