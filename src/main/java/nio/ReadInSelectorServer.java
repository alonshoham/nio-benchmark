package nio;

import com.gigaspaces.lrmi.nio.async.LRMIThreadPoolExecutor;
import common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nio.Util.*;

public class ReadInSelectorServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    Selector clientSelector;

    public void run( int port) throws IOException
    {
        final ExecutorService executor = initExecutor();
        clientSelector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        InetSocketAddress sa =  new InetSocketAddress( InetAddress.getLoopbackAddress(), port );
        ssc.socket().bind( sa );
        ssc.register( clientSelector, SelectionKey.OP_ACCEPT );

        while ( true ) {
            try {
                while ( clientSelector.select(100) == 0 );
                Set<SelectionKey> readySet = clientSelector.selectedKeys();
                for(Iterator<SelectionKey> it=readySet.iterator();
                    it.hasNext();)
                {
                    final SelectionKey key = it.next();
                    it.remove();
                    if ( key.isAcceptable() ) {
                        acceptClient( ssc );
                    } else if(key.isReadable()){
//                        key.interestOps(0);
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_PAYLOAD);
                        try {
                            int data = channel.read(buffer);
                            if (data == -1 || buffer.get(buffer.position() - 1) == '\n') {
                                executor.submit(new ChannelEntryTask(channel, buffer));
                            } else {
                                logger.warn("failed to read from buffer. data = " + data);
                            }
                        } catch (IOException e) {
                            logger.warn("Failed to read from " + channel + " - cancelling key" + System.lineSeparator() + e);
                            key.cancel();
                        }
                    } else{
                        logger.warn("UNEXPECTED KEY");
                    }
                }
            } catch ( IOException e ) { logger.error("Failed to process selector", e); }
        }
    }

    void acceptClient( ServerSocketChannel ssc ) throws IOException
    {
        SocketChannel clientSocket = ssc.accept();
        clientSocket.configureBlocking(false);
        clientSocket.socket().setTcpNoDelay(true);
        clientSocket.register( clientSelector, SelectionKey.OP_READ);
        logger.info("Added new client {}", clientSocket);
    }

    private static void echo(SocketChannel channel, ByteBuffer buff) {
        try {
            buff.flip();
            channel.write(buff);
            if (buff.hasRemaining()) {
                System.out.println("failed to write to buffer");
            }
            buff.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ExecutorService initExecutor() {
        logger.info("pool size: {}, poolType: {}", poolSize, poolType);
        switch (poolType) {
            case "fixed": return Executors.newFixedThreadPool(poolSize);
            case "work-stealing": Executors.newWorkStealingPool(poolSize);
            case "dynamic": return new LRMIThreadPoolExecutor(0, poolSize, 60000, Integer.MAX_VALUE, Long.MAX_VALUE,
                    Thread.NORM_PRIORITY,
                    "LRMI-Custom",
                    true, true);
            default: throw new IllegalArgumentException("Unsupported pool type: " + poolType);
        }
    }

    public static void main( String argv[] ) throws IOException {
       parseArgs(argv);
        new ReadInSelectorServer().run(Constants.PORT);
    }

    static class ChannelEntryTask implements Runnable{
        private final SocketChannel channel;
        private final ByteBuffer buffer;
        ChannelEntryTask(SocketChannel channel, ByteBuffer buffer) {
            this.channel = channel;
            this.buffer = buffer;
        }
        @Override
        public void run() {
            echo(channel, buffer);
//            key.interestOps(SelectionKey.OP_READ);
//            selector.wakeup();
        }
    }
}

