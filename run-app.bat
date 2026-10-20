@echo off
chcp 65001 >nul
echo ==========================================================
echo    DOSTATOK - Full Stack System Starter
echo ==========================================================

rem Reuse the DOSTATOK backend if its authenticated API is already responding.
set "API_STATUS="
for /f %%S in ('curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/api/auth/me 2^>nul') do set "API_STATUS=%%S"
if "%API_STATUS%"=="401" goto check_profile_endpoint

rem Do not start Vite against an older or unrelated process on port 8080.
netstat -ano -p tcp | findstr /R /C:":8080 .*LISTENING" >nul
if not errorlevel 1 (
    echo [ERROR] Port 8080 is occupied by a server without the DOSTATOK authentication API.
    echo Stop that backend process, then run this launcher again.
    exit /b 1
)

echo Starting Java backend in this VS Code terminal...
start "" /b cmd /c ""%~dp0backend\run.bat""

set /a ATTEMPTS=0
:wait_for_backend
set "API_STATUS="
for /f %%S in ('curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/api/auth/me 2^>nul') do set "API_STATUS=%%S"
if "%API_STATUS%"=="401" goto check_profile_endpoint
set /a ATTEMPTS+=1
if %ATTEMPTS% GEQ 45 (
    echo [ERROR] Backend did not become ready on port 8080. Check the Java messages above.
    exit /b 1
)
timeout /t 1 >nul
goto wait_for_backend

:check_profile_endpoint
set "PROFILE_API_STATUS="
for /f %%S in ('curl.exe -s -o nul -w "%%{http_code}" -X PUT -H "Origin: http://localhost:5173" -H "Content-Type: application/json" -d "{}" http://localhost:8080/api/auth/profile 2^>nul') do set "PROFILE_API_STATUS=%%S"
if "%PROFILE_API_STATUS%"=="401" goto start_frontend
echo [ERROR] The backend on port 8080 is an older DOSTATOK version without profile support.
echo Close its backend terminal, then run this launcher again to compile and start the updated backend.
exit /b 1

:start_frontend
echo Starting Vite frontend; opening the default browser...
cd /d "%~dp0frontend"
npm run dev -- --open
