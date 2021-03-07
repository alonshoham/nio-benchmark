package netty.echo;

import common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class HandshakeServerHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte code = in.readByte();
        switch (code) {
            case Constants.V1_FIXED_READ_ECHO:
                ctx.pipeline().addLast(new FixedReadEchoHandler());
                break;
            case Constants.V2_FIXED_READ_SUBMIT_ECHO:
                ctx.pipeline().addLast(new FixedReadSubmitEchoHandler());
                break;
            default: throw new IllegalArgumentException("Unsupported version: " + code);
        }
        ctx.pipeline().remove(this);
    }
}
