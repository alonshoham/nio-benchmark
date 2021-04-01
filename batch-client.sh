#!/bin/bash
source settings.sh
for c in nio.NioServer netty.NettyServer
do
    for w in 1 4 8 16 32
    do
        for t in 1 2 4 8 16 32 64
        do
	    	   java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar org.openjdk.jmh.Main nio.JMHClientMain -rff $c-w$w-t$t.csv -f 1 -i 5 -t $t
        done
    done
done
