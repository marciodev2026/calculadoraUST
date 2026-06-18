# Sobe Mailpit + backend (H2) + frontend para desenvolvimento local
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$root = Split-Path $PSScriptRoot -Parent

Write-Host "Iniciando Mailpit (SMTP 1025)..." -ForegroundColor Cyan
& "$PSScriptRoot\start-mailpit.ps1"

Write-Host "Iniciando backend (perfil local/H2)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\backend'; mvn spring-boot:run '-Dspring-boot.run.profiles=local'"

Start-Sleep -Seconds 15

Write-Host "Iniciando frontend (Vite)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm run dev"

Write-Host ""
Write-Host "Acesse: http://localhost:5173" -ForegroundColor Green
Write-Host "Mailpit: http://localhost:8025 (rode scripts/start-mailpit.ps1)" -ForegroundColor Cyan
Write-Host "Login: analista@ust.gov.br / analista123" -ForegroundColor Green
Write-Host "       gestor@ust.gov.br / gestor123" -ForegroundColor Green
Write-Host "       admin@ust.gov.br / admin123" -ForegroundColor Green
