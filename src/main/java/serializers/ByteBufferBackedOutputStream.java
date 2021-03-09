package serializers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedOutputStream extends OutputStream {
    private ByteBuffer buf;

    public ByteBufferBackedOutputStream() {
    }

    public ByteBufferBackedOutputStream(ByteBuffer buffer) {
        this.buf = buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buf = buffer;
    }

    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        buf.put(bytes, off, len);
    }
}