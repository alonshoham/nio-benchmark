package nio;


import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import static nio.NIOSingleThreadServer.PORT;

public class NIOClient implements Runnable{
    final private int operations;
    final public int clientId;
    private SocketChannel client;
    private CountDownLatch countDownLatch;

    NIOClient(int operations, int clientId) {
        this.operations = operations;
        this.clientId = clientId;
        try {
            client = SocketChannel.open(new InetSocketAddress(InetAddress
                    .getLoopbackAddress(), PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void run() {
        for (int i = 0; i < operations; i++) {
            String message = clientId + "-" + i;
            sendMessage(message + '\n');
        }
        countDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
