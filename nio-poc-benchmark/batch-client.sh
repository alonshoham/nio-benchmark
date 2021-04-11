#!/bin/bash
source settings.sh
for c in lrmi nio
do
    export GS_NIO_TYPE=$c
    for w in 4 #1 4 8 16 32
    do
        for t in 1 2 4 8 16 32 64
        do
            echo "*** Starting benchmark with $c and $w workers for $t benchmark threads"
            java -cp target/nio-poc-benchmark-0.1-jar-with-dependencies.jar org.openjdk.jmh.Main jmh.benchmarks.basic.ReadByIdBenchmark -rff $c-w$w-t$t.csv -f 1 -i 5 -t $t
        done
    done
done
