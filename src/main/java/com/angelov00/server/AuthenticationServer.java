package com.angelov00.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AuthenticationServer {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int MAX_BUFFER_SIZE = 1024;

    public static void main(String[] args) {

        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started at port " + SERVER_PORT);

            while(true) {
                int readyChannels = selector.select();
                // select is blocking, but may still return 0
                if(readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeySet = new HashSet<>();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if(key.isAcceptable()) {
                        acceptClient(selector, serverSocketChannel);
                    } else if (key.isReadable()) {
                        handleClientRequest(key);
                    }

                }
            }


        } catch (Exception e) {
            // TODO
            System.out.println(e.getMessage());
        }
    }

    private static void acceptClient(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        clientChannel.register(selector, SelectionKey.OP_READ, buffer);
        System.out.println("Client connected: " + clientChannel.getRemoteAddress());
    }

    private static void handleClientRequest(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        buffer.clear();
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            clientChannel.close();
            return;
        }

        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String command = new String(data).trim();

        String response = processCommand(command);

        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();
        clientChannel.write(buffer);
    }

    private static String processCommand(String command) {
        return null;
    }

}
