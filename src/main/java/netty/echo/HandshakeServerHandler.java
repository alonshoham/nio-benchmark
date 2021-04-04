package netty.echo;

import common.Settings;
import common.RequestType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HandshakeServerHandler extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int HEADER_SIZE = 4;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte code = in.readByte();
        switch (RequestType.valueOf(code)) {
            case V0_TERMINATE:
                logger.info("Received exit command - terminating...");
                System.exit(0);
                break;
            case V1_FIXED_READ_ECHO:
                ctx.pipeline().addLast(new FixedReadEchoHandler());
                break;
            case V2_FIXED_READ_SUBMIT_ECHO:
                ctx.pipeline().addLast(new FixedReadSubmitEchoHandler());
                break;
            case V3_DYNAMIC_READ_REPLY:
                ctx.pipeline().addLast(createLengthDecoder(), createLengthEncoder(), new DynamicReadReplyHandler());
                break;
            case V4_DYNAMIC_READ_SUBMIT_REPLY:
                ctx.pipeline().addLast(createLengthDecoder(), createLengthEncoder(), new DynamicReadSubmitReplyHandler());
                break;
            case V5_REQUEST_RESPONSE:
                ctx.pipeline().addLast(createLengthDecoder(), createLengthEncoder(), new RequestResponseHandler());
                break;
            default: throw new IllegalArgumentException("Unsupported version: " + code);
        }
        ctx.pipeline().remove(this);
    }

    private ChannelInboundHandler createLengthDecoder() {
        return new LengthFieldBasedFrameDecoder(
                Settings.MAX_FRAME_LENGTH, 0, HEADER_SIZE, 0, HEADER_SIZE);
    }

    private ChannelOutboundHandler createLengthEncoder() {
        return new LengthFieldPrepender(HEADER_SIZE);
    }
}
