# Inicia backend com H2 (sem Docker/PostgreSQL)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Set-Location $PSScriptRoot\..\backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
