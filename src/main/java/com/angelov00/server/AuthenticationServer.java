package com.angelov00.server;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {

        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started at port " + SERVER_PORT);

            while (true) {
                int readyChannels = selector.select();
                // select is blocking, but may still return 0
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeySet = new HashSet<>();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        buffer.clear();

                        int r = sc.read(buffer);
                        if (r < 0) {
                            System.out.println("Client has closed the connection");
                            sc.close();
                            continue;
                        }
                        buffer.flip();
                        sc.write(buffer);

                    } else if (key.isAcceptable()) {
                        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                        SocketChannel accept = sockChannel.accept();
                        accept.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        accept.register(selector, SelectionKey.OP_READ, buffer);
                    }

                    keyIterator.remove();
                }
            }


        } catch (Exception e) {
            // TODO
            System.out.println(e.getMessage());
        }
    }

}
