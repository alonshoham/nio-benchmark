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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import static nio.NIOSingleThreadServer.PORT;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
@State(Scope.Thread)
public class NettyNioClient extends AbstractNettyClient{
    private final NioEventLoopGroup group;

    public NettyNioClient() {
        // Configure the client.
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        NettyNioClient client = this;
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new EchoClientHandler(client));
                    }
                });

        // Start the client.
        try {
            f = b.connect(HOST, PORT).sync();
        }catch (Exception e){

        }

    }



//    public static void main(String[] args) {
//        ExecutorService executorService = Executors.newFixedThreadPool(4);
//
//        for (int i = 0; i < 4; i++) {
//            int finalI = i;
//            executorService.submit(() ->{
//                        EchoClient echoClient = new EchoClient();
//                echoClient.sendMessage("HELLO-" + finalI);
//            });
//        }
//    }

    public void close() throws Exception{
        try{
            f.channel().closeFuture().sync();
        }finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }
}

