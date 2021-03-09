package common;

import serializers.SmartExternalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.Supplier;

public class Response implements SmartExternalizable {
    private byte[] payload;

    public Response() {
    }

    public Response(byte[] payload) {
        this.payload = payload;
    }


    @Override
    public Supplier<Externalizable> getFactory() {
        return Response::new;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int length = in.readInt();
        this.payload = new byte[length];
        in.read(payload);
    }
}
