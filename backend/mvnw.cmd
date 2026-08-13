@ECHO OFF
REM Maven Wrapper launcher for Windows cmd.exe.
REM If `mvn` is already on PATH it is used directly, otherwise the
REM distribution in .mvn\wrapper\maven-wrapper.properties is downloaded
REM once into %USERPROFILE%\.m2\wrapper\dists and reused afterwards.

SETLOCAL ENABLEDELAYEDEXPANSION

WHERE mvn >nul 2>nul
IF %ERRORLEVEL% EQU 0 (
  mvn %*
  EXIT /B %ERRORLEVEL%
)

SET BASEDIR=%~dp0
SET PROPS_FILE=%BASEDIR%.mvn\wrapper\maven-wrapper.properties

FOR /F "usebackq tokens=1,* delims==" %%A IN (`findstr /B "distributionUrl=" "%PROPS_FILE%"`) DO SET DIST_URL=%%B

FOR /F "tokens=2 delims=-" %%V IN ("%DIST_URL:apache-maven-=apache-maven-%") DO SET DIST_FILE=%%~nxV

REM Extract version number between "apache-maven-" and "-bin.zip"
SET DIST_VERSION=%DIST_URL:*apache-maven-=%
SET DIST_VERSION=%DIST_VERSION:-bin.zip=%

SET CACHE_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%DIST_VERSION%
SET MAVEN_HOME=%CACHE_DIR%\apache-maven-%DIST_VERSION%

IF NOT EXIST "%MAVEN_HOME%\bin\mvn.cmd" (
  ECHO mvnw: downloading Apache Maven %DIST_VERSION%...
  IF NOT EXIST "%CACHE_DIR%" MKDIR "%CACHE_DIR%"
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%CACHE_DIR%\maven.zip'"
  powershell -NoProfile -Command "Expand-Archive -Path '%CACHE_DIR%\maven.zip' -DestinationPath '%CACHE_DIR%' -Force"
  DEL "%CACHE_DIR%\maven.zip"
)

CALL "%MAVEN_HOME%\bin\mvn.cmd" %*
ENDLOCAL
