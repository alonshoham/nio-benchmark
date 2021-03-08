package processors;

import common.ExecutorProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

public class DynamicReadSubmitReplyProcessor extends ReadProcessor {
    private final ByteBuffer header = ByteBuffer.allocateDirect(4);
    private ByteBuffer request;
    private final Executor executor = ExecutorProvider.executor;

    @Override
    public String getName() {
        return "dynamic-read-submit-reply";
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        if (request == null) {
            if (!readNonBlocking(channel, header))
                return;
            int length = header.getInt();
            this.request = ByteBuffer.allocate(length);
        }
        if (!readNonBlocking(channel, request))
            return;
        // Create response (copy of request, with length header):
        int length = request.limit();
        ByteBuffer response = ByteBuffer.allocate(4 + length);
        response.putInt(length);
        response.put(request);
        response.flip();
        // Reset:
        header.position(0);
        request = null;
        executor.execute(() -> {
            try {
                tryWrite(channel, response);
            } catch (IOException e) {
                logger.error("Failed to write buffer", e);
            }
        });
    }
}
