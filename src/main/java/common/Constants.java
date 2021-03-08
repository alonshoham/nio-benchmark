package common;

import java.net.InetSocketAddress;

public class Constants {
    public static final String HOST = getEnv("GSN_HOST","localhost");
    public static final int PORT = getEnv("GSN_PORT",8007);
    public static final InetSocketAddress ADDRESS = new InetSocketAddress(HOST, PORT);
    public static final byte VERSION = getEnv("GSN_VERSION", (byte) 1);
    public static final int PAYLOAD = getEnv("GSN_PAYLOAD",1024);
    public static final int MAX_FRAME_LENGTH = 2 * PAYLOAD;

    public static String getEnv(String key, String defVal) {
        String s = System.getenv(key);
        return s != null ? s : defVal;
    }

    public static int getEnv(String key, int defVal) {
        String s = System.getenv(key);
        return s != null ? Integer.parseInt(s) : defVal;
    }

    public static byte getEnv(String key, byte defVal) {
        String s = System.getenv(key);
        return s != null ? Byte.parseByte(s) : defVal;
    }
}
