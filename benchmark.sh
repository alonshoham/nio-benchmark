source settings.sh
java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar org.openjdk.jmh.Main nio.JMHClientMain -rf csv -f 1 -i 5 -t $1
