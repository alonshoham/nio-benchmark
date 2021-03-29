package processors;

import common.Settings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FixedReadEchoProcessor extends ReadProcessor {
    private final int expectedPayload = Settings.PAYLOAD;

    @Override
    public String getName() {
        return "fixed-read-echo (expected payload: " + expectedPayload + ")";
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = readWithTerminator(channel);
        if (buffer != null) {
            if (buffer.limit() != expectedPayload)
                logger.warn("Unexpected payload length: {}", buffer.limit());
            tryWrite(channel, buffer);
        }
    }
}
