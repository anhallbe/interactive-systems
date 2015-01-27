@Echo Off
REM -- ----------------------------------------------------------------
REM -- 2014-02-17/FK: Added -Djava.rmi.server.useCodebaseOnly=false
REM -- This file starts the Jini Lookup Server (reggie) on Ms Windows.
REM -- This file uses Windows NT command extensions.
REM -- Fredrik Kilander, fki@kth.se
REM -- ----------------------------------------------------------------
REM --
REM -- PRECONDITIONS
REM -- This file should be in test/bin/pc
REM -- The file reggie.jar should be in test/lib 
REM -- The file reggie-dl.jar should be in test/cbs
REM -- Codebase files should be in test/cbs
REM -- The simple codebase server should be running (r1_http.bat)
REM -- The rmi deamon should be running (r2_rmid.bad)
REM --
REM -- The easiest way to start and stop the reggie Lookup Service, is to
REM -- Start:
REM --   run  r2_rmid
REM --   run  r3_reggie
REM -- Run your programs, do work for as long as it takes...
REM -- Stop:
REM --   Open a Windows command prompt and give the command
REM --     rmid -stop
REM --   This will shut down rmid and reggie as well.

Set LABROOT=%~dp0..\..

Set PCY=%LABROOT%\lib\policy.all

Set LOG=%TEMP%\reggie_log

Set CFG=%~dp0.\HTTPD_CFG.BAT

IF NOT EXIST "%CFG%" GOTO nofile
CALL "%CFG%"
SET HTTP=%CODEBASE%

:nofile

SET CBS=%HTTP%/reggie-dl.jar

REM -- If you are using another web server as codebase, uncomment the
REM -- line below and modify appropriately.

REM Set CBS=http://%COMPUTERNAME%:80/reggie-dl.jar

Set JAR=%LABROOT%\lib\reggie.jar

Echo ******************************
Echo Starting a Jini Lookup Service
Echo PCY = %PCY%
Echo LOG = %LOG%
Echo CBS = %CBS%
Echo JAR = %JAR%


REM ********************************
REM Remove the old log
REM R[emove]D[irectory] /S[earch the tree] /Q[uietly]
REM ********************************

If Not Exist %LOG% Goto LogIsGone
Echo Removing %LOG%
RD /S /Q %LOG%
:LogIsGone

REM ********************************
REM Disable the classpath variable.
REM ********************************

Set CLASSPATH=

Echo ******************************

REM ************************
REM Invoke the program.
REM Since we are running reggie here, the 'public' at the end of
REM the arguments refers to the group which should be served.
REM 
REM The expected behaviour is that reggie registers with rmid as
REM an activatable service and then exits, ie the command prompt
REM reappears. The command window can be used again.
REM ************************

Set CBO=-Djava.rmi.server.useCodebaseOnly=false

java %CBO% -jar "%JAR%" "%CBS%" "%PCY%" "%LOG%" public
