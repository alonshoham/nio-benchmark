package serializers;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class GsObjectOutput extends ObjectOutputAdapter {
    private static final short CODE_NULL = 0;
    private static final short CODE_OBJECT = 1;

    private final Map<Class<?>,Short> map = new HashMap<>();
    private short nextCode = 2;

    public GsObjectOutput(ObjectOutput output) {
        super(output);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            output.writeShort(CODE_NULL);
        } else if (obj instanceof SmartExternalizable) {
            Class<?> clazz = obj.getClass();
            Short code = map.get(clazz);
            if (code != null) {
                output.writeShort(code);
                ((SmartExternalizable)obj).writeExternal(output);
            } else {
                code = nextCode++;
                map.put(clazz, code);
                output.writeShort(code);
                output.writeObject(obj);
            }
        } else {
            output.writeShort(CODE_OBJECT);
            output.writeObject(obj);
        }
    }
}
