package jmh.benchmarks.basic;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.query.IdQuery;
import jmh.model.Message;
import jmh.utils.GigaSpaceFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openspaces.core.GigaSpace;

import java.rmi.RemoteException;

import static jmh.utils.DefaultProperties.*;

@State(Scope.Benchmark)
public class ReadByIdBenchmark {

    @Benchmark
    public Object testReadById(SpaceState spaceState, ThreadParams threadParams) {
        //return spaceState.gigaSpace.readById(Message.class, String.valueOf(threadParams.getThreadIndex()));
        return spaceState.gigaSpace.readById(new IdQuery<SpaceDocument>(SpaceState.TYPE_NAME, threadParams.getThreadIndex()));
    }

    @State(Scope.Benchmark)
    public static class SpaceState {

        @Param({MODE_EMBEDDED, MODE_REMOTE})
        private static String mode;

        private GigaSpace gigaSpace;

        public static final String TYPE_NAME = "Message";

        @Setup
        public void setup(BenchmarkParams benchmarkParams) {
            //System.setProperty("com.gs.nio.type", "lrmi");
            //System.setProperty("com.gs.nio.type", "nio");
            //System.setProperty("com.gs.nio.type", "netty");
            //System.setProperty("com.gs.nio.host", "192.168.68.108");
            //System.setProperty("com.gs.nio.enabled", "false");
            gigaSpace = GigaSpaceFactory.getOrCreateSpace(DEFAULT_SPACE_NAME, mode.equals(MODE_EMBEDDED));
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

        @TearDown
        public void teardown() {
            if (mode.equals(MODE_EMBEDDED)) {
                try {
                    gigaSpace.getSpace().getDirectProxy().shutdown();
                } catch (RemoteException e) {
                    System.err.println("failed to shutdown Space" + e);
                }
            }
        }
    }


    public static void main(String[] args) throws RunnerException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        Options opt = new OptionsBuilder()
                .include(ReadByIdBenchmark.class.getName())
                //.param(PARAM_MODE, MODE_EMBEDDED)
                .param(PARAM_MODE, MODE_REMOTE)
                .threads(threads)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}