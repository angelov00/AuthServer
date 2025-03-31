package com.angelov00.server;

import com.angelov00.server.comand.CommandHandler;
import com.angelov00.server.repository.SessionRepository;
import com.angelov00.server.repository.UserRepository;
import com.angelov00.server.repository.impl.DatabaseUserRepositoryImpl;
import com.angelov00.server.repository.impl.InMemorySessionRepositoryImpl;
import com.angelov00.server.service.AuthService;
import com.angelov00.server.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AuthenticationServer {

    private static final String LOG_FILE_PATH = "C:\\Users\\Martin\\Desktop\\log.txt";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) throws IOException {

        UserRepository userRepository = new DatabaseUserRepositoryImpl();
        SessionRepository sessionRepository = new InMemorySessionRepositoryImpl();
        Logger logger = new Logger(LOG_FILE_PATH);

        AuthService authService = new AuthService(userRepository, sessionRepository, logger);
        CommandHandler commandHandler = new CommandHandler(authService);

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

                        String receivedData = new String(buffer.array(), 0, buffer.limit(), StandardCharsets.UTF_8);
                        String clientIp = sc.getRemoteAddress().toString();

                        String response = commandHandler.handleCommand(receivedData, clientIp);

                        buffer.clear();
                        buffer.put(response.getBytes(StandardCharsets.UTF_8));
                        buffer.flip();
                        sc.write(buffer);
                    }
                    else if (key.isAcceptable()) {
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
            System.out.println(e.getMessage());
        }
    }

}
