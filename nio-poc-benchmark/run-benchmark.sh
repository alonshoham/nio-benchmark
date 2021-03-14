source settings.sh
# arg #1 - num of benchmark threads
java -cp target/nio-poc-benchmark-0.1-jar-with-dependencies.jar jmh.benchmarks.basic.ReadByIdBenchmark $1
