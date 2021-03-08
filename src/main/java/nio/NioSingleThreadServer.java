package nio;

import common.Constants;
import common.RequestType;
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

import static common.Util.*;

public class NioSingleThreadServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Selector selector;
    private final ServerSocketChannel ssc;

    public NioSingleThreadServer() throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(Constants.ADDRESS);
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

    void acceptClient( ServerSocketChannel ssc ) throws IOException {
        SocketChannel clientSocket = ssc.accept();
        byte code = (byte) clientSocket.socket().getInputStream().read();
        ReadProcessor processor = initReader(code);
        clientSocket.configureBlocking(false);
        clientSocket.socket().setTcpNoDelay(true);
        clientSocket.register(selector, SelectionKey.OP_READ, processor);
        logger.info("Added new client {} with processor {}", clientSocket, processor.getName());
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

    public static void main( String argv[] ) throws IOException {
       parseArgs(argv);
        new NioSingleThreadServer().run();
    }
}
