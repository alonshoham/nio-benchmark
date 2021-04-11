package jmh.benchmarks.basic;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.internal.utils.GsEnv;
import com.gigaspaces.management.GigaSpacesRuntime;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.query.IdQuery;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.io.Serializable;

@State(Scope.Benchmark)
public class ReadByIdBenchmark {

    @Benchmark
    public Object testReadById(SpaceState spaceState, ThreadParams threadParams) {
        //return spaceState.gigaSpace.readById(Message.class, String.valueOf(threadParams.getThreadIndex()));
        return spaceState.gigaSpace.readById(new IdQuery<SpaceDocument>(SpaceState.TYPE_NAME, threadParams.getThreadIndex()));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private GigaSpace gigaSpace;

        public static final String TYPE_NAME = "Message";

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            //System.setProperty("com.gs.nio.type", "lrmi");
            //System.setProperty("com.gs.nio.type", "nio");
            //System.setProperty("com.gs.nio.type", "netty");
            //System.setProperty("com.gs.nio.host", "192.168.68.108");
            //System.setProperty("com.gs.nio.enabled", "false");
            gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("test")
                    .lookupLocators(GsEnv.get("LOOKUP_LOCATORS", "127.0.0.1"))
                    .lookupTimeout(15000))
                    .create();
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            gigaSpace.clear(null);
            gigaSpace.getTypeManager().registerTypeDescriptor(new SpaceTypeDescriptorBuilder(TYPE_NAME)
                    .addFixedProperty("id", int.class)
                    .addFixedProperty("payload", String.class)
                    .idProperty("id")
                    .create());
            for(int i = 0 ; i < benchmarkParams.getThreads() ; i++) {
                //gigaSpace.write(new Message().setId(String.valueOf(i)).setPayload("foo"));
                gigaSpace.write(new SpaceDocument(TYPE_NAME)
                        .setProperty("id", i)
                        .setProperty("payload", "foo"));
            }
        }

        private void shutdown() {
            System.out.println("Executing shutdown hook...");
            //gigaSpace.clear(null);
            //System.out.println("Space cleared");
            try {
                AsyncFuture<Serializable> future = gigaSpace.execute(new ShutdownTask());
                future.get();
                System.out.println("shutdown hook succeeded");
            } catch (Throwable e) {
                System.out.println("shutdown hook failed");
                e.printStackTrace();
            } finally {
                GigaSpacesRuntime.shutdown();
                System.out.println("shutdown hook completed");
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                .threads(threads)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}