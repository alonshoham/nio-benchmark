@echo off
call settings.bat
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.JMHClientMain threads=1 cycles=10
pause