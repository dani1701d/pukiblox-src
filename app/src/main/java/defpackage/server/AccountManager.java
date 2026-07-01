package defpackage.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AccountManager {

    private static final File ACCOUNT_FOLDER = new File("accounts");

    static {
        if (!ACCOUNT_FOLDER.exists()) {
            ACCOUNT_FOLDER.mkdirs();
        }
    }

    public static boolean register(String username, String password) {

        File file = new File(ACCOUNT_FOLDER, username + ".account");

        if (file.exists()) {
            return false;
        }

        try (PrintWriter writer =
                     new PrintWriter(file, StandardCharsets.UTF_8.name())) {

            writer.println(password);

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    public static boolean login(String username, String password) {

        File file = new File(ACCOUNT_FOLDER, username + ".account");

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(file),
                                     StandardCharsets.UTF_8))) {

            String savedPassword = reader.readLine();

            return password.equals(savedPassword);

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

}