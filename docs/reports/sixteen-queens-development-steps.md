# Sixteen Queens Development Steps

1. Validate input board size (`8..16`), default `16`.
2. Run sequential bitmask backtracking to get count and timing.
3. Run parallel bitmask backtracking (first-row branch split) to get count and timing.
4. Compare timings and compute speedup.
5. Accept player answer as comma-separated columns by row.
6. Validate answer using diagonal and column checks.
7. Reject duplicates if already recognized in current cycle.
8. Store/track recognized answers and player names in DB layer.
9. Reset recognized flags when all target solutions are identified.
10. Add unit tests for correctness, parallel equivalence, and duplicate checks.

## Run locally

From [backend/pom.xml](backend/pom.xml):

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```
