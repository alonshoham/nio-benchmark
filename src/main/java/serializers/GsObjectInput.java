package serializers;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GsObjectInput extends ObjectInputAdapter {
    private static final short CODE_NULL = 0;
    private static final short CODE_OBJECT = 1;

    private final Map<Short, Supplier<Externalizable>> map = new HashMap<>();

    public GsObjectInput(ObjectInput input) {
        super(input);
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        short code = input.readShort();
        if (code == CODE_NULL)
            return null;
        if (code == CODE_OBJECT)
            return input.readObject();
        Supplier<Externalizable> factory = map.get(code);
        if (factory != null) {
            Externalizable result = factory.get();
            result.readExternal(input);
            return result;
        } else {
            SmartExternalizable result = (SmartExternalizable) input.readObject();
            map.put(code, result.getFactory());
            return result;
        }
    }
}
