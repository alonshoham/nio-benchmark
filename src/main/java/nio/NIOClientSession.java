package nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NIOClientSession {
    SocketChannel clientSocket;
    ByteBuffer buff = ByteBuffer.allocateDirect(256);

    NIOClientSession(SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
    }

    void echo() throws IOException {
        if(clientSocket.read(buff) == -1 || buff.get(buff.position()-1) == '\n'){
            buff.flip();
            clientSocket.write(buff);
            buff.clear();
        }
    }
}
