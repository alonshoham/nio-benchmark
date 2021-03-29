package bio;

import common.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processors.*;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

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
                ReadProcessor processor = ReadProcessor.initReader(code);
                Settings.initSocketChannel(clientSocket);
                channels.add(new BioChannel(clientSocket, processor));
            }
        } catch (IOException e) {
            logger.error("Failed to process selector", e);
        }
    }

    public static void main(String[] args) throws IOException {
        Settings.parseArgs(args);
        new BioServer().run();
    }
}
