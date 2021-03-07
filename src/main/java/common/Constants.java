package common;

public class Constants {
    public static final int PORT = 8007;
    public static final int MAX_PAYLOAD = 1024;

    public static final byte V1_FIXED_READ_ECHO = 1;
    public static final byte V2_FIXED_READ_SUBMIT_ECHO = 2;

    public static String getEnv(String key, String defVal) {
        String s = System.getenv(key);
        return s != null ? s : defVal;
    }
}
