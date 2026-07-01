package defpackage.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WorldIO {
    private static final File WORLD_FOLDER = new File("worlds");
    static {
        if (!WORLD_FOLDER.exists()) {
            WORLD_FOLDER.mkdirs();
        }
    }

    public static void save(World world) {
        try {
            File file = new File(WORLD_FOLDER, world.getName() + ".map");
            PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8.name());
            writer.print(world.getMapData());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAll() {
        File[] files = WORLD_FOLDER.listFiles();
        if (files == null)
            return;
        System.out.println("Loading worlds...");
        for (File file : files) {
            if (!file.getName().endsWith(".map"))
                continue;
            try {
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(file),
                                        StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                System.out.println("Found file: " + file.getName());
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                String worldName = file.getName().substring(0, file.getName().length() - 4);
                World world = new World(worldName);
                world.setMapData(sb.toString());
                ServerData.worlds.put(worldName, world);
                System.out.println("Loaded world: " + worldName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Loaded worlds: " + ServerData.worlds.size());
    }
}