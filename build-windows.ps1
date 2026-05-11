# Windows build script for JPMS Asteroids project
# This handles the file locking issue on Windows

Write-Host "Stopping Java processes..." -ForegroundColor Green
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "Deleting locked directories..." -ForegroundColor Green
Remove-Item -Path ".\mods-mvn" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".\libs" -Recurse -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

Write-Host "Building project..." -ForegroundColor Green
mvn install -DskipTests

Write-Host "Build complete!" -ForegroundColor Cyan

