package common;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

public class Settings {
    public static final String HOST = getEnv("GSN_HOST","localhost");
    public static final int PORT = getEnv("GSN_PORT",8007);
    public static final InetSocketAddress ADDRESS = new InetSocketAddress(HOST, PORT);
    public static final byte VERSION = getEnv("GSN_VERSION", (byte) 1);
    public static final int PAYLOAD = getEnv("GSN_PAYLOAD",1024);
    public static final int MAX_FRAME_LENGTH = 2 * PAYLOAD;
    public static String poolType = "fixed";
    public static int poolSize = 4;
    private static final boolean CHANNEL_TCP_NODELAY = true;

    public static void parseArgs(String[] args) {
        if(args != null) {
            for (String arg: args){
                String[] parsed = arg.split("=");
                if(parsed.length == 2) {
                    String key = parsed[0];
                    String value = parsed[1];
                    if (key.equals("poolSize"))
                        poolSize = Integer.parseInt(value);
                    if (key.equals("poolType"))
                        poolType = value.toLowerCase();
                }
            }
        }
    }

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

    public static void initSocketChannel(SocketChannel channel)
            throws IOException {
        channel.setOption(StandardSocketOptions.TCP_NODELAY, CHANNEL_TCP_NODELAY);
    }

    public static void initChildOptions(ServerBootstrap serverBootstrap) {
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, CHANNEL_TCP_NODELAY);
    }
}
