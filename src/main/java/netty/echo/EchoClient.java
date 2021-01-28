/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package netty.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nio.NIOSingleThreadServer.PORT;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */

public class EchoClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    private final NioEventLoopGroup group;
    private ChannelFuture f;
    public final static AttributeKey ATTRIBUTE_KEY = AttributeKey.newInstance("future");


    public EchoClient() {
        // Configure the client.
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new EchoClientHandler());
                    }
                });

        // Start the client.
        try {
            f = b.connect(HOST, PORT).sync();
        }catch (Exception e){

        }

    }

    public CompletableFuture<String> sendMessageInternal(String message){
        byte[] bytes = message.getBytes(CharsetUtil.UTF_8);
        CompletableFuture<String> future = new CompletableFuture<>();
        f.channel().attr(ATTRIBUTE_KEY).set(future);
        f.channel().writeAndFlush(Unpooled.wrappedBuffer(bytes));
        return future;
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
        System.out.println("Client received: " + response);
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            int finalI = i;
            executorService.submit(() ->{
                        EchoClient echoClient = new EchoClient();
                echoClient.sendMessage("HELLO-" + finalI);
            });
        }
    }

    public void close() throws Exception{
        try{
            f.channel().closeFuture().sync();
        }finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }
}

