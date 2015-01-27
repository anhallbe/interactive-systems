@echo off
set BAT=%~dp0.
call "%BAT%\httpdCopy.bat" dist\ChatServer.jar    server
call "%BAT%\httpdCopy.bat" dist\ChatServer-dl.jar server
call "%BAT%\httpdCopy.bat" lib\jini-core.jar      server
call "%BAT%\httpdCopy.bat" lib\jini-ext.jar       server
call "%BAT%\httpdCopy.bat" lib\reggie-dl.jar      server
