@echo off
setlocal
pushd "%~dp0"
set "BACKUP=..\database\dostatok-pre-implementation-20261014-223314.db"
if not exist "%BACKUP%" (
    echo [ERROR] Verified pre-implementation backup not found: %BACKUP%
    popd
    exit /b 1
)
set "TEST_DB=..\database\dostatok-security-test-%RANDOM%-%RANDOM%.db"
if exist "%TEST_DB%" (
    echo [ERROR] Test database already exists; refusing to overwrite it.
    popd
    exit /b 1
)
copy /y "%BACKUP%" "%TEST_DB%" >nul
if errorlevel 1 (
    echo [ERROR] Could not create isolated test database.
    popd
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED "-DDOSTATOK_DB_PATH=%CD%\%TEST_DB%" -cp "out;lib\sqlite-jdbc-3.53.4.0.jar;lib\gson-2.11.0.jar" com.dostatok.main.SecurityTestSuite
set "RESULT=%ERRORLEVEL%"
del /q "%TEST_DB%"
popd
exit /b %RESULT%
