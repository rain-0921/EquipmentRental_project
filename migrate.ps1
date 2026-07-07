$ErrorActionPreference = 'Stop'
$mysql = "C:\xampp\mysql\bin\mysql.exe"
if (-not (Test-Path $mysql)) {
    Write-Host "MySQL CLI not found - skipping migration. Run scripts/migrate_v3.sql manually if needed." -ForegroundColor Yellow
    exit 0
}
# Use cmd.exe to perform shell redirection - PowerShell script blocks can't.
& cmd.exe /c "`"$mysql`" -u root < `"$PSScriptRoot\scripts\migrate_v3.sql`""
if ($LASTEXITCODE -ne 0) { Write-Host "Migration failed" -ForegroundColor Red; exit $LASTEXITCODE }
Write-Host "Migration applied" -ForegroundColor Green