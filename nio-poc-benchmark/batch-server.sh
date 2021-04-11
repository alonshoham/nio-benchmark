#!/bin/bash
source settings.sh
for c in lrmi nio
do
    export GS_NIO_TYPE=$c
    for w in 4 #1 4 8 16 32
    do
        export GS_NIO_SERVER_READER_POOL_SIZE=$w
        for t in 1 2 4 8 16 32 64
        do
            echo "*** Starting space with $c and $w workers for $t benchmark threads"
            $GS_NIO_GS_HOME/bin/gs.sh space run --lus test
        done
    done
done
