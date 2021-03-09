package serializers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class ReusableSmartSerializer extends Serializer {
    private final ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream();
    private final ByteBufferBackedInputStream bis = new ByteBufferBackedInputStream();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private GsObjectOutput gos;
    private GsObjectInput gis;

    @Override
    public void serialize(ByteBuffer buffer, Object obj) throws IOException {
        bos.setBuffer(buffer);
        if (oos == null) {
            oos = new ObjectOutputStream(bos);
            gos = new GsObjectOutput(oos);
        } else
            oos.reset();
        gos.writeObject(obj);
        gos.flush();
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer) throws IOException {
        bis.setBuffer(buffer);
        if (ois == null) {
            ois = new ObjectInputStream(bis);
            gis = new GsObjectInput(ois);
        }

        try {
            return (T) gis.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
