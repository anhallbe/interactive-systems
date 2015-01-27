@Echo Off
REM -- ----------------------------------------------------------------
REM -- *** IMPORTANT ***
REM -- This file is for Ms Windows
REM -- Use this only if you plan to have your codebase server on a PC.
REM -- ----------------------------------------------------------------

REM -- This command file launches the auxilliary HTTP server.
REM -- It can be used for two things:
REM --   1) Serving up Java jar-files for the JarRunner launcher program.
REM --   2) Acting as a codebase server for Java RMI applications.
REM -- Fredrik Kilander, DSV

REM -- INSTRUCTIONS BEFORE USE
REM -- Put this file in the  bin  directory of the lab file-tree.
REM -- Put the file tools.jar in  lib  directory of the lab file-tree.
REM -- Decide on the value of the WWWROOT variable (see below).
REM -- Copy, or edit the install_*.bat files to place the files in
REM -- WWWROOT, and run them.

Set LABROOT="%~dp0..\.."

REM -- Set WWWROOT to the directory where you install your jar-files.
REM -- DO NOT point it to dist in the developer tree!
REM -- Make sure that this is a separate directory, away from
REM -- the building site and your running Java programs.

Set WWWROOT="%LABROOT:"=%\cbs"

REM -- Set TOOLJAR to the location of the tools.jar file. This file
REM -- contains the HTTP server that came with Jini 1.1.

Set TOOLJAR=%LABROOT%\lib\tools.jar

REM -- Set PORT to the portnumber on which the HTTP server should
REM -- listen for requests. If your system is already running a HTTP
REM -- server you probably want a high-numbered port, like 8000, 8080
REM -- or 8800 if they are available.

Set PORT=8080

REM -- Finally, remember that the codebase property of a JVM tells other
REM -- Java programs where to fetch the class definitions they need in
REM -- order to resolve remote objects they receive.

Echo TOOLJAR = %TOOLJAR%
Echo PORT    = %PORT%
Echo WWWROOT = %WWWROOT%

Set OUT=HTTPD_CFG.BAT
echo @ECHO OFF                                 > %OUT%
echo REM AUTO-SCRIPT BY %~dpnx0               >> %OUT%
echo SET CODEBASE=http://%COMPUTERNAME%:%PORT%>> %OUT%

REM -- No classpath by variable.

Set CLASSPATH=

REM Start /b java -jar %TOOLJAR% -port %PORT% -dir %WWWROOT% -trees
Start /b java -jar %TOOLJAR% -port %PORT% -dir %WWWROOT%
