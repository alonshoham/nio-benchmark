package processors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FixedReadEchoProcessor extends ReadProcessor {

    @Override
    public String getName() {
        return "fixed-read-echo";
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = readWithTerminator(channel);
        if (buffer != null)
            tryWrite(channel, buffer);
    }
}
