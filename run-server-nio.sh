source settings.sh
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.NioSingleThreadServer poolType=work-stealing poolSize=8
