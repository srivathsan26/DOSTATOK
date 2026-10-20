@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

set "DB_PATH=%DOSTATOK_DB_PATH%"
if not defined DB_PATH set "DB_PATH=%~dp0database\dostatok.db"
set "SQLITE_JAR=%~dp0backend\lib\sqlite-jdbc-3.53.4.0.jar"
set "OUT_DIR=%~dp0backend\out"

echo DOSTATOK financial data reset
echo Database: "%DB_PATH%"
echo.
echo This deletes all accounts, transactions, income, expenses, investments,
echo assets, loans, repayments, and financial goals. User profiles are kept.
echo A timestamped backup will be created beside the database first.
echo Stop the DOSTATOK backend before continuing.
echo.
set /p "CONFIRM=Type RESET to continue: "
if /i not "%CONFIRM%"=="RESET" (
    echo Reset cancelled.
    pause
    exit /b 0
)

if not exist "%DB_PATH%" (
    echo [ERROR] Database file not found: "%DB_PATH%"
    pause
    exit /b 1
)
if not exist "%SQLITE_JAR%" (
    echo [ERROR] SQLite JDBC driver not found: "%SQLITE_JAR%"
    pause
    exit /b 1
)
where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac was not found. Install a JDK and add it to PATH.
    pause
    exit /b 1
)
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

javac -encoding UTF-8 -cp "%SQLITE_JAR%" -d "%OUT_DIR%" "%~dp0backend\src\com\dostatok\main\ResetFinancialData.java"
if errorlevel 1 (
    echo [ERROR] Could not compile the reset utility.
    pause
    exit /b 1
)

java -cp "%OUT_DIR%;%SQLITE_JAR%" com.dostatok.main.ResetFinancialData "%DB_PATH%"
if errorlevel 1 (
    pause
    exit /b 1
)
pause
