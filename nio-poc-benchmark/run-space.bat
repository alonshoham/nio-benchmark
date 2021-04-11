@echo off
call settings.bat
pushd %GS_NIO_GS_HOME%\bin
call gs.bat space run --lus test
rem pause