package jmh.benchmarks.basic;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.management.GigaSpacesRuntime;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.transport.PocSettings;
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
        int threadIndex = threadParams.getThreadIndex();
        GigaSpace gigaSpace = spaceState.spaceProxies[threadIndex];
        return gigaSpace.readById(new IdQuery<SpaceDocument>(SpaceState.TYPE_NAME, threadIndex));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        private static final String spaceName = "test";
        public static final String TYPE_NAME = "Message";
        private GigaSpace[] spaceProxies;

        private GigaSpace createProxy(String spaceName) {
            return new GigaSpaceConfigurer(new SpaceProxyConfigurer(spaceName)).create();
        }

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            GigaSpace gigaSpace = createProxy(spaceName);
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

            boolean proxyPerThread = PocSettings.clientConnectionPoolType.equals("singleton");
            System.out.println("proxyPerThread: " + proxyPerThread);
            spaceProxies = new GigaSpace[benchmarkParams.getThreads()];
            for (int i = 0; i < spaceProxies.length; i++) {
                spaceProxies[i] = proxyPerThread ? createProxy(spaceName) : gigaSpace;
            }
        }

        private void shutdown() {
            System.out.println("Executing shutdown hook...");
            try {
                AsyncFuture<Serializable> future = spaceProxies[0].execute(new ShutdownTask());
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