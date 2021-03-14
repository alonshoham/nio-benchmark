source settings.sh
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar bio.BioServer poolType=work-stealing poolSize=8
