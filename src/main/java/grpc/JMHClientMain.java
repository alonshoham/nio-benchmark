package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import common.Prop;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class JMHClientMain {

    private static int threads = 1;
    private static int cycles = 25;
    private static boolean print = false;

    @Benchmark
    public void testEcho(JMHNIOClient client){
        client.sendMessage("message");
    }

//    @TearDown
//    public void close(JMHNIOClient client){
//        client.close();
//    }

    public static void main(String[] args) throws Exception {
        parseArgs(args);
        Options opt = new OptionsBuilder()
                .include(JMHClientMain.class.getName())
                .forks(1)
                .threads(threads)
                .measurementIterations(cycles)
                .build();
        new Runner(opt).run();
    }

    private static void parseArgs(String[] args){
        for (String arg: args){
            String[] parsed = arg.split("=");
            if(parsed != null && parsed.length == 2){
                String key = parsed[0].toLowerCase();
                String value = parsed[1];
                switch (Prop.fromValue(key)){
                    case THREADS:
                        System.out.println("num of threads " + value);
                        threads = Integer.valueOf(value);
                        break;
                    case CYCLES:
                        System.out.println("num of cycles " + value);
                        cycles = Integer.valueOf(value);
                        break;
                    case PRINT:
                        print = Boolean.getBoolean(value);
                        break;
                }
            }
        }
    }

    @State(Scope.Thread)
    public static class JMHNIOClient {
        private static final Random random = new Random();
        private final ManagedChannel channel;
        private final HelloWorldClient client;
        private static final String target = "localhost:50051";

        public JMHNIOClient() {
            // Create a communication channel to the server, known as a Channel. Channels are thread-safe
            // and reusable. It is common to create channels at the beginning of your application and reuse
            // them until the application shuts down.
            channel = ManagedChannelBuilder.forTarget(target)
                    // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            client = new HelloWorldClient(channel);
        }

        public String sendMessage(String msg) {
            msg = msg + "-" + random.nextInt(100) + '\n';
            String response = client.echo(msg);
            if (print) {
                System.out.println("Sent: " + msg);
                System.out.println("Received: " + response);
            }
            return response;
        }

        public void close(){
            try {
                System.out.println("CLOSING!!");
                // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
                // resources the channel should be shut down when it will no longer be used. If it may be used
                // again leave it running.
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
