$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$srcFiles = Get-ChildItem -Recurse -Filter *.java -Path 'src\main\java' | ForEach-Object { $_.FullName }
Write-Host "Compiling $($srcFiles.Count) Java sources..."
# Pass file list via --source-list file so PowerShell never sees the @ prefix
$srcFiles | Out-File -Encoding ascii sources.txt
# Read back and pass as a flat array (no @)
$lines = Get-Content sources.txt
$argList = @('-d', 'out', '-cp', 'lib\*') + $lines
& javac @argList
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "BUILD OK"