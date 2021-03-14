@echo off
call settings.bat
pushd ..
call gs.bat space run --lus test
pause