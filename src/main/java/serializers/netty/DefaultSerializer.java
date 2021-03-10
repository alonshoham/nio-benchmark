package serializers.netty;

import io.netty.buffer.ByteBuf;

import java.io.*;

public class DefaultSerializer extends NettySerializer {
    @Override
    public void serialize(ByteBuf buffer, Object obj) throws IOException {
        try (OutputStream os = new ByteBufBackedOutputStream(buffer);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    @Override
    public <T> T deserialize(ByteBuf buffer) throws IOException {
        try (InputStream is = new ByteBufBackedInputStream(buffer);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
