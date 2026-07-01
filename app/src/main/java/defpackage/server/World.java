package defpackage.server;

import java.util.HashSet;
import java.util.Set;

public class World {
    private final String name;
    private String owner;
    private String mapData;

    private final Set<ClientHandler> players = new HashSet<>();

    public World(String name) {
        this.name = name;
        this.owner = "Unknown";

        // Пустая карта
        this.mapData = "";
    }

    public void createDefaultMap() {
        String spawn =
                "400,300,-16777216,SPAWN,true,none,true,false,,40,none,300,false;";

        mapData = spawn + "|none|" + owner + "|none";
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMapData() {
        return mapData;
    }

    public void setMapData(String mapData) {
        this.mapData = mapData;
    }

    public Set<ClientHandler> getPlayers() {
        return players;
    }
}