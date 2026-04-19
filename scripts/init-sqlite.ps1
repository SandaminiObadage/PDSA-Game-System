$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$dbFile = Join-Path $root 'database\pdsa_game_system.db'
$schemaFile = Join-Path $root 'database\ddl\01_schema_sqlite.sql'
$seedFile = Join-Path $root 'database\seed\01_seed_sqlite.sql'

if (-not (Get-Command sqlite3 -ErrorAction SilentlyContinue)) {
    throw "sqlite3 command not found. Install SQLite CLI and rerun this script."
}

if (Test-Path $dbFile) {
    Remove-Item $dbFile -Force
}

sqlite3 $dbFile ".read $schemaFile"
sqlite3 $dbFile ".read $seedFile"

Write-Host "SQLite database initialized: $dbFile"
