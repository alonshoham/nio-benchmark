package netty.echo;

import common.ExecutorProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class DynamicReadSubmitReplyHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Executor executor = ExecutorProvider.executor;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("handlerAdded: {}", ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf request = (ByteBuf) msg;
        // Copy request to response:
        ByteBuf response = ctx.alloc().buffer(request.readableBytes());
        response.writeBytes(request);
        request.release();
        // Write response in a separate thread:
        executor.execute(() -> ctx.writeAndFlush(response));
    }
}
