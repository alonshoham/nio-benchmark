package bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processors.ReadProcessor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class BioChannel implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SocketChannel channel;
    private final ReadProcessor processor;

    public BioChannel(SocketChannel channel, ReadProcessor processor) {
        logger.info("Added new client {} with processor {}", channel, processor.getName());
        this.channel = channel;
        this.processor = processor;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                processor.read(channel);
            }
        } catch (IOException e) {
            logger.warn("Error while reading from {}", channel, e);
        }
    }
}
