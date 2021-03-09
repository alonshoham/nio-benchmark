package serializers;

import java.io.*;
import java.nio.ByteBuffer;

public class ReusableSerializer extends Serializer {
    private final ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream();
    private final ByteBufferBackedInputStream bis = new ByteBufferBackedInputStream();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    @Override
    public void serialize(ByteBuffer buffer, Object obj) throws IOException {
        bos.setBuffer(buffer);
        if (oos == null)
            oos = new ObjectOutputStream(bos);
        else
            oos.reset();
        oos.writeObject(obj);
        oos.flush();
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer) throws IOException {
        bis.setBuffer(buffer);
        if (ois == null)
            ois = new ObjectInputStream(bis);

        try {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
