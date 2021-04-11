@echo off
call settings.bat
rem arg #1 - num of benchmark threads
rem java -cp target\nio-poc-benchmark-0.1-jar-with-dependencies.jar jmh.benchmarks.basic.ReadByIdBenchmark 2
java -cp target/nio-poc-benchmark-0.1-jar-with-dependencies.jar org.openjdk.jmh.Main jmh.benchmarks.basic.ReadByIdBenchmark -f 1 -i 5 -wi 5
rem -rff $c-w$w-t$t.csv -f 1 -i 5 -t $t
pause
