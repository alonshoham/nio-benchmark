package common;

import serializers.SmartExternalizable;

import java.io.*;
import java.util.function.Supplier;

public class Request implements SmartExternalizable {
    private byte[] payload;

    public Request() {
    }

    public Request(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public Supplier<Externalizable> getFactory() {
        return Request::new;
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
