package bio;

import common.Settings;
import common.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processors.*;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static common.Util.parseArgs;

public class BioServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServerSocketChannel ssc;
    private final List<BioChannel> channels = new ArrayList<>();

    public BioServer() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.bind(Settings.ADDRESS);
    }

    public void run() {
        try {
            while (true) {
                SocketChannel clientSocket = ssc.accept();
                byte code = (byte) clientSocket.socket().getInputStream().read();
                ReadProcessor processor = initReader(code);
                Settings.initSocketChannel(clientSocket);
                channels.add(new BioChannel(clientSocket, processor));
            }
        } catch (IOException e) {
            logger.error("Failed to process selector", e);
        }
    }

    private ReadProcessor initReader(byte code) {
        switch (RequestType.valueOf(code)) {
            case V1_FIXED_READ_ECHO: return new FixedReadEchoProcessor();
            case V2_FIXED_READ_SUBMIT_ECHO: return new FixedReadSubmitEchoProcessor();
            case V3_DYNAMIC_READ_REPLY: return new DynamicReadReplyProcessor();
            case V4_DYNAMIC_READ_SUBMIT_REPLY: return new DynamicReadSubmitReplyProcessor();
            default: throw new IllegalArgumentException("Unsupported code: " + code);
        }
    }

    public static void main(String args[]) throws IOException {
        parseArgs(args);
        new BioServer().run();
    }
}
