package common;

import com.gigaspaces.lrmi.nio.async.LRMIThreadPoolExecutor;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static common.Settings.poolSize;
import static common.Settings.poolType;

public class ExecutorProvider {
    public static final Executor executor = initExecutor();

    private static Executor initExecutor() {
        LoggerFactory.getLogger(ExecutorProvider.class).info("pool size: {}, poolType: {}", poolSize, poolType);
        switch (poolType) {
            case "fixed": return Executors.newFixedThreadPool(poolSize);
            case "work-stealing": Executors.newWorkStealingPool(poolSize);
            case "dynamic": return new LRMIThreadPoolExecutor(0, poolSize, 60000, Integer.MAX_VALUE, Long.MAX_VALUE,
                    Thread.NORM_PRIORITY,
                    "LRMI-Custom",
                    true, true);
            default: throw new IllegalArgumentException("Unsupported pool type: " + poolType);
        }
    }
}
