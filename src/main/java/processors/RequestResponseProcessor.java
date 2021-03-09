package processors;

import common.*;
import serializers.Serializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RequestResponseProcessor extends ReadProcessor {
    private final ByteBuffer headerBuf = ByteBuffer.allocateDirect(4);
    private final Serializer serializer = Serializer.createSerializer();
    private ByteBuffer requestBuf;

    @Override
    public String getName() {
        return "request-response";
    }

    @Override
    public void read(SocketChannel channel) throws IOException {
        if (requestBuf == null) {
            if (!readNonBlocking(channel, headerBuf))
                return;
            int length = headerBuf.getInt();
            this.requestBuf = ByteBuffer.allocate(length);
        }
        if (!readNonBlocking(channel, requestBuf))
            return;
        // deserialize request:
        Request request = serializer.deserialize(requestBuf);
        // Fake execute request, create response (copy of request):
        Response response = new Response(request.getPayload());
        // Serialize response:
        ByteBuffer responseBuf = ByteBuffer.allocate(Settings.MAX_FRAME_LENGTH);
        serializer.serializeWithLength(responseBuf, response);
        responseBuf.flip();
        // Reset:
        headerBuf.position(0);
        requestBuf = null;
        // Write response:
        tryWrite(channel, responseBuf);
    }
}
