source settings.sh
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.JMHClientMain cycles=10 threads=$1

