# Inicia Mailpit para captura de e-mails em desenvolvimento
# SMTP: localhost:1025 | Interface web: http://localhost:8025

$mailpitExe = Join-Path $PSScriptRoot "mailpit.exe"

if (Test-Path $mailpitExe) {
    $running = Get-NetTCPConnection -LocalPort 8025 -State Listen -ErrorAction SilentlyContinue
    if (-not $running) {
        Start-Process -FilePath $mailpitExe -WindowStyle Hidden
        Write-Host "Mailpit iniciado (scripts/mailpit.exe)." -ForegroundColor Green
    } else {
        Write-Host "Mailpit já está em execução." -ForegroundColor Green
    }
} else {
    $existing = docker ps -a --filter "name=ust-mailpit" --format "{{.Names}}" 2>$null
    if ($existing -eq "ust-mailpit") {
        docker start ust-mailpit 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Mailpit iniciado via Docker." -ForegroundColor Green
        } else {
            Write-Host "Docker indisponível. Baixe mailpit.exe em:" -ForegroundColor Yellow
            Write-Host "https://github.com/axllent/mailpit/releases" -ForegroundColor Yellow
            Write-Host "Coloque em scripts/mailpit.exe e execute este script novamente." -ForegroundColor Yellow
            exit 1
        }
    } else {
        docker run -d --name ust-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Mailpit iniciado via Docker." -ForegroundColor Green
        } else {
            Write-Host "Docker indisponível. Baixe mailpit.exe em:" -ForegroundColor Yellow
            Write-Host "https://github.com/axllent/mailpit/releases" -ForegroundColor Yellow
            Write-Host "Coloque em scripts/mailpit.exe e execute este script novamente." -ForegroundColor Yellow
            exit 1
        }
    }
}

Write-Host ""
Write-Host "Interface web: http://localhost:8025" -ForegroundColor Cyan
Write-Host "SMTP:        localhost:1025" -ForegroundColor Cyan
