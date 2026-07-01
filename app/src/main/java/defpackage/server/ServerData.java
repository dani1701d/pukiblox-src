package defpackage.server;

import java.util.concurrent.ConcurrentHashMap;

public class ServerData {
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, World> worlds = new ConcurrentHashMap<>();
}