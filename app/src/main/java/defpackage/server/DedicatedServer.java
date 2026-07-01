package defpackage.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DedicatedServer {
    private ServerSocket serverSocket;
    private boolean running = false;

    public void start() {
        try {
            serverSocket = new ServerSocket(25565);
            running = true;
            System.out.println("Server started on port 25565");

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
}