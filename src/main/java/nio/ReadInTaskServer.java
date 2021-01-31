package nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.channels.*;

import static nio.NIOSingleThreadServer.PORT;

public class ReadInTaskServer
{
    Selector clientSelector;
    final Map<SocketChannel, ChannelEntry> clients = new ConcurrentHashMap<>();

    public void run(int port, int poolSize) throws IOException
    {
        final ExecutorService executor = Executors.newFixedThreadPool( poolSize );
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
//                        System.out.println("Current key: " + key);
                        key.interestOps(0);
                        executor.submit(new ChannelEntryTask(key, clients.get((SocketChannel) key.channel()), clientSelector));

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
        int poolSize = argv.length == 1 ? Integer.valueOf(argv[0]) : 4;
        System.out.println("pool size: " + poolSize);
        new ReadInTaskServer().run( PORT, poolSize);
    }

    static class ChannelEntry{
        private final SocketChannel socketChannel;
        final ByteBuffer buff = ByteBuffer.allocate(256);
        ChannelEntry(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }
        public void echo() {
            try {
//                synchronized (buff) {
                    int data = socketChannel.read(buff);
                    if (data == -1 || buff.get(buff.position() - 1) == '\n') {
                        buff.flip();
                        socketChannel.write(buff);
                        if(buff.hasRemaining()){
                            System.out.println("failed to write to buffer");
                        }
                        buff.clear();
                    }else{
                        System.out.println("failed to read from buffer");
                    }
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ChannelEntryTask implements Runnable{
        private final ChannelEntry channelEntry;
        private final SelectionKey key;
        private final Selector selector;
        ChannelEntryTask(SelectionKey key, ChannelEntry channelEntry, Selector selector) {
            this.channelEntry = channelEntry;
            this.key = key;
            this.selector = selector;
        }

        @Override
        public void run() {
            channelEntry.echo();
            key.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }
    }
}

