@Echo Off
REM -- ----------------------------------------------------------------
REM -- *** IMPORTANT ***
REM -- This file is for starting RMID on a Ms Windows PC.
REM -- ----------------------------------------------------------------

REM -- This file launches the Java RMI daemon which is needed
REM -- to register reggie as an activatable service.
REM -- This file uses Windows NT command extensions.
REM -- Fredrik Kilander, DSV

REM -- INSTRUCTIONS BEFORE USE
REM -- Put this file in the  bin  directory of the lab file-tree.

Set LABROOT="%~dp0..\.."

REM -- The location of the directory where rmid should write its log.

Set LOG=%TEMP%\rmid_log

REM -- The policy file rmid should apply.

Set PCY=%LABROOT%\lib\policy.all

Echo ********************************
Echo Starting the Java RMI daemon
Echo You can shut it down by giving the command    rmid -stop
Echo in a command window. That will also stop activated services.
Echo LOG = %LOG%
Echo PCY = %PCY%

REM ********************************
REM Remove old instances of the log directory. This is not the
REM intended way to start rmid, it is supposed to pick up from the
REM logfiles. But since we are developing here...
REM R[emove]D[irectory] /S[earch the tree] /Q[uietly]
REM ********************************

If Not Exist %LOG% Goto LogIsGone
Echo Removing %LOG%
RD /S /Q %LOG%
:LogIsGone

Echo ********************************

REM ********************************
REM Invoke the program in a separate CMD window.
REM ********************************

Start rmid -log %LOG% -J-Djava.security.policy=%PCY% -J-Djava.rmi.server.logCalls=true
