package serializers;

import java.io.*;
import java.nio.ByteBuffer;

public class DefaultSerializer extends Serializer {
    @Override
    public void serialize(ByteBuffer buffer, Object obj) throws IOException {
        try (OutputStream os = new ByteBufferBackedOutputStream(buffer);
             ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
        }
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer) throws IOException {
        try (InputStream is = new ByteBufferBackedInputStream(buffer);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
