package nio;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {
    enum Prop {
        THREADS("threads"),
        CYCLES("cycles"),
        OPS("ops");
        String value;
        Prop(String value) {
            this.value = value;
        }
        public static Prop fromValue(String value) {
            for (Prop prop : Prop.values()) {
                if (prop.value.equals(value)) {
                    return prop;
                }
            }
            throw new NoSuchElementException("");
        }
    }

    private static int threads = 1;
    final static int warmups = 5;
    private static int cycles = 5;
    private static int ops = 100000;
    public static void main(String[] args) throws InterruptedException {
        parseArgs(args);
        final ExecutorService executor = Executors.newFixedThreadPool( threads );
        final NIOClient[] clients = new NIOClient[threads];
        for (int i = 0; i < threads ; i++) {
            clients[i] = new NIOClient(ops, i);
        }
        for (int j = 0; j < warmups ; j++) {
            CountDownLatch countDownLatch = new CountDownLatch(threads);
            for (int i = 0; i < threads ; i++) {
                clients[i].setCountDownLatch(countDownLatch);
                executor.submit(clients[i]);
            }
            countDownLatch.await();
        }
        double[] TPs = new double[cycles];
        double[] latencies = new double[cycles];
        for (int j = 0; j < cycles ; j++) {
            CountDownLatch countDownLatch = new CountDownLatch(threads);
            long s = System.nanoTime();
            for (int i = 0; i < threads ; i++) {
                clients[i].setCountDownLatch(countDownLatch);
                executor.submit(clients[i]);
            }
            countDownLatch.await();
            long e = System.nanoTime();
            latencies[j] = 0.001* (e - s);
            TPs[j] = 1e+6 * threads * ops /latencies[j];
        }
        double TP = Math.rint(Arrays.stream(TPs).sum()/cycles);
        double elapsed = Math.rint(Arrays.stream(latencies).sum()/cycles);
        System.out.println("Threads: " + threads + ". Elapsed time for " + ops + " echos: " + elapsed + " us. total TP:" + TP + " TP per thread: " + TP/threads);
    }
    private static void parseArgs(String[] args){
        for (String arg: args){
            String[] parsed = arg.split("=");
            if(parsed != null && parsed.length == 2){
                String key = parsed[0].toLowerCase();
                int value = Integer.valueOf(parsed[1]);
                switch (Prop.fromValue(key)){
                    case THREADS:
                        System.out.println("num of threads " + value);
                        threads = value;
                        break;
                    case CYCLES:
                        System.out.println("num of cycles " + value);
                        cycles = value;
                        break;
                    case OPS:
                        System.out.println("num of ops " + value);
                        ops = value;
                        break;
                }
            }
        }
    }
}
