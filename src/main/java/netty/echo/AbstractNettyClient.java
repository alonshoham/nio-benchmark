package netty.echo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

abstract public class AbstractNettyClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    public volatile CompletableFuture completableFuture;
    protected ChannelFuture f;

    public CompletableFuture<String> sendMessageInternal(String message){
        byte[] bytes = message.getBytes(CharsetUtil.UTF_8);
        this.completableFuture = new CompletableFuture();
        f.channel().writeAndFlush(Unpooled.wrappedBuffer(bytes));
        return completableFuture;
    }

    public void sendMessage(String message) {
        String response = null;
        try {
            response = sendMessageInternal(message).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        System.out.println("Client received: " + response);
    }
}
