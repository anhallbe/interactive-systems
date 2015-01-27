@echo off
rem -- ----------------------------------------------------------------
rem -- 2014-02-17/FK: Added -Djava.rmi.server.useCodebaseOnly=false
rem -- This file starts the ChatServer on Ms Windows.
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

set CBS=%HTTP%/server/ChatServer-dl.jar

set JAR=%HTTP%/server/ChatServer.jar

set CLASSPATH=

set CBO=-Djava.rmi.server.useCodebaseOnly=false
set POL=-Djava.security.policy=%PCY%
set CDB=-Djava.rmi.server.codebase=%CBS%

java %CBO% "%POL%" "%CDB%" -jar "%JRN%" "%JAR%" %*
