package defpackage.server;

public class PacketHandler {
    private static void sendWorldList(ClientHandler client) {
        System.out.println("Before send:");
        System.out.println(ServerData.worlds);
        System.out.println(ServerData.worlds.size());

        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for (World world : ServerData.worlds.values()) {

            if (!first) {
                sb.append(",");
            }

            sb.append(world.getName());

            first = false;
        }

        System.out.println("=== Worlds ===");

        for (World world : ServerData.worlds.values()) {
            System.out.println(world.getName());
        }

        System.out.println("==============");

        client.send("WORLDS " + sb.toString());
    }

    public static void handle(ClientHandler client, String line) {
        String[] args = line.split(" ", 3);
        String command = args[0];

        switch (command) {
            case "PING": {
                client.send("PONG");
                break;
            }

            case "LOGIN": {
                String username = args[1];
                String password = args[2];

                if (AccountManager.login(username, password)) {
                    client.setUsername(username);
                    client.send("LOGIN_OK");
                } else {
                    client.send("LOGIN_FAIL");
                }
                break;
            }

            case "GET_WORLDS": {
                sendWorldList(client);
                break;
            }

            case "JOIN": {
                String username = args[1];
                String worldName = args[2];

                World world = ServerData.worlds.get(worldName);

                if (world == null) {
                    return;
                }

                client.setUsername(username);
                if (client.getWorld() != null) {
                    client.getWorld().getPlayers().remove(client);
                }

                client.setWorld(world);
                world.getPlayers().add(client);

                client.send("MAP_DATA " + world.getMapData());

                break;
            }

            case "CREATE": {
                String worldName = line.substring("CREATE ".length());
                if (!ServerData.worlds.containsKey(worldName)) {
                    World world = new World(worldName);
                    world.setOwner(client.getUsername());
                    world.createDefaultMap();
                    ServerData.worlds.put(worldName, world);

                    System.out.println("After CREATE:");
                    System.out.println(ServerData.worlds);
                    System.out.println(ServerData.worlds.size());

                    WorldIO.save(world);
                }
                sendWorldList(client);
                break;
            }

            case "SAVE_MAP": {
                String[] split = line.split(" ", 3);
                String worldName = split[1];
                String mapData = split[2];

                World world = ServerData.worlds.get(worldName);
                if (world != null) {
                    world.setMapData(mapData);
                    WorldIO.save(world);
                }
                break;
            }

            case "MOVE": {
                // MOVE username x y headColor bodyColor shirtUrl faceUrl danceUntil isAfk hideHead
                if (args.length < 3) return;

                String username = args[1];
                String[] data = args[2].split(" ");

                if (data.length < 9) return;
                World world = client.getWorld();
                if (world == null) return;

                for (ClientHandler p : world.getPlayers()) {
                    if (p == client) continue;
                    p.send("MOVE " + args[1] + " " + args[2]);
                }
                break;
            }

            case "GET_GLOBAL_PLAYERS": {
                int online = 0;

                for (ClientHandler player : ServerData.clients.values()) {
                    if (player.getUsername() != null && !player.getUsername().isEmpty()) {
                        online++;
                    }
                }

                client.send("GLOBAL_PLAYERS " + online);
                break;
            }

            case "LEAVE_WORLD": {
                World world = client.getWorld();

                if (world != null) {
                    world.getPlayers().remove(client);
                    System.out.println(client.getUsername() + " left world " + world.getName());
                    client.setWorld(null);
                }
                break;
            }

            case "REGISTER": {
                String username = args[1];
                String password = args[2];

                if (AccountManager.register(username, password)) {
                    client.send("REGISTER_OK");
                } else {
                    client.send("REGISTER_FAIL");
                }
                break;
            }

            case "CHAT": {
                if (args.length < 3)
                    break;

                World world = client.getWorld();

                if (world == null)
                    break;

                for (ClientHandler player : world.getPlayers()) {
                    player.send(line);
                }
                break;
            }

            default:
                System.out.println("Unknown packet: " + line);
                break;
        }
    }
}