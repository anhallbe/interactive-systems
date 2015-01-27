@echo off
rem -- ----------------------------------------------------------------
rem -- 2014-02-17/FK: Added -Djava.rmi.server.useCodebaseOnly=false
rem -- This file starts the ChatClient on Ms Windows.
rem -- ----------------------------------------------------------------

set CFG=%~dp0.\HTTPD_CFG.BAT

IF NOT EXIST "%CFG%" GOTO nofile
CALL "%CFG%"
SET HTTP=%CODEBASE%

:nofile

set ROOT=%~dp0..\..

set LIB=%ROOT%/lib

set PCY=%LIB%/policy.all

set JRN=%LIB%/JarRunner.jar

set CBS=%HTTP%/client/ChatClient-dl.jar

set JAR=%HTTP%/client/ChatClient.jar


rem -- If you want logging, modify the properties file appropriately
rem -- and uncomment the next two lines.

rem set LOG=chatclient-logging.properties
rem set DLOG=-Djava.util.logging.config.file=%LOG%

set CLASSPATH=

set CBO=-Djava.rmi.server.useCodebaseOnly=false
set POL=-Djava.security.policy=%PCY%
set CDB=-Djava.rmi.server.codebase=%CBS%

IF DEFINED DLOG (
  java "%DLOG%" %CBO% "%POL%" "%CDB%" -jar "%JRN%" "%JAR%" %*
) ELSE (
  java %CBO% "%POL%" "%CDB%" -jar "%JRN%" "%JAR%" %*
)
