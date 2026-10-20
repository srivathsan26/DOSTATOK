@echo off
setlocal

:: Set UTF-8 encoding
chcp 65001 >nul

echo ==========================================================
echo    DOSTATOK - Personal Financial Management System
echo    Backend Launcher (Pure Java JDK - No Maven/Gradle)
echo ==========================================================

:: Switch working directory to the backend folder
cd /d "%~dp0"

set "SQLITE_JAR=lib\sqlite-jdbc-3.53.4.0.jar"
set "GSON_JAR=lib\gson-2.11.0.jar"
set "OUT_DIR=out"

:: 1. Verify Java compiler
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' was not found in your PATH. Please install JDK 21+ and configure PATH.
    exit /b 1
)

:: 2. Verify Required JAR files
if not exist "%SQLITE_JAR%" (
    echo [ERROR] Missing SQLite JDBC JAR: %SQLITE_JAR%
    exit /b 1
)

if not exist "%GSON_JAR%" (
    echo [ERROR] Missing Gson JAR: %GSON_JAR%
    exit /b 1
)

:: 3. Prepare Build and Data Directories
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
if not exist "data" mkdir "data"

:: Set Classpath
set "CP=%OUT_DIR%;%SQLITE_JAR%;%GSON_JAR%"

:: 4. Compile Java sources
echo [1/2] Compiling Java backend sources...
javac -encoding UTF-8 -cp "%SQLITE_JAR%;%GSON_JAR%" -d "%OUT_DIR%" src\com\dostatok\controller\*.java src\com\dostatok\dao\*.java src\com\dostatok\database\*.java src\com\dostatok\exception\*.java src\com\dostatok\main\*.java src\com\dostatok\model\*.java src\com\dostatok\service\*.java src\com\dostatok\util\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java compilation failed with exit code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Backend compiled successfully.

:: 5. Launch HTTP Server
echo [2/2] Starting DOSTATOK backend server on port 8080...
echo ----------------------------------------------------------
java --enable-native-access=ALL-UNNAMED -cp "%CP%" com.dostatok.main.App

endlocal
