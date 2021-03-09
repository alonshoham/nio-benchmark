@echo off
call settings.bat
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar bio.BioServer poolType=work-stealing poolSize=8