package jmh.benchmarks.basic;

import org.openspaces.core.executor.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Executors;

public class ShutdownTask implements Task<Serializable> {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownTask.class);
    @Override
    public Serializable execute() {
        logger.info("Shutdown request accepted");
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                logger.info("Shutdown thread started - sleeping before shutting down...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.info("Shutdown request interrputed");
                    e.printStackTrace();
                } finally {
                    logger.info("Shutting down...");
                    System.exit(0);
                }
            }
        });
        return null;
    }
}
