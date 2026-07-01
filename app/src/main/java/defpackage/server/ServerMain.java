package defpackage.server;

public class ServerMain {
    public static void main(String[] args) {
        WorldIO.loadAll();
        new DedicatedServer().start();
    }
}