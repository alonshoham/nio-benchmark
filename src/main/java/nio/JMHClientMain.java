package nio;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

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
        private SocketChannel client;

        public JMHNIOClient() {
            try {
                client = SocketChannel.open(new InetSocketAddress(InetAddress
                        .getLoopbackAddress(), 8007));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String sendMessage(String msg) {
            msg = msg + "-" + random.nextInt(100) + '\n';
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            String response = null;
            try {
                client.write(buffer);
                buffer.clear();
                client.read(buffer);
                response = new String(buffer.array()).trim();
                buffer.clear();
                if(print) {
                    System.out.println("Sent: " + msg);
                    System.out.println("Received: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        public void close(){
            try {
                System.out.println("CLOSING!!");
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
