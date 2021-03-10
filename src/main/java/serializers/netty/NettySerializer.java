package serializers.netty;

import common.Settings;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public abstract class NettySerializer {
    public static NettySerializer createSerializer() {
        String serializer = Settings.SERIALIZER;
        System.out.println("Serializer: " + serializer);
        switch (serializer) {
            case "DefaultSerializer": return new DefaultSerializer();
            case "ReusableSerializer": return new ReusableSerializer();
            case "ReusableMarshalSerializer": return new ReusableMarshalSerializer();
            case "ReusableSmartSerializer": return new ReusableSmartSerializer();
            default: throw new IllegalArgumentException("Unsupported serializer: " + serializer);
        }
    }

    public abstract void serialize(ByteBuf buffer, Object obj) throws IOException;

    public abstract <T> T deserialize(ByteBuf buffer) throws IOException;
}
