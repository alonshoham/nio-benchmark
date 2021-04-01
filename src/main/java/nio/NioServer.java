package nio;

import common.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processors.ReadProcessor;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NioServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServerSocketChannel ssc;
    private final SelectorHandler boss;
    private final SelectorHandler[] workers;
    private int nextWorker;

    public NioServer() throws IOException {
        int numOfWorkers = Settings.WORKERS;
        logger.info("Binding to {} (I/O workers: {})", Settings.ADDRESS, numOfWorkers);
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(Settings.ADDRESS);
        boss = new SelectorHandler("boss");
        workers = numOfWorkers != -1 ? initWorkers(numOfWorkers) : new SelectorHandler[] {boss};
        ssc.register(boss.selector, SelectionKey.OP_ACCEPT);
    }

    private SelectorHandler[] initWorkers(int numOfWorkers) throws IOException {
        SelectorHandler[] result = new SelectorHandler[numOfWorkers];
        for (int i = 0; i < numOfWorkers; i++) {
            String name = "worker-" + i;
            result[i] = new SelectorHandler(name);
            Thread thread = new Thread(result[i], name);
            thread.setDaemon(true);
            thread.start();
        }
        return result;
    }

    private SelectorHandler getWorker() {
        SelectorHandler result = workers[nextWorker++];
        if (nextWorker == workers.length)
            nextWorker = 0;
        return result;
    }

    private void processAccept(SelectionKey key) throws IOException {
        SocketChannel clientSocket = ssc.accept();
        byte code = (byte) clientSocket.socket().getInputStream().read();
        ReadProcessor processor = ReadProcessor.initReader(code);
        clientSocket.configureBlocking(false);
        Settings.initSocketChannel(clientSocket);
        getWorker().register(clientSocket, processor);
    }

    private void processRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ((ReadProcessor)key.attachment()).read(channel);
    }

    public static void main(String[] args) throws IOException {
       Settings.parseArgs(args);
        new NioServer().boss.run();
    }

    private class SelectorHandler implements Runnable {
        private final Logger logger;
        private final Selector selector;
        private final BlockingQueue<RegistrationRequest> registrationRequests = new LinkedBlockingQueue<>();

        public SelectorHandler(String name) throws IOException {
            logger = LoggerFactory.getLogger(this.getClass().getName() + "." + name);
            selector = Selector.open();
        }

        public void register(SocketChannel socket, ReadProcessor processor) throws ClosedChannelException {
            if (this == boss)
                registerImpl(socket, processor);
            else
                registrationRequests.offer(new RegistrationRequest(socket, processor));
        }

        private void registerImpl(SocketChannel socket, ReadProcessor processor) throws ClosedChannelException {
            socket.register(selector, SelectionKey.OP_READ, processor);
            logger.info("Added new client {} with processor {}", socket, processor.getName());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    while (!registrationRequests.isEmpty()) {
                        RegistrationRequest request = registrationRequests.poll();
                        registerImpl(request.socketChannel, request.processor);
                    }
                    if (selector.select(100) != 0) {
                        Set<SelectionKey> readySet = selector.selectedKeys();
                        for (Iterator<SelectionKey> it = readySet.iterator(); it.hasNext(); ) {
                            final SelectionKey key = it.next();
                            it.remove();
                            try {
                                if (key.isAcceptable()) {
                                    processAccept(key);
                                } else if (key.isReadable()) {
                                    processRead(key);
                                }
                            } catch (IOException e) {
                                logger.warn("Failed to read from {} - cancelling key", key.channel(), e);
                                key.cancel();
                                if (Settings.EXIT_ON_ERROR) {
                                    logger.info("EXIT_ON_ERROR enabled - exiting...");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                }
            } catch(IOException e) {
                logger.error("Failed to process selector", e);
            }
        }
    }

    private static class RegistrationRequest {
        private final SocketChannel socketChannel;
        private final ReadProcessor processor;

        private RegistrationRequest(SocketChannel socketChannel, ReadProcessor processor) {
            this.socketChannel = socketChannel;
            this.processor = processor;
        }
    }
}
