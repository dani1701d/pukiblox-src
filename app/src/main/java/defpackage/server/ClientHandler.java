package defpackage.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private World world;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        ServerData.clients.put(socket.getRemoteSocketAddress().toString(), this);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            String line;

            while ((line = in.readLine()) != null) {
                System.out.println("[CLIENT] " + line);
                PacketHandler.handle(this, line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Удаляем из мира
            if (world != null) {
                world.getPlayers().remove(this);
            }

            // Удаляем из списка клиентов
            ServerData.clients.remove(socket.getRemoteSocketAddress().toString());

            try { socket.close(); } catch (IOException ignored) {}

            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        }
    }

    public void send(String text) {
        System.out.println("[SERVER] " + text);
        out.println(text);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}