package netty.echo;

import common.Request;
import common.Response;
import common.Settings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serializers.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RequestResponseHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Serializer serializer = Serializer.createSerializer();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("handlerAdded: {}, serializer: {}", ctx.channel(), serializer.getClass().getName());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf requestBuf = (ByteBuf) msg;
        // deserialize request:
        Request request = serializer.deserialize(requestBuf.nioBuffer());
        requestBuf.release();
        // Fake execute request, create response (copy of request):
        Response response = new Response(request.getPayload());
        // Serialize response:
        ByteBuffer responseBuf = ByteBuffer.allocate(Settings.MAX_FRAME_LENGTH);
        serializer.serialize(responseBuf, response);
        responseBuf.flip();
        ByteBuf responseBuf2 = ctx.alloc().buffer(responseBuf.limit());
        responseBuf2.writeBytes(responseBuf);
        // write response
        ctx.writeAndFlush(responseBuf2);
    }
}
