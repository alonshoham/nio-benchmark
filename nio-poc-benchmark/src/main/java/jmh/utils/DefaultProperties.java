package jmh.utils;

public class DefaultProperties {
    public static final int FORKS_DEFAULT = 1;
    //public static final int DEFAULT_TIMEOUT = 24 * 60 * 1000;
    public static final int WARMUP_ITERATIONS_DEFAULT = 5;
    public static final int MEASUREMENT_ITERATIONS_DEFAULT = 25;
    public static final String DEFAULT_SPACE_NAME = "test";
    public static final String[] JVM_ARGS_EMBEDDED_DEFAULT = "-Xms1g -Xmx1g -XX:+UseSerialGC".split(" ");
    public static final String[] JVM_ARGS_REMOTE_DEFAULT = "-Dcom.gs.ops-ui.enabled=false -Xms1g -Xmx1g -XX:+UseSerialGC".split(" ");

    public static final String PARAM_MODE="mode";
    public static final String MODE_EMBEDDED="embedded";
    public static final String MODE_REMOTE="remote";

    public static final String PARAM_SQL_ENABLE_VALIDATION = "enableValidation";
    public static final String SQL_ENABLE_VALIDATION = "true";
}