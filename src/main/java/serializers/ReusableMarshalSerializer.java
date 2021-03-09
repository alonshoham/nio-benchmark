package serializers;

import com.gigaspaces.internal.io.MarshalInputStream;
import com.gigaspaces.internal.io.MarshalOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReusableMarshalSerializer extends Serializer {
    private final ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream();
    private final ByteBufferBackedInputStream bis = new ByteBufferBackedInputStream();
    private MarshalOutputStream oos;
    private MarshalInputStream ois;

    @Override
    public void serialize(ByteBuffer buffer, Object obj) throws IOException {
        bos.setBuffer(buffer);
        if (oos == null)
            oos = new MarshalOutputStream(bos);
        else
            oos.reset();
        oos.writeObject(obj);
        oos.flush();
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer) throws IOException {
        bis.setBuffer(buffer);
        if (ois == null)
            ois = new MarshalInputStream(bis);

        try {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
