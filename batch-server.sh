#!/bin/bash
source settings.sh
for c in nio.NioServer netty.NettyServer
do
    for w in 1 4 8 16 32
    do
	    export GSN_IO_WORKERS=$t
        for t in 1 2 4 8 16 32 64
        do
	       java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar $c
        done
    done
done
