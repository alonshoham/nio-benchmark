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
package netty;

import common.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import netty.echo.HandshakeServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echoes back any received data from a client.
 */
public final class NettyServer {

    public static void main(String[] args) throws Exception {
        NettyFactory factory = NettyFactory.getDefault();
        Logger logger = LoggerFactory.getLogger(NettyServer.class);
        logger.info("Starting Netty server with {}", factory.getServerSocketChannel().getName());
        // Configure the server.
        EventLoopGroup bossGroup = factory.createEventLoopGroup(1);
        EventLoopGroup workerGroup = factory.createEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(factory.getServerSocketChannel())
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
//                     p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast(new HandshakeServerHandler());
                 }
             });

            // Start the server.
            ChannelFuture f = b.bind(Constants.PORT).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
