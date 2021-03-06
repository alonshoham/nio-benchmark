package nio;

import common.ExecutorProvider;
import common.Settings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ReadInTaskServer
{
    Selector clientSelector;
    final Map<SocketChannel, ChannelEntry> clients = new ConcurrentHashMap<>();

    public void run() throws IOException
    {
        final Executor executor = ExecutorProvider.executor;
        clientSelector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(Settings.ADDRESS);
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
                        executor.execute(new ChannelEntryTask(key, clients.get(key.channel()), clientSelector));

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

    public static void main(String[] args) throws IOException {
        Settings.parseArgs(args);
        new ReadInTaskServer().run();
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

