package processors;

import common.RequestType;
import common.Settings;
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
        ByteBuffer buffer = ByteBuffer.allocate(Settings.MAX_FRAME_LENGTH);
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

    protected boolean readNonBlocking(SocketChannel channel, ByteBuffer buf) throws IOException {
        int read = channel.read(buf);
        if (read == -1)
            throw new EOFException("No data in channel " + channel);
        if (buf.hasRemaining())
            return false;
        buf.flip();
        return true;
    }

    protected void tryWrite(SocketChannel channel, ByteBuffer buf) throws IOException {
        channel.write(buf);
        if (buf.hasRemaining()) {
            logger.warn("failed to write to buffer - {} bytes remaining", buf.remaining());
        }
    }

    public static ReadProcessor initReader(byte code) {
        switch (RequestType.valueOf(code)) {
            case V0_TERMINATE:
                LoggerFactory.getLogger(ReadProcessor.class).info("Received exit command - terminating...");
                System.exit(0);
            case V1_FIXED_READ_ECHO: return new FixedReadEchoProcessor();
            case V2_FIXED_READ_SUBMIT_ECHO: return new FixedReadSubmitEchoProcessor();
            case V3_DYNAMIC_READ_REPLY: return new DynamicReadReplyProcessor();
            case V4_DYNAMIC_READ_SUBMIT_REPLY: return new DynamicReadSubmitReplyProcessor();
            case V5_REQUEST_RESPONSE: return new RequestResponseProcessor();
            default: throw new IllegalArgumentException("Unsupported code: " + code);
        }
    }

}
