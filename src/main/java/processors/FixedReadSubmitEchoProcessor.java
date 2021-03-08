package processors;

import common.ExecutorProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

public class FixedReadSubmitEchoProcessor extends ReadProcessor {
    private final Executor executor = ExecutorProvider.executor;

    @Override
    public String getName() {
        return "fixed-read-submit-echo";
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = readWithTerminator(channel);
        if (buffer != null) {
            executor.execute(() -> {
                try {
                    tryWrite(channel, buffer);
                } catch (IOException e) {
                    logger.error("Failed to write buffer", e);
                }
            });
        }
    }
}
