package nio;

import common.Client;
import common.Constants;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

@State(Scope.Benchmark)
public class JMHClientMain {

    private static int threads = 1;
    private static int payload = Constants.MAX_PAYLOAD;
    private static int cycles = 10;

    @Benchmark
    public void testEcho(JMHNIOClient client, Blackhole blackhole) {
        Object result = client.sendMessageFixed();
        blackhole.consume(result);
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
            if(parsed.length == 2){
                String key = parsed[0].toLowerCase();
                String value = parsed[1];
                switch (Prop.fromValue(key)){
                    case THREADS:
                        System.out.println("num of threads " + value);
                        threads = Integer.parseInt(value);
                        break;
                    case CYCLES:
                        System.out.println("num of cycles " + value);
                        cycles = Integer.parseInt(value);
                        break;
                    case PAYLOAD:
                        payload = Integer.parseInt(value);
                        break;
                }
            }
        }
    }

    @State(Scope.Thread)
    public static class JMHNIOClient {
        private final Client client;
        private final byte[] message = generatePayload(payload, (byte)'a', (byte) '\n');

        private static byte[] generatePayload(int length, byte b, byte terminator) {
            byte[] result = new byte[length];
            for (int i = 0; i < length; i++) {
                result[i] = b;
            }
            result[length - 1] = terminator;
            return result;
        }


        public JMHNIOClient() {
            try {
                client = new Client();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public byte[] sendMessageFixed() {
            ByteBuffer request = ByteBuffer.wrap(message);
            try {
                client.writeBlocking(request);
                ByteBuffer response = ByteBuffer.allocate(message.length);
                client.readBlocking(response);
                return response.array();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
