package nio;

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

public class NIOSingleThreadServer
{
    public final static int PORT = 1235;
    private static Selector selector;
    public void run( int port) throws IOException
    {
        selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress sa =  new InetSocketAddress( InetAddress
                .getLoopbackAddress(), port );
        ssc.socket().bind( sa );
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            while ( selector.select(100) == 0 );
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    register(ssc);
                }

                if (key.isReadable()) {
                    echo(buffer, key);
                }

            }
        }
    }
    private static void register(ServerSocketChannel serverSocket)
            throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }


    private static void echo(ByteBuffer buffer, SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        if(client.read(buffer) == -1 || buffer.get(buffer.position()-1) == '\n'){
            buffer.flip();
            client.write(buffer);
            buffer.clear();
        }

    }

    public static void main( String argv[] ) throws IOException {
        //new LargerHttpd().run( Integer.parseInt(argv[0]), 3/*threads*/ );
        new NIOSingleThreadServer().run( PORT);
    }
}

