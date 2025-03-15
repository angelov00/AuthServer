package com.angelov00.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class AuthenticationClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 1024;
    private static final String sessionId = null;

    public static void main(String[] args) {

        try(SocketChannel socketChannel = SocketChannel.open();
            Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the Authentication Server!");

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while(true) {
                System.out.print("> ");
                String command = scanner.nextLine().trim();

                if(command.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection...");
                    break;
                }

                sendCommand(socketChannel, buffer, command);

                String response = readResponse(socketChannel, buffer);
                System.out.println(response);
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sendCommand(SocketChannel socketChannel, ByteBuffer buffer, String command) throws IOException {
        buffer.clear();
        buffer.put(command.getBytes());
        buffer.flip();
        while(buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private static String readResponse(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if(bytesRead == -1) {
            // TODO maybe EOFException?
            throw new IOException("Server closed the connection.");
        }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        return new String(data).trim();
    }
}
