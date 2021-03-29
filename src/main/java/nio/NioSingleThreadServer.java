package nio;

import common.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processors.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioSingleThreadServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Selector selector;
    private final ServerSocketChannel ssc;

    public NioSingleThreadServer() throws IOException {
        logger.info("Binding to {}", Settings.ADDRESS);
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(Settings.ADDRESS);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT );
    }

    public void run() {
        while (true) {
            try {
                while ( selector.select(100) == 0 );
                Set<SelectionKey> readySet = selector.selectedKeys();
                for (Iterator<SelectionKey> it=readySet.iterator(); it.hasNext();)
                {
                    final SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        acceptClient(ssc);
                    } else if(key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        try {
                            ((ReadProcessor)key.attachment()).read(channel);
                        } catch (IOException e) {
                            logger.warn("Failed to read from " + channel + " - cancelling key", e);
                            key.cancel();
                        }
                    } else{
                        logger.warn("UNEXPECTED KEY");
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to process selector", e);
            }
        }
    }

    private void acceptClient(ServerSocketChannel ssc) throws IOException {
        SocketChannel clientSocket = ssc.accept();
        byte code = (byte) clientSocket.socket().getInputStream().read();
        ReadProcessor processor = ReadProcessor.initReader(code);
        clientSocket.configureBlocking(false);
        Settings.initSocketChannel(clientSocket);
        clientSocket.register(selector, SelectionKey.OP_READ, processor);
        logger.info("Added new client {} with processor {}", clientSocket, processor.getName());
    }

    public static void main(String[] args) throws IOException {
       Settings.parseArgs(args);
        new NioSingleThreadServer().run();
    }
}
