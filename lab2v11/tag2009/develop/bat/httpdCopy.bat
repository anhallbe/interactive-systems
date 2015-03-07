@REM ** ************************************************************
@REM **
@REM ** Copies files so that they can be served by the HTTPd server.
@REM **
@REM ** ************************************************************
@ECHO OFF

REM ** You may want to update this to an absolute path.

SET TGT=%~dp0..\test\cbs

XCOPY /F /Z %1 %TGT%\
