package serializers.netty;

import io.netty.buffer.ByteBuf;
import serializers.ByteBufferBackedInputStream;
import serializers.ByteBufferBackedOutputStream;
import serializers.Serializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class ReusableSerializer extends NettySerializer {
    private final ByteBufBackedOutputStream bos = new ByteBufBackedOutputStream();
    private final ByteBufBackedInputStream bis = new ByteBufBackedInputStream();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    @Override
    public void serialize(ByteBuf buffer, Object obj) throws IOException {
        bos.setBuffer(buffer);
        if (oos == null)
            oos = new ObjectOutputStream(bos);
        else
            oos.reset();
        oos.writeObject(obj);
        oos.flush();
    }

    @Override
    public <T> T deserialize(ByteBuf buffer) throws IOException {
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
