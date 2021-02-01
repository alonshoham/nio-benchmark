package nio;

import com.gigaspaces.lrmi.nio.async.LRMIThreadPoolExecutor;
import com.j_spaces.kernel.threadpool.DynamicThreadPoolExecutor;
import com.j_spaces.kernel.threadpool.queue.DynamicQueue;
import sun.management.jmxremote.LocalRMIServerSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static nio.NIOSingleThreadServer.PORT;
import static nio.Util.*;

public class ReadInSelectorServer
{
    Selector clientSelector;
    final Map<SocketChannel, ChannelEntry> clients = new ConcurrentHashMap<>();

    public void run( int port) throws IOException
    {
        final ExecutorService executor;
        if(poolType.equals("fixed"))
            executor = Executors.newFixedThreadPool( poolSize );
        else if(poolType.equals("work-stealing"))
            executor = Executors.newWorkStealingPool( poolSize );
        else if(poolType.equals("dynamic"))
            executor = new LRMIThreadPoolExecutor(0, poolSize, 60000, Integer.MAX_VALUE, Long.MAX_VALUE,
                    Thread.NORM_PRIORITY,
                    "LRMI-Custom",
                    true, true);
        else throw new IllegalArgumentException("");
        System.out.println("pool size: " + poolSize + " poolType: " + poolType);
        clientSelector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        InetSocketAddress sa =  new InetSocketAddress( InetAddress
                .getLoopbackAddress(), port );
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
                        ChannelEntry entry = clients.get((SocketChannel) key.channel());
                        if(entry == null)
                            throw new RuntimeException();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        SocketChannel channel = entry.socketChannel;
                        int data = channel.read(buffer);
                        if (data == -1 || buffer.get(buffer.position() - 1) == '\n') {
                            executor.submit(new ChannelEntryTask(key, entry, clientSelector, buffer));
                        }else{
                            System.out.println("failed to read from buffer. data = " + data);
                        }

                    } else{
                        System.out.println("UNEXPECTED KEY");
                    }
                }
            } catch ( IOException e ) { System.out.println(e); }
        }
    }

    void acceptClient( ServerSocketChannel ssc ) throws IOException
    {
        SocketChannel clientSocket = ssc.accept();
        clientSocket.configureBlocking(false);
        clientSocket.register( clientSelector, SelectionKey.OP_READ );
        clientSocket.socket().setTcpNoDelay(true);
        clients.put(clientSocket, new ChannelEntry(clientSocket));
        System.out.println("Added new client");
    }

    public static void main( String argv[] ) throws IOException {
       parseArgs(argv);
        new ReadInSelectorServer().run( PORT);
    }

    static class ChannelEntry{
        private final SocketChannel socketChannel;
        ChannelEntry(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }
        public void echo(ByteBuffer buff) {
            try {
                buff.flip();
                socketChannel.write(buff);
                if(buff.hasRemaining()) {
                    System.out.println("failed to write to buffer");
                }
                buff.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ChannelEntryTask implements Runnable{
        private final ChannelEntry channelEntry;
        private final SelectionKey key;
        private final Selector selector;
        private final ByteBuffer buffer;
        ChannelEntryTask(SelectionKey key, ChannelEntry channelEntry, Selector selector, ByteBuffer buffer) {
            this.channelEntry = channelEntry;
            this.key = key;
            this.selector = selector;
            this.buffer = buffer;
        }
        @Override
        public void run() {
            channelEntry.echo(buffer);
//            key.interestOps(SelectionKey.OP_READ);
//            selector.wakeup();
        }
    }
}

