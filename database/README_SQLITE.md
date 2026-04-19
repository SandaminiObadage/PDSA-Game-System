# SQLite Setup

This project now includes a SQLite-ready schema and seed data.

## Files
- `database/ddl/01_schema_sqlite.sql`
- `database/seed/01_seed_sqlite.sql`
- `backend/src/main/resources/db/migration/V1__init_sqlite_schema.sql` (Flyway)

## Create DB manually (sqlite3)

```bash
sqlite3 pdsa_game_system.db ".read database/ddl/01_schema_sqlite.sql"
sqlite3 pdsa_game_system.db ".read database/seed/01_seed_sqlite.sql"
```

## Windows PowerShell helper

```powershell
./scripts/init-sqlite.ps1
```

This creates `database/pdsa_game_system.db` and applies schema + seed.
