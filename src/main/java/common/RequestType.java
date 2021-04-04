package common;

public enum RequestType {
    V0_TERMINATE(0),
    V1_FIXED_READ_ECHO(1),
    V2_FIXED_READ_SUBMIT_ECHO(2),
    V3_DYNAMIC_READ_REPLY(3),
    V4_DYNAMIC_READ_SUBMIT_REPLY(4),
    V5_REQUEST_RESPONSE(5);

    RequestType(int version) {
        this.version = (byte) version;
    }

    public final byte version;

    public byte getVersion() {
        return version;
    }

    public static RequestType valueOf(byte code) {
        return values()[code];
    }
}
