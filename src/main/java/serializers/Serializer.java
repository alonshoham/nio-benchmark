package serializers;

import common.Settings;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Serializer {
    private static final int BYTES_INTEGER = 4;

    public static Serializer createSerializer() {
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

    public abstract void serialize(ByteBuffer buffer, Object obj) throws IOException;

    public abstract <T> T deserialize(ByteBuffer buffer) throws IOException;

    public void serializeWithLength(ByteBuffer buffer, Object obj) throws IOException {
        // Save position before write:
        int prevPos = buffer.position();
        // Skip ahead, save bytes for unknown length (int):
        buffer.position(prevPos + BYTES_INTEGER);
        // serialize:
        serialize(buffer, obj);
        // Prepend length in before payload:
        buffer.putInt(prevPos, buffer.position() - prevPos - BYTES_INTEGER);
    }
}
