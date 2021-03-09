package serializers;

import java.io.Externalizable;
import java.util.function.Supplier;

public interface SmartExternalizable extends Externalizable {
    Supplier<Externalizable> getFactory();
}
