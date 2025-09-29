package org.johnils.vokabeln.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserController {
    public static final File root = new File("./data");
    private static final Logger LOGGER = LogManager.getLogger();
    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static Map<String, User> users = new ConcurrentHashMap<>();

    public static boolean createUser(String name, String password) {
        File userRoot = new File(root.getAbsolutePath() + "/" + name.trim().replace(" ", ""));

        if (userRoot.exists() || users.get(name) != null) {
            LOGGER.warn("Tried to create User '{}' that already exists", name);
            return false;
        }

        userRoot.mkdir();

        File user = new File(userRoot.getAbsolutePath() + "/user.txt");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(user));

            bw.write("name=\""+ name +"\"");
            bw.newLine();
            bw.write("password=\""+ encoder.encode(password) +"\"");
            bw.newLine();

            bw.flush();
            bw.close();
        } catch (IOException e) {
            LOGGER.error("Couldn't open user.txt for writing for User '{}'", name);
            return false;
        }

        users.put(name, new User(name,encoder.encode(password),userRoot.getAbsolutePath()));

        LOGGER.info("Successfully created User '{}'", name);
        return true;
    }
    public static void deleteUser(String name) {
        User user = users.get(name);
        if (user==null) return;

        try {
            Files.walk(new File(user.root()).toPath())
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException _) {

                        }
                    });

        } catch (IOException e) {
            LOGGER.error("Couldn't delete User '{}' {}",name,e);
            return;
        }
        LOGGER.info("Deleted User '{}'",user);
        users.remove(name);
    }

    public static boolean authorize(String user, String password) {
        return users.get(user) != null && encoder.matches(password,users.get(user).password());
    }
}
