@echo off
rem 1: read-echo in same thread
rem 2: read, context-switch (submit), echo
set GSN_VERSION=1
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.JMHClientMain threads=1 cycles=10
pause