@echo off
set BAT=%~dp0.
call "%BAT%\httpdCopy.bat" dist\ChatClient.jar    client
call "%BAT%\httpdCopy.bat" dist\ChatClient-dl.jar client
call "%BAT%\httpdCopy.bat" lib\jini-core.jar      client
call "%BAT%\httpdCopy.bat" lib\jini-ext.jar       client
call "%BAT%\httpdCopy.bat" lib\reggie-dl.jar      client
