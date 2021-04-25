package jmh.benchmarks.basic;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.internal.client.spaceproxy.operations.ReadTakeEntrySpaceOperationRequest;
import com.gigaspaces.internal.lrmi.stubs.LRMISpaceImpl;
import com.gigaspaces.management.GigaSpacesRuntime;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.query.IdQuery;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.rmi.RemoteException;

@State(Scope.Benchmark)
public class DirectNioReadByIdBenchmark {

    @Benchmark
    public Object testReadById(SpaceState spaceState, ThreadParams threadParams) throws RemoteException {
        return spaceState.directProxy.executeOperation(SpaceState.readByIdRequest);
    }

    @State(Scope.Thread)
    public static class SpaceState {
        private static final String TYPE_NAME = "Message";
        private static final GigaSpace gigaSpace;
        private static final ReadTakeEntrySpaceOperationRequest readByIdRequest;
        private LRMISpaceImpl directProxy;

        static {
            final String spaceName = "test";

            gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer(spaceName)).create();
            Runtime.getRuntime().addShutdownHook(new Thread(SpaceState::shutdown));
            gigaSpace.clear(null);
            gigaSpace.getTypeManager().registerTypeDescriptor(new SpaceTypeDescriptorBuilder(TYPE_NAME)
                    .addFixedProperty("id", int.class)
                    .addFixedProperty("payload", String.class)
                    .idProperty("id")
                    .create());
            gigaSpace.write(new SpaceDocument(TYPE_NAME)
                    .setProperty("id", 1)
                    .setProperty("payload", "foo"));

            // execute read to init request cache.
            gigaSpace.readById(new IdQuery<SpaceDocument>(TYPE_NAME, 1));
            try {
                readByIdRequest = getDirectProxy(gigaSpace).getConnectionPool().acquire().cachedRequestObject;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static LRMISpaceImpl getDirectProxy(GigaSpace gigaSpace) {
            return (LRMISpaceImpl) gigaSpace.getSpace().getDirectProxy().getRemoteJSpace();
        }

        @Setup
        public void setup() {
            directProxy = getDirectProxy(gigaSpace).createCopy();
        }

        private static void shutdown() {
            System.out.println("Executing shutdown hook...");
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
                .include(DirectNioReadByIdBenchmark.class.getName())
                .threads(threads)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}