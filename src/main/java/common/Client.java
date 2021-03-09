package common;

import serializers.Serializer;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client implements Closeable {
    private final SocketChannel channel;
    public final Serializer serializer = Serializer.createSerializer();
    private final ByteBuffer header = ByteBuffer.allocateDirect(4);

    public Client() throws IOException {
        channel = SocketChannel.open(Settings.ADDRESS);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public void writeBlocking(byte v) throws IOException {
        channel.socket().getOutputStream().write(v);
    }

    public void writeBlocking(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining())
            channel.write(buffer);
    }

     public void readBlocking(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1)
                throw new EOFException("No more data in channel " + channel);
        }
        buffer.flip();
    }

    public ByteBuffer readBlockingWithLength() throws IOException {
        header.position(0);
        readBlocking(header);
        ByteBuffer buffer = ByteBuffer.allocate(header.getInt());
        readBlocking(buffer);
        return buffer;
    }
}
