package serializers.netty;

import io.netty.buffer.ByteBuf;
import serializers.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReusableSmartSerializer extends NettySerializer {
    private final ByteBufBackedOutputStream bos = new ByteBufBackedOutputStream();
    private final ByteBufBackedInputStream bis = new ByteBufBackedInputStream();
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private GsObjectOutput gos;
    private GsObjectInput gis;

    @Override
    public void serialize(ByteBuf buffer, Object obj) throws IOException {
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
    public <T> T deserialize(ByteBuf buffer) throws IOException {
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
