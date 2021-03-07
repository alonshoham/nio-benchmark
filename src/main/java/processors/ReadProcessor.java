package processors;

import common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class ReadProcessor {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public abstract String getName();
    public abstract void read(SocketChannel channel) throws IOException;

    protected ByteBuffer readWithTerminator(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_PAYLOAD);
        int data = channel.read(buffer);
        if (data == -1)
            throw new EOFException("No data in channel " + channel);
        if (buffer.get(buffer.position() - 1) == '\n') {
            buffer.flip();
            return buffer;
        } else {
            logger.warn("failed to read from buffer. data = " + data);
            return null;
        }
    }

    protected void tryWrite(SocketChannel channel, ByteBuffer buff) throws IOException {
        channel.write(buff);
        if (buff.hasRemaining()) {
            logger.warn("failed to write to buffer - {} bytes remaining", buff.remaining());
        }
    }
}
