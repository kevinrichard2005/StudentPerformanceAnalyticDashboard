@REM ----------------------------------------------------------------------------
@REM Maven Wrapper (Windows) - downloads the Maven version declared in
@REM .mvn\wrapper\maven-wrapper.properties on first use, then delegates to it.
@REM Requires JAVA_HOME or java on PATH. No system-wide Maven installation needed.
@REM ----------------------------------------------------------------------------
@echo off
setlocal
set "MVNW_PROJECT_DIR=%~dp0"
set "MVNW_CMD="

for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%MVNW_PROJECT_DIR%.mvn\wrapper\mvnw.ps1"`) do set "MVNW_CMD=%%i"

if not defined MVNW_CMD (
  echo [mvnw] Failed to locate or download Maven. 1>&2
  exit /b 1
)

call "%MVNW_CMD%" -f "%MVNW_PROJECT_DIR%pom.xml" %*
exit /b %ERRORLEVEL%
