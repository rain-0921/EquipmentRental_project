Get-Process java -ErrorAction SilentlyContinue |
    Select-Object Id, @{Name='RSS_MB'; Expression={ [math]::Round($_.WorkingSet64/1MB, 1) } } |
    Format-Table -AutoSize