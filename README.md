# nio-benchmark
Benchmark of Java NIO echo server using JMH


The benchmark runs the same client code against various servers. 

### Client
The client main is nio.JMHClientMain with the optional arguments:
- threads=\<number> determines the number of clients initialized (defaults to 1)
- cycles=\<number determines the number of JMH iterations (defaults to 25)
- print=\<boolean> if true, console will print the echo of client/server (defaults to false)

### Server
The server implementations available:
- nio.ReadInSelectorServer
- nio.ReadInTaskServer
- netty.echo.NettyNioServer
- netty.echo.NettyEpollServer

Both nio servers accept the following optional args:
- poolType=\<String> - determines which thread pool will be used. possible values:
    - fixed - fixed thread pool
    - work-stealing - fork/join pool
    - dynamic - Gigaspaces custom thread pool impl
    - default is fixed
    
- poolSize=\<number> - determines the pool size (defaults to 4) 

### How to run
- run mvn install
- Start client:`java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.JMHClientMainthreads=2 cycles=10 print=true`
- Start server:`java -cp target/nio-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar nio.ReadInSelectorServer poolType=work-stealing poolSize=8`

### Output
The client main will output a standard JMH output of average TP

